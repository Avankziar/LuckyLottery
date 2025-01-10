package me.avankziar.lly.spigot.handler.lottery;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.assistance.Utility;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.objects.Advertising;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.KeepsakeItem;
import me.avankziar.lly.general.objects.PlayerData;
import me.avankziar.lly.general.objects.ScratchCardField;
import me.avankziar.lly.general.objects.WinningClass.PayoutType;
import me.avankziar.lly.general.objects.WinningClassSuper;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lottery.ScratchCard;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.general.objects.lottery.payout.ClassicLottoPayout;
import me.avankziar.lly.general.objects.lottery.payout.LottoSuperPayout;
import me.avankziar.lly.general.objects.lottery.ticket.LottoSuperTicket;
import me.avankziar.lly.general.objects.lottery.ticket.ScratchCardTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;

public class ScratchCardHandler
{
	protected static Collection<ScratchCard> scratchcard = new HashSet<>();
	
	public static void initalizedYamls()
	{
		for(YamlDocument y : LLY.getPlugin().getYamlHandler().getScratchCard())
		{
			try
			{
				if(!y.contains("LotteryName") || !y.contains("Description")
						|| !y.contains("ScratchCardField.1.WinningAmount")
						|| !y.contains("ScratchCardField.1.Chance")
						|| !y.contains("ScratchCardField.1.Display"))
				{
					LLY.log.warning(
							y.getFile().getName()+".yaml is missing essential values! "
									+ "Please check these otherwise the lottery cannot be registered!");
					continue;
				}
				String lottoname = y.getString("LotteryName");
				String description = y.getString("Description");
				double costPerTicket = y.getDouble("CostPerTicket", 2.5);
				int amountOfField = y.getInt("AmountOfField");
				int amountOfSameFieldToWin = y.getInt("AmountOfSameFieldToWin");
				ArrayList<ScratchCardField> scratchCardField = new ArrayList<>();
				boolean check = false;
				for(int i = 1; i <= 1000; i++)
				{
					if(!y.contains("ScratchCardField."+i+".WinningAmount")
							|| !y.contains("ScratchCardField."+i+".Chance")
							|| !y.contains("ScratchCardField."+i+".Display"))
					{
						LLY.log.warning(lottoname+" ScratchCardField number "+i+" is missing! "+
								lottoname+" will not be skiped!");
						continue;
					}
					double winningAmount = y.getDouble("ScratchCardField."+i+".WinningAmount");
					double chance = y.getDouble("ScratchCardField."+i+".Chance");
					String display = y.getString("ScratchCardField."+i+".Display");
					KeepsakeItem ksi = null;
					if(y.contains("ScratchCardField."+i+".KeepsakeItem.Material")
							&& y.contains("ScratchCardField."+i+".KeepsakeItem.Amount")
							&& y.contains("ScratchCardField."+i+".KeepsakeItem.Displayname")
							&& y.contains("ScratchCardField."+i+".KeepsakeItem.EnchantmentGlintOverride"))
					{
						try
						{
							Material material = Material.valueOf(y.getString("ScratchCardField."+i+".KeepsakeItem.Material"));
							int amount = y.getInt("ScratchCardField."+i+".KeepsakeItem.Amount");
							amount = amount > 64 ? 64 : (amount < 1 ? 1 : amount);
							String displayname = y.getString("ScratchCardField."+i+".KeepsakeItem.Displayname");
							ArrayList<String> lore = new ArrayList<>();
							if(y.contains("ScratchCardField."+i+".Lore"))
							{
								lore = (ArrayList<String>) y.getStringList("ScratchCardField."+i+".KeepsakeItem.Lore");
							}
							String ego = y.getString("ScratchCardField."+i+".KeepsakeItem.EnchantmentGlintOverride");
							Boolean enchantmentGlintOverride = ego.equals("null") ? null : (ego.equals("true") ? true : false);
							ksi = new KeepsakeItem(material, amount, displayname, lore, enchantmentGlintOverride);
							if(y.contains("ScratchCardField."+i+".KeepsakeItem.Enchantment"))
							{
								for(String s : y.getStringList("ScratchCardField."+i+".KeepsakeItem.Enchantment"))
								{
									String[] sp = s.split(";");
									if(sp.length != 2)
									{
										LLY.log.warning(lottoname+" ScratchCardField number "+i+" is has a small issue! "+
												"KeepsakeItem Enchantment is skipt: "+s);
										continue;
									}
									ksi.addEnchantment(sp[0].toLowerCase(), Integer.valueOf(sp[1]));
								}
							}
							if(y.contains("ScratchCardField."+i+".KeepsakeItem.ItemFlag"))
							{
								for(String s : y.getStringList("ScratchCardField."+i+".KeepsakeItem.ItemFlag"))
								{
									ksi.addItemFlag(ItemFlag.valueOf(s));
								}
							}
						} catch(Exception e)
						{
							LLY.log.warning(lottoname+" ScratchCardField number "+i+" is has a small issue! "+
									"Keepsake Item was skipt");
							continue;
						}
					}
					ScratchCardField scf = new ScratchCardField(winningAmount, chance, display, ksi);
					scratchCardField.add(scf);
				}
				if(scratchCardField.size() < 5)
				{
					LLY.log.warning(lottoname+" ScratchCardField amount < 5! "+
							"ScratchCard will not be registered!");
					continue;
				}
				ArrayList<Advertising> advertising = new ArrayList<>();
				for(int i = 1; i < 100; i++)
				{
					if(!y.contains("Advertising."+i+".Message") || !y.contains("Advertising."+i+".Time"))
					{
						continue;
					}
					boolean active = y.getBoolean("Advertising."+i+".IsActive", false);
					boolean canIgnored = y.getBoolean("Advertising."+i+".CanIgnore", false);
					ArrayList<String> message = (ArrayList<String>) y.getStringList("Advertising."+i+".Message");
					ArrayList<DrawTime> time = new ArrayList<>();
					for(String s : y.getStringList("Advertising."+i+".Time"))
					{
						String[] a = s.split("-");
						if(a.length != 4)
						{
							LLY.log.warning(lottoname+" has by AdvertisingTime a Problem! "
											+ "AdvertisingTime "+s+" missed 3 of `-` Character! "
											+ "AdvertisingTime was not registered!");
							continue;
						}
						try
						{
							int weekOfMonth = Integer.valueOf(a[0]);
							weekOfMonth = weekOfMonth > 5 ? 5 
									: (weekOfMonth < 1 ? 1 : weekOfMonth);
							DayOfWeek dayOfWeek = DayOfWeek.valueOf(a[1]);
							int hour = Integer.valueOf(a[2]);
							hour = hour > 23 ? 23
									: (hour < 0 ? 0 : hour);
							int min = Integer.valueOf(a[3]);
							min = min > 59 ? 59
									: (min < 0 ? 0 : min);
							time.add(new DrawTime(weekOfMonth, dayOfWeek, hour, min));
						} catch(Exception e)
						{
							LLY.log.warning(lottoname+" has by AdvertisingTime a Problem! "
									+ "AdvertisingTime "+s+" is incorrect!");
							continue;
						}
					}
					advertising.add(new Advertising(active, canIgnored, message, time));
				}
				if(check)
				{
					continue;
				}
				int fieldPerLine = y.getInt("Display.FieldPerLine");
				ArrayList<String> displayFieldUnscratched = new ArrayList<>();
				ArrayList<String> displayFieldScratched = new ArrayList<>();
				ScratchCard sc = new ScratchCard(lottoname, description, GameType.X_NUMBER_OF_FIELDS,
						costPerTicket, amountOfField, amountOfSameFieldToWin, scratchCardField, advertising,
						fieldPerLine, displayFieldUnscratched, displayFieldScratched);
				LLY.log.info("ScratchCard "+lottoname+" loaded!");
				scratchcard.add(sc);
				sc.getTicketMysql().setupMysql(LLY.getPlugin().getMysqlSetup(), me.avankziar.lly.general.database.ServerType.ALL);
			} catch(Exception e)
			{
				continue;
			}
		}
	}
	
	public static void initalizedDraws()
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				asyncDraw();
			}
		}.runTaskTimer(LLY.getPlugin(), 0L, 60*20L);
	}
	
	private static void asyncDraw()
	{
		final LocalDateTime ldt = LocalDateTime.now();
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{	
				for(ScratchCard ls : LotteryHandler.getScratchCard())
				{
					for(Advertising ad : ls.getAdvertising())
					{
						if(!ad.isActive())
						{
							continue;
						}
						for(DrawTime dr : ad.getDrawTime())
						{
							if(dr.isNow(ldt))
							{
								advertise(ls, ad);
							}
						}
					}
				}
			}
		}.runTaskAsynchronously(LLY.getPlugin());
	}
	
	private static void advertise(final ScratchCard ls, Advertising ad)
	{
		String[] sa = replacer(ls, null, null, null, null, false,
				ad.getMessage()).toArray(new String[ad.getMessage().size()]);
		for(Player player : Bukkit.getOnlinePlayers())
		{
			PlayerData pd = LLY.getPlugin().getMysqlHandler().getData(new PlayerData(), "`player_uuid` = ?", player.getUniqueId().toString());
			if(pd == null || (ad.canIgnored() && pd.isIgnoreAdvertising()))
			{
				continue;
			}
			MessageHandler.sendMessage(player, sa);
		}
	}
	
	public static void drawLotto(final ScratchCard sc, ArrayList<ScratchCardField> drawFields)
	{
		LLY plugin = LLY.getPlugin();
		long drawTime = System.currentTimeMillis();
		//Lottery Draw
		ArrayList<ScratchCardField> drawFields = new ArrayList<>();
		
		
		
		//Update Object
		lsd.setChoosenNumbers(drawnNumber);
		lsd.setDrawTime(drawTime);
		lsd.setWasDrawn(true);
		//Lotto Ticket call
		ArrayList<LottoSuperTicket> lstA = plugin.getMysqlHandler().getFullList(
				sc.getTicketMysql(), "`id` ASC", "`draw_id` = ?", lsd.getId());
		if(lstA.isEmpty())
		{
			//No Tickets was bought
			List<String> msgl = replacer(sc, lsd, null, null, null, null, false,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("LottoSuper.Draw.NoTicketAreBought"));
			MessageHandler.sendMessage(msgl.toArray(new String[msgl.size()]));
			double nextpot = lsd.getActualPot();
			if(nextpot < sc.getStandartPot())
			{
				nextpot = sc.getStandartPot();
			}
			nextpot += sc.getAmountToAddToThePotIfNoOneIsWinning();
			if(nextpot > sc.getMaximumPot())
			{
				nextpot = sc.getMaximumPot();
			}
			plugin.getMysqlHandler().updateData(lsd, "`id` = ?", lsd.getId());
			if(reopen || !sc.isDrawManually())
			{
				LottoSuperDraw cldNext = new LottoSuperDraw(0, sc.getLotteryName(), false, 0, nextpot,
						new LinkedHashSet<>(), new LinkedHashSet<>());
				plugin.getMysqlHandler().create(cldNext);
			}
			return;
		}
		//Get Next ClassicLottoDraw ID
		int lsdNextId = plugin.getMysqlHandler().lastID(lsd)+1;
		//Adding all WinningClass to a payout object
		ArrayList<LottoSuperPayout> lspA = new ArrayList<>();
		//Create all WinningClasss
		int highestWC = 1;
		int lowestWC = 1;
		for(WinningClassSuper wcs : sc.getWinningClassSuper())
		{
			double payout = 0.0;
			switch(wcs.getPayoutType())
			{
			case LUMP_SUM: payout = wcs.getAmount(); break;
			case PERCENTAGE: payout = lsd.getActualPot() * wcs.getAmount() / 100; break;
			}
			lspA.add(new LottoSuperPayout(wcs.getWinningClassLevel(),
					wcs.getPayoutType(),
					payout,
					new HashSet<UUID>(), 0,
					wcs.getNumberMatchToWin(),
					wcs.getAddtionalNumberMatchToWin()));
			if(lowestWC < wcs.getWinningClassLevel())
			{
				lowestWC = wcs.getWinningClassLevel();
			}
		}
		//Added WinningClass for all player that lost, the int is the highest level
		lspA.add(new LottoSuperPayout(lowestWC+1, PayoutType.LUMP_SUM, 0, new HashSet<UUID>(), 0, 0, 0));
		//Lotto Ticket Evaluation
		String repeatCategory = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.RepeatTicket.Category")
				.replace("%lotteryname%", sc.getLotteryName());
		String repeatComment = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.RepeatTicket.Comment");
		LinkedHashMap<Integer, ArrayList<LottoSuperTicket>> payoutToTicket = new LinkedHashMap<>();
		for(LottoSuperTicket lst : lstA)
		{
			//Amount of number which match
			int matchNumber = matchChoosenNumber(drawnNumber, lst.getChoosenNumbers());
			int additionalMatchNumber = matchChoosenNumber(addtitionalDrawnNumber, lst.getAdditionalChoosenNumbers());
			//Payout Index which is WinningClass
			int index = getIndexPayout(lspA, matchNumber, additionalMatchNumber, additionalMatchNumber);
			//Getting the PayoutObject
			LottoSuperPayout lsp = getPayout(lspA, index);
			lsp.getUUIDs().add(lst.getLotteryPlayer());
			lsp.setWinnersAmount(lsp.getWinnersAmount()+1);
			lspA.set(index, lsp);
			if(lst.shouldRepeate())
			{
				//If ticket should repeat, create new one and update the old one.
				double price = sc.getCostPerTicket();
				if(EconomyHandler.hasBalance(lst.getLotteryPlayer(), price))
				{
					if(price > 0.0)
					{
						EconomyHandler.withdraw(lst.getLotteryPlayer(), price, repeatCategory, repeatComment);
					}
					LottoSuperTicket lstRepeat = new LottoSuperTicket(0, 
							lsdNextId, sc.getLotteryName(), lst.getLotteryPlayer(),
							true, 0, 0, lst.getChoosenNumbers(), lst.getAdditionalChoosenNumbers());
					plugin.getMysqlHandler().create(lstRepeat);
				}
				lst.setShouldRepeate(false);
				plugin.getMysqlHandler().updateData(lst, "`id` = ?", lst.getID());
			}
			//Adding all Tickets to a list which is put in a LinkedHashMap sorted after CLP Level.
			ArrayList<LottoSuperTicket> lstATPo = new ArrayList<>();
			if(payoutToTicket.containsKey(lsp.getWinningClassLevel()))
			{
				lstATPo = payoutToTicket.get(lsp.getWinningClassLevel());
			}
			lstATPo.add(lst);
			payoutToTicket.put(lsp.getWinningClassLevel(), lstATPo);
		}
		//Highest WinningClass scored?
		LottoSuperPayout hlsp = getPayout(lspA, highestWC);
		ArrayList<String> jackpotWinners = new ArrayList<>();
		ArrayList<String> globalMsg = new ArrayList<>();
		if(hlsp.getUUIDs().size() > 0)
		{
			hlsp.getUUIDs().stream().forEach(x -> jackpotWinners.add(Utility.convertUUIDToName(x.toString())));
			double payout = lsd.getActualPot();
			globalMsg = replacer(sc, lsd, null, lspA, payout, String.join(", ", jackpotWinners), hlsp.getUUIDs().size() > 0,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("LottoSuper.Draw.JackpotWasBreached"));
		} else
		{
			double payout = lsd.getActualPot();
			globalMsg = replacer(sc, lsd, null, lspA, payout, String.join(", ", jackpotWinners), hlsp.getUUIDs().size() > 0,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("LottoSuper.Draw.JackpotIsUntouched"));
		}
		//Send all OnlinePlayers the normal message
		MessageHandler.sendMessage(globalMsg.toArray(new String[globalMsg.size()]));
		//Put all Player in a hashset to check if there won to not send a "you lost" msg.
		//Therefor reverse the sorting of the clp.
		lspA.sort(Comparator.comparingInt(LottoSuperPayout::getWinningClassLevel).reversed());
		//Give Players a the price and send a message
		double nextpot = 0.0;
		for(LottoSuperPayout lsp : lspA)
		{
			int lv = lsp.getWinningClassLevel();
			if(lsp.getUUIDs().size() == 0)
			{
				//No one won in this payout/WinningClass. Added in Next pot if the payouttype is percentage.
				if(lsp.getPayoutType() == PayoutType.PERCENTAGE)
				{
					nextpot += lsp.getPayout();
				}
				continue;
			}
			if(lsp.getPayout() <= 0.0)
			{
				//CLP Lv 0 for all player which has Lost AND where other ticket of the same player didnt also not win.
				for(LottoSuperTicket lst : payoutToTicket.get(lv))
				{
					ArrayList<String> nowinmsg = new ArrayList<>();
					nowinmsg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.NotWon"));
					nowinmsg = replacer(sc, lsd, lst, lspA, null, null, hlsp.getUUIDs().size() > 0,
							nowinmsg);
					MessageHandler.sendMessage(lst.getLotteryPlayer(), nowinmsg.toArray(new String[nowinmsg.size()]));
					lst.setWinningClassLevel(lv);
					plugin.getMysqlHandler().updateData(lst, "`id` = ?", lst.getID());
				}
				continue;
			}
			double payout = lsp.getFinalPayoutPerUUID();
			String wincategory = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.Win.Category");
			String wincomment = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.Win.Comment")
					.replace("%lotteryname%", sc.getLotteryName())
					.replace("%level%", String.valueOf(lv));
			WinningClassSuper wcs = sc.getWinningClassSuper().stream()
					.filter(x -> x.getWinningClassLevel() == lv)
					.findAny().orElse(null);
			for(LottoSuperTicket lst : payoutToTicket.get(lv))
			{
				ArrayList<String> winmsg = replacer(sc, lsd, lst, lspA, payout, null, hlsp.getUUIDs().size() > 0,
						(ArrayList<String>) plugin.getYamlHandler().getLang().getStringList("LottoSuper.Draw.Won"));
				EconomyHandler.deposit(lst.getLotteryPlayer(), payout, 
						wincategory, 
						wincomment);
				MessageHandler.sendMessage(lst.getLotteryPlayer(), winmsg.toArray(new String[winmsg.size()]));
				lst.setWinningClassLevel(lv);
				lst.setWinningPrize(payout);
				plugin.getMysqlHandler().updateData(lst, "`id` = ?", lst.getID());
				if(wcs != null)
				{
					wcs.getExecutableCommands().forEach(x -> x.execute(lst.getLotteryPlayer()));
					if(wcs.getKeepsakeItem() != null)
					{
						String displayname = 
								replacer(sc, lsd, lst, lspA, null, null, false,
										wcs.getKeepsakeItem().getDisplayname());
						ArrayList<String> lore = replacer(sc, lsd, lst, lspA, null, null, false,
								wcs.getKeepsakeItem().getLore());
						wcs.getKeepsakeItem().sendItem(lst.getLotteryPlayer(), displayname, lore);
					}
				}
			}
		}
		plugin.getMysqlHandler().updateData(lsd, "`id` = ?", lsd.getId());
		//Determine next pot
		if(hlsp.getUUIDs().size() > 0)
		{
			nextpot = sc.getStandartPot();
		} else
		{
			if(nextpot < sc.getStandartPot())
			{
				nextpot = sc.getStandartPot();
			}
			nextpot += sc.getAmountToAddToThePotIfNoOneIsWinning();
			if(nextpot > sc.getMaximumPot())
			{
				nextpot = sc.getMaximumPot();
			}
		}
		//Create next lotto draw.
		if(reopen || !sc.isDrawManually())
		{
			LottoSuperDraw lsdNext = new LottoSuperDraw(0, sc.getLotteryName(), false, 0, nextpot,
					new LinkedHashSet<>(), new LinkedHashSet<>());
			plugin.getMysqlHandler().create(lsdNext);
		}
	}
	
	public static ArrayList<ScratchCardField> drawLotteryNumber(int amountOfField, ArrayList<ScratchCardField> scfA)
	{
		ArrayList<ScratchCardField> list = new ArrayList<>();
		while(list.size() <= amountOfField)
		{
			ScratchCardField scf = ScratchCardField.roll(scfA, 0);
			if(scf != null)
			{
				list.add(scf);
			}
		}
		return list;
	}
	
	/**
	 * Replacer for message in the drawing of the lottery.
	 */
	public static ArrayList<String> replacer(ScratchCard ls, ScratchCardTicket lst,
			ArrayList<ScratchCardPayout> lspA, Double payout, String jackpotwinners, boolean wasJackpotBreached,
			ArrayList<String> list)
	{
		ArrayList<String> li = new ArrayList<>();
		for(String s : list)
		{
			String r = replacer(ls, lst, lspA, payout, jackpotwinners, wasJackpotBreached, s);
			li.add(r);
		}
		return li;
	}
	
	public static String replacer(ScratchCard ls, ScratchCardTicket lst,
			ArrayList<ScratchCardPayout> lspA, Double payout, String jackpotwinners, boolean wasJackpotBreached,
			String r)
	{
		String s = r;
		if(payout != null)
		{
			s = s.replace("%payout%", EconomyHandler.format(payout));
		} else
		{
			s = s.replace("%payout%", "/");
		}
		if(jackpotwinners != null)
		{
			s = s.replace("%winners%", jackpotwinners);
		} else
		{
			s = s.replace("%winners%", "/");
		}
		if(s.contains("%lottosupercmd%"))
		{
			s = s.replace("%lottosupercmd%", CommandSuggest.getCmdString(CommandSuggest.Type.LOTTOSUPER));
		}
		if(s.contains("%lottosuperbet%"))
		{
			s = s.replace("%lottosuperbet%", CommandSuggest.getCmdString(CommandSuggest.Type.LOTTOSUPER_PLAY));
		}
		if(ls != null)
		{
			s = s.replace("%lotteryname%", ls.getLotteryName())
					.replace("%description%", ls.getDescription())
					.replace("%amountofchoosennumber%", String.valueOf(ls.getAmountOfFields()))
					.replace("%additionalamountofchoosennumber%", String.valueOf(ls.getAmountOfSameFieldToWin()))
					.replace("%jackpotamount%", EconomyHandler.format(ls.getJackpot().getWinningAmount()))
					.replace("%costperticket%", EconomyHandler.format(ls.getCostPerTicket()));
		} else
		{
			s = s.replace("%lotteryname%", "/")
					.replace("%description%", "/")
					.replace("%amountofchoosennumber%", "/")
					.replace("%additionalamountofchoosennumber%", "/")
					.replace("%jackpotamount%", "/")
					.replace("%costperticket%", "/");
		}
		if(lst != null)
		{
			s = s.replace("%lotteryname%", lst.getLotteryName())
					.replace("%ticketid%", String.valueOf(lst.getID()))
					.replace("%ticketplayer%", Utility.convertUUIDToName(lst.getLotteryPlayer().toString()));
		} else
		{
			s = s.replace("%lotteryname%", "/")
					.replace("%ticketid%", "/")
					.replace("%ticketplayer%", "/");
		}
		if(lspA != null)
		{
			StringBuilder wcwinneramount = new StringBuilder();
			double nextpot = 0.0;
			for(int i = 0; i < lspA.size(); i++)
			{
				if(lspA.get(i) == null)
				{
					continue;
				}
				ClassicLottoPayout clp = lspA.get(i);
				wcwinneramount.append(LLY.getPlugin().getYamlHandler().getLang()
						.getString("LottoSuper.Draw.WinningClassReplacer")
						.replace("%level%", String.valueOf(clp.getWinningClassLevel()))
						.replace("%winneramount%", String.valueOf(clp.getWinnersAmount())));
				if(i+1 < lspA.size())
				{
					wcwinneramount.append(LLY.getPlugin().getYamlHandler().getLang()
							.getString("LottoSuper.Draw.WinningClassReplacerSeperator"));
				}
				if(clp.getUUIDs().size() <= 0)
				{
					nextpot += clp.getPayout();
				} else
				{
					s = s.replace("%winningclass"+i+"payout%", EconomyHandler.format(clp.getFinalPayoutPerUUID()));
				}
			}
			s = s.replace("%winningclasswinneramount%", wcwinneramount.toString());
		} else
		{
			s = s.replace("%winningclasswinneramount%", "/")
					.replace("%nextpot%", "/");
		}
		return s;
	}
	
	public static String matchChoosenNumber(LinkedHashSet<Integer> set, int i)
	{
		return set.contains(i) 
				? LLY.getPlugin().getYamlHandler().getLang().getString("WasChoosen")
						.replace("%number%", String.valueOf(i))
				: LLY.getPlugin().getYamlHandler().getLang().getString("WasntChoosen")
						.replace("%number%", String.valueOf(i)); 
	}
	
	public static LottoSuperPayout getPayout(ArrayList<LottoSuperPayout> list, int level)
	{
		Optional<LottoSuperPayout> op = list.stream()
				.filter(x -> x.getWinningClassLevel() == level)
				.findFirst();
		return op.isPresent() ? op.get() : null;
	}
	
	public static int getIndexPayout(ArrayList<LottoSuperPayout> list, 
	        int matchNumber, int additionMatchNumber, int originalAdditionMatchNumber) 
	{
	    for (LottoSuperPayout lsp : list) 
	    {
	        if (lsp.getNumberMatchToWin() == matchNumber
	                && lsp.getAddtionalNumberMatchToWin() == additionMatchNumber) 
	        {
	            return lsp.getWinningClassLevel();
	        }
	    }
	    if (additionMatchNumber > 0) 
	    {
	        return getIndexPayout(list, matchNumber, additionMatchNumber - 1, originalAdditionMatchNumber);
	    }
	    if (matchNumber > 0) 
	    {
	        return getIndexPayout(list, matchNumber - 1, originalAdditionMatchNumber, originalAdditionMatchNumber);
	    }
	    return 0;
	}
	
	public static int matchChoosenNumber(LinkedHashSet<Integer> set, LinkedHashSet<Integer> choosenNumber)
	{
		int i = 0;
		for(Iterator<Integer> iter = set.iterator(); iter.hasNext();)
		{
			if(choosenNumber.contains(iter.next()))
			{
				i++;
			}
		}
		return i;
	}
}