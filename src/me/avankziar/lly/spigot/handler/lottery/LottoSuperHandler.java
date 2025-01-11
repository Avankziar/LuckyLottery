package me.avankziar.lly.spigot.handler.lottery;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.scheduler.BukkitRunnable;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.assistance.TimeHandler;
import me.avankziar.lly.general.assistance.Utility;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.objects.Advertising;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.ExecutableCommand;
import me.avankziar.lly.general.objects.ExecutableCommand.ServerType;
import me.avankziar.lly.general.objects.KeepsakeItem;
import me.avankziar.lly.general.objects.PlayerData;
import me.avankziar.lly.general.objects.WinningClass.PayoutType;
import me.avankziar.lly.general.objects.WinningClassSuper;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.general.objects.lottery.payout.ClassicLottoPayout;
import me.avankziar.lly.general.objects.lottery.payout.LottoSuperPayout;
import me.avankziar.lly.general.objects.lottery.ticket.LottoSuperTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;

public class LottoSuperHandler 
{
	protected static Collection<LottoSuper> lottosuper = new HashSet<>();
	
	public static void initalizedYamls()
	{
		for(YamlDocument y : LLY.getPlugin().getYamlHandler().getLottoSuper())
		{
			try
			{
				if(!y.contains("LotteryName") || !y.contains("Description")
						|| !y.contains("WinningClass.1.Payout"))
				{
					LLY.log.warning(
							y.getFile().getName()+".yaml is missing essential values! "
									+ "Please check these otherwise the lottery cannot be registered!");
					continue;
				}
				String lottoname = y.getString("LotteryName");
				String description = y.getString("Description");
				double standartPot = y.getDouble("StandartPot", 1_000_000.0);
				double maximumPot = y.getDouble("MaximumPot", 10_000_000.0);
				double amountToAddToThePotIfNoOneIsWinning = y.getDouble("AmountToAddToThePotIfNoOneIsWinning", 500_000.0);
				double costPerTicket = y.getDouble("CostPerTicket", 2.5);
				int maximalAmountOfTicketWhichCanAPlayerBuy = y.getInt("maximalAmountOfTicketWhichCanAPlayerBuy", -1);
				int fristNumberToChooseFrom = y.getInt("FristNumberToChooseFrom", 1);
				int lastNumberToChooseFrom = y.getInt("LastNumberToChooseFrom", 49);
				int amountOfChoosedNumber = y.getInt("AmountOfChoosedNumber", 6);
				int additionalFristNumberToChooseFrom = y.getInt("AdditionalFristNumberToChooseFrom", 1);
				int additionalLastNumberToChooseFrom = y.getInt("AdditionalLastNumberToChooseFrom", 49);
				int additionalAmountOfChoosedNumber = y.getInt("AdditionalAmountOfChoosedNumber", 6);
				ArrayList<DrawTime> drawTime = new ArrayList<>();
				if(y.get("DrawTime") != null)
				{
					for(String s : y.getStringList("DrawTime"))
					{
						String[] a = s.split("-");
						if(a.length != 4)
						{
							LLY.log.warning(lottoname+" has by DrawTime a Problem! "
											+ "DrawTime "+s+" missed 3 of `-` Character! "
											+ "DrawTime was not registered!");
							continue;
						}
						try
						{
							int weekOfMonth = Integer.valueOf(a[0]);
							DayOfWeek dayOfWeek = DayOfWeek.valueOf(a[1]);
							int hour = Integer.valueOf(a[2]);
							int min = Integer.valueOf(a[3]);
							drawTime.add(new DrawTime(weekOfMonth, dayOfWeek, hour, min));
						} catch(Exception e)
						{
							LLY.log.warning(lottoname+" has by DrawTime a Problem! "
									+ "DrawTime "+s+" is incorrect!");
							continue;
						}
					}
				}			
				String drawOnServer = y.getString("DrawOnServer", "hub");
				boolean drawManually = y.getBoolean("DrawManually", false);
				LinkedHashSet<WinningClassSuper> winningClassSuper = new LinkedHashSet<>();
				for(int i = 1; i <= amountOfChoosedNumber * (additionalAmountOfChoosedNumber+1); i++)
				{
					if(!y.contains("WinningClass."+i+".Payout"))
					{
						break;
					}
					PayoutType payt = PayoutType.valueOf(y.getString("WinningClass."+i+".PayoutType", PayoutType.LUMP_SUM.toString()));
					double payout = y.getDouble("WinningClass."+i+".Payout");
					int numberMatchToWin = y.getInt("WinningClass."+i+".NumberMatchToWin");
					int additionalNumberMatchToWin = y.getInt("WinningClass."+i+".AdditionalNumberMatchToWin");
					ArrayList<ExecutableCommand> execcmd = new ArrayList<>();
					if(y.contains("WinningClass."+i+".ExecutableCommand"))
					{
						for(String s : y.getStringList("WinningClass."+i+".ExecutableCommand"))
						{
							String[] sp = s.split(";");
							if(sp.length != 6)
							{
								LLY.log.warning(lottoname+" WinningClass number "+i+" is has a small issue! "+
										"ExecutableCommands skipt the row: "+s);
								continue;
							}
							try
							{
								ServerType serverType = ServerType.valueOf(sp[0]);
								int chanceToExecute = Integer.valueOf(sp[1]);
								int startRandom = Integer.valueOf(sp[2]);
								int endRandom = Integer.valueOf(sp[3]);
								boolean shouldRandomAsDouble = Boolean.valueOf(sp[4]);
								String command = sp[5];
								execcmd.add(new ExecutableCommand(serverType, command, chanceToExecute, startRandom, endRandom, shouldRandomAsDouble));
							} catch(Exception e)
							{
								LLY.log.warning(lottoname+" WinningClass number "+i+" is has a small issue! "+
										"ExecutableCommands skipt the row: "+s);
								continue;
							}
						}
					}
					KeepsakeItem ksi = null;
					if(y.contains("WinningClass."+i+".KeepsakeItem.Material")
							&& y.contains("WinningClass."+i+".KeepsakeItem.Amount")
							&& y.contains("WinningClass."+i+".KeepsakeItem.Displayname")
							&& y.contains("WinningClass."+i+".KeepsakeItem.EnchantmentGlintOverride"))
					{
						try
						{
							Material material = Material.valueOf(y.getString("WinningClass."+i+".KeepsakeItem.Material"));
							int amount = y.getInt("WinningClass."+i+".KeepsakeItem.Amount");
							amount = amount > 64 ? 64 : (amount < 1 ? 1 : amount);
							String displayname = y.getString("WinningClass."+i+".KeepsakeItem.Displayname");
							ArrayList<String> lore = new ArrayList<>();
							if(y.contains("WinningClass."+i+".Lore"))
							{
								lore = (ArrayList<String>) y.getStringList("WinningClass."+i+".KeepsakeItem.Lore");
							}
							String ego = y.getString("WinningClass."+i+".KeepsakeItem.EnchantmentGlintOverride");
							Boolean enchantmentGlintOverride = ego.equals("null") ? null : (ego.equals("true") ? true : false);
							ksi = new KeepsakeItem(material, amount, displayname, lore, enchantmentGlintOverride);
							if(y.contains("WinningClass."+i+".KeepsakeItem.Enchantment"))
							{
								for(String s : y.getStringList("WinningClass."+i+".KeepsakeItem.Enchantment"))
								{
									String[] sp = s.split(";");
									if(sp.length != 2)
									{
										LLY.log.warning(lottoname+" WinningClass number "+i+" is has a small issue! "+
												"KeepsakeItem Enchantment is skipt: "+s);
										continue;
									}
									ksi.addEnchantment(sp[0].toLowerCase(), Integer.valueOf(sp[1]));
								}
							}
							if(y.contains("WinningClass."+i+".KeepsakeItem.ItemFlag"))
							{
								for(String s : y.getStringList("WinningClass."+i+".KeepsakeItem.ItemFlag"))
								{
									ksi.addItemFlag(ItemFlag.valueOf(s));
								}
							}
						} catch(Exception e)
						{
							LLY.log.warning(lottoname+" WinningClass number "+i+" is has a small issue! "+
									"Keepsake Item was skipt");
							continue;
						}
					}					
					WinningClassSuper wcs = new WinningClassSuper(i, payt, payout, numberMatchToWin, execcmd, ksi, additionalNumberMatchToWin);
					winningClassSuper.add(wcs);
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
				LottoSuper ls = new LottoSuper(lottoname, description, GameType.X_FROM_Y_AND_Z_FROM_U,
						standartPot, maximumPot, amountToAddToThePotIfNoOneIsWinning, costPerTicket, 
						maximalAmountOfTicketWhichCanAPlayerBuy,
						fristNumberToChooseFrom, lastNumberToChooseFrom, amountOfChoosedNumber, 
						drawTime, winningClassSuper, drawOnServer, drawManually, advertising,
						additionalFristNumberToChooseFrom, additionalLastNumberToChooseFrom, additionalAmountOfChoosedNumber);
				LLY.log.info("LottoSuper "+lottoname+" loaded!");
				lottosuper.add(ls);
				LottoSuperTicket lst = new LottoSuperTicket(lottoname);
				lst.setupMysql(LLY.getPlugin().getMysqlSetup(), me.avankziar.lly.general.database.ServerType.ALL);
				LottoSuperDraw lsd = new LottoSuperDraw(lottoname);
				lsd.setupMysql(LLY.getPlugin().getMysqlSetup(), me.avankziar.lly.general.database.ServerType.ALL);
				checkIfDrawIsRegistered(ls);
			} catch(Exception e)
			{
				continue;
			}
		}
	}
	
	private static void checkIfDrawIsRegistered(final LottoSuper ls)
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				LottoSuperDraw lsd = new LottoSuperDraw(
						0, ls.getLotteryName(), false, 0, ls.getStandartPot(),
						new LinkedHashSet<Integer>(), new LinkedHashSet<Integer>());
				if(LLY.getPlugin().getMysqlHandler().exist(lsd, "`was_drawn` = ?", false))
				{
					return;
				}
				if(ls.isDrawManually())
				{
					return;
				}
				LLY.getPlugin().getMysqlHandler().create(lsd);
			}
		}.runTaskAsynchronously(LLY.getPlugin());
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
		final String server = LLY.getPlugin().getServername();
		final LocalDateTime ldt = LocalDateTime.now();
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{	
				for(LottoSuper ls : LotteryHandler.getLottoSuper())
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
					if(ls.isDrawManually())
					{
						continue;
					}
					if(!ls.getDrawOnServer().equals(server))
					{
						continue;
					}
					for(DrawTime dr : ls.getDrawTime())
					{
						if(dr.isNow(ldt))
						{
							drawLotto(ls, new LinkedHashSet<Integer>(), new LinkedHashSet<Integer>(), true);
						}
					}
				}
			}
		}.runTaskAsynchronously(LLY.getPlugin());
	}
	
	private static void advertise(final LottoSuper ls, Advertising ad)
	{
		LottoSuperDraw lsd = LLY.getPlugin().getMysqlHandler().getData(ls.getDrawMysql(), "`was_drawn` = ?", false);
		String[] sa = replacer(ls, lsd, null, null, null, null, false,
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
	
	public static void drawLotto(final LottoSuper ls,
			LinkedHashSet<Integer> manuallyDrawnNumber, LinkedHashSet<Integer> additionalManuallyDrawnNumber,
			Boolean reopen)
	{
		LLY plugin = LLY.getPlugin();
		LottoSuperDraw lsd = plugin.getMysqlHandler().getData(ls.getDrawMysql(), "`was_drawn` = ?", false);
		long drawTime = System.currentTimeMillis();
		if(lsd == null)
		{
			LLY.log.warning("Attention! The MysqlObject of LottoSuperDraw is missing! Draw of "
					+ls.getLotteryName()+" canceled! Open a LottoSuperDraw with a ingame cmd!");
			return;
		}
		//Lottery Draw
		LinkedHashSet<Integer> drawnNumber = manuallyDrawnNumber;
		if(manuallyDrawnNumber.size() <= 0)
		{
			drawnNumber = drawLotteryNumber(ls.getFirstNumberToChooseFrom(),
					ls.getLastNumberToChooseFrom(), ls.getAmountOfChoosedNumber());
		} else
		{
			if(drawnNumber.size() < ls.getAmountOfChoosedNumber())
			{
				while(drawnNumber.size() < ls.getAmountOfChoosedNumber())
				{
					Random r = new Random();
					int i = r.nextInt(ls.getLastNumberToChooseFrom()) + ls.getFirstNumberToChooseFrom();
					drawnNumber.add(i);
				}
			}
			//Sorting DrawnNumber
			drawnNumber = sortDrawnNumber(drawnNumber);
		}
		LinkedHashSet<Integer> addtitionalDrawnNumber = additionalManuallyDrawnNumber;
		if(additionalManuallyDrawnNumber.size() <= 0)
		{
			addtitionalDrawnNumber = drawLotteryNumber(ls.getAdditionalFirstNumberToChooseFrom(),
					ls.getAdditionalLastNumberToChooseFrom(), ls.getAdditionalAmountOfChoosenNumber());
		} else
		{
			if(addtitionalDrawnNumber.size() < ls.getAdditionalAmountOfChoosenNumber())
			{
				while(addtitionalDrawnNumber.size() < ls.getAdditionalAmountOfChoosenNumber())
				{
					Random r = new Random();
					int i = r.nextInt(ls.getAdditionalLastNumberToChooseFrom()) + ls.getAdditionalFirstNumberToChooseFrom();
					addtitionalDrawnNumber.add(i);
				}
			}
			//Sorting DrawnNumber
			addtitionalDrawnNumber = sortDrawnNumber(addtitionalDrawnNumber);
		}
		//Update Object
		lsd.setChoosenNumbers(drawnNumber);
		lsd.setAdditionalChoosenNumbers(addtitionalDrawnNumber);
		lsd.setDrawTime(drawTime);
		lsd.setWasDrawn(true);
		//Lotto Ticket call
		ArrayList<LottoSuperTicket> lstA = plugin.getMysqlHandler().getFullList(
				ls.getTicketMysql(), "`id` ASC", "`draw_id` = ?", lsd.getId());
		if(lstA.isEmpty())
		{
			//No Tickets was bought
			List<String> msgl = replacer(ls, lsd, null, null, null, null, false,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("LottoSuper.Draw.NoTicketAreBought"));
			MessageHandler.sendMessage(msgl.toArray(new String[msgl.size()]));
			double nextpot = lsd.getActualPot();
			if(nextpot < ls.getStandartPot())
			{
				nextpot = ls.getStandartPot();
			}
			nextpot += ls.getAmountToAddToThePotIfNoOneIsWinning();
			if(nextpot > ls.getMaximumPot())
			{
				nextpot = ls.getMaximumPot();
			}
			plugin.getMysqlHandler().updateData(lsd, "`id` = ?", lsd.getId());
			if(reopen || !ls.isDrawManually())
			{
				LottoSuperDraw cldNext = new LottoSuperDraw(0, ls.getLotteryName(), false, 0, nextpot,
						new LinkedHashSet<>(), new LinkedHashSet<>());
				plugin.getMysqlHandler().create(cldNext);
			}
			return;
		}
		//Get Next ClassicLottoDraw ID
		int lsdNextId = plugin.getMysqlHandler().lastID(lsd)+1;
		//Adding all WinningClass to a payout object
		LinkedHashMap<Integer, LottoSuperPayout> lspA = new LinkedHashMap<>();
		//Create all WinningClasss
		int highestWC = 1;
		int lowestWC = 1;
		for(WinningClassSuper wcs : ls.getWinningClassSuper())
		{
			LLY.log.info("WinningClassSuper: "+wcs.getWinningClassLevel());//REMOVEME
			double payout = 0.0;
			switch(wcs.getPayoutType())
			{
			case LUMP_SUM: payout = wcs.getAmount(); break;
			case PERCENTAGE: payout = lsd.getActualPot() * wcs.getAmount() / 100; break;
			}
			lspA.put(wcs.getWinningClassLevel(), new LottoSuperPayout(wcs.getWinningClassLevel(),
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
		lspA.put(lowestWC+1, new LottoSuperPayout(lowestWC+1, PayoutType.LUMP_SUM, 0, new HashSet<UUID>(), 0, 0, 0));
		//Lotto Ticket Evaluation
		String repeatCategory = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.RepeatTicket.Category")
				.replace("%lotteryname%", ls.getLotteryName());
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
			lspA.put(index, lsp);
			if(lst.shouldRepeate())
			{
				//If ticket should repeat, create new one and update the old one.
				double price = ls.getCostPerTicket();
				if(EconomyHandler.hasBalance(lst.getLotteryPlayer(), price))
				{
					if(price > 0.0)
					{
						EconomyHandler.withdraw(lst.getLotteryPlayer(), price, repeatCategory, repeatComment);
					}
					LottoSuperTicket lstRepeat = new LottoSuperTicket(0, 
							lsdNextId, ls.getLotteryName(), lst.getLotteryPlayer(),
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
			globalMsg = replacer(ls, lsd, null, lspA, payout, String.join(", ", jackpotWinners), hlsp.getUUIDs().size() > 0,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("LottoSuper.Draw.JackpotWasBreached"));
		} else
		{
			double payout = lsd.getActualPot();
			globalMsg = replacer(ls, lsd, null, lspA, payout, String.join(", ", jackpotWinners), hlsp.getUUIDs().size() > 0,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("LottoSuper.Draw.JackpotIsUntouched"));
		}
		//Send all OnlinePlayers the normal message
		MessageHandler.sendMessage(globalMsg.toArray(new String[globalMsg.size()]));
		//Put all Player in a hashset to check if there won to not send a "you lost" msg.
		//Therefor reverse the sorting of the clp.
		ArrayList<LottoSuperPayout> lspAl = new ArrayList<>();
		lspA.entrySet().forEach(x -> lspAl.add(x.getValue()));
		lspAl.sort(Comparator.comparingInt(LottoSuperPayout::getWinningClassLevel).reversed());
		//Give Players a the price and send a message
		double nextpot = 0.0;
		for(LottoSuperPayout lsp : lspAl)
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
					nowinmsg = replacer(ls, lsd, lst, lspA, null, null, hlsp.getUUIDs().size() > 0,
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
					.replace("%lotteryname%", ls.getLotteryName())
					.replace("%level%", String.valueOf(lv));
			WinningClassSuper wcs = ls.getWinningClassSuper().stream()
					.filter(x -> x.getWinningClassLevel() == lv)
					.findAny().orElse(null);
			for(LottoSuperTicket lst : payoutToTicket.get(lv))
			{
				ArrayList<String> winmsg = replacer(ls, lsd, lst, lspA, payout, null, hlsp.getUUIDs().size() > 0,
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
								replacer(ls, lsd, lst, lspA, null, null, false,
										wcs.getKeepsakeItem().getDisplayname());
						ArrayList<String> lore = replacer(ls, lsd, lst, lspA, null, null, false,
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
			nextpot = ls.getStandartPot();
		} else
		{
			if(nextpot < ls.getStandartPot())
			{
				nextpot = ls.getStandartPot();
			}
			nextpot += ls.getAmountToAddToThePotIfNoOneIsWinning();
			if(nextpot > ls.getMaximumPot())
			{
				nextpot = ls.getMaximumPot();
			}
		}
		//Create next lotto draw.
		if(reopen || !ls.isDrawManually())
		{
			LottoSuperDraw lsdNext = new LottoSuperDraw(0, ls.getLotteryName(), false, 0, nextpot,
					new LinkedHashSet<>(), new LinkedHashSet<>());
			plugin.getMysqlHandler().create(lsdNext);
		}
	}
	
	public static LinkedHashSet<Integer> drawLotteryNumber(int firstNumber, int lastNumber, int amountOfNumber)
	{
		LinkedHashSet<Integer> set = new LinkedHashSet<>();
		while(set.size() < amountOfNumber)
		{
			Random r = new Random();
			int i = r.nextInt(lastNumber) + firstNumber;
			set.add(i);
		}
		return set;
	}
	
	public static LinkedHashSet<Integer> sortDrawnNumber(LinkedHashSet<Integer> drawnNumber)
	{
		ArrayList<Integer> sort = new ArrayList<>();
		sort.addAll(drawnNumber);
		Collections.sort(sort);
		LinkedHashSet<Integer> set = new LinkedHashSet<>();
		set.addAll(sort);
		return set;
	}
	
	/**
	 * Replacer for message in the drawing of the lottery.
	 */
	public static ArrayList<String> replacer(LottoSuper ls, LottoSuperDraw lsd, LottoSuperTicket lst,
			LinkedHashMap<Integer, LottoSuperPayout> lspA, Double payout, String jackpotwinners, boolean wasJackpotBreached,
			ArrayList<String> list)
	{
		ArrayList<String> li = new ArrayList<>();
		for(String s : list)
		{
			String r = replacer(ls, lsd, lst, lspA, payout, jackpotwinners, wasJackpotBreached, s);
			li.add(r);
		}
		return li;
	}
	
	public static String replacer(LottoSuper ls, LottoSuperDraw lsd, LottoSuperTicket lst,
			LinkedHashMap<Integer, LottoSuperPayout> lspA, Double payout, String jackpotwinners, boolean wasJackpotBreached,
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
					.replace("%amountofchoosennumber%", String.valueOf(ls.getAmountOfChoosedNumber()))
					.replace("%additionalamountofchoosennumber%", String.valueOf(ls.getAdditionalAmountOfChoosenNumber()))
					.replace("%amounttoaddpot%", EconomyHandler.format(ls.getAmountToAddToThePotIfNoOneIsWinning()))
					.replace("%costperticket%", EconomyHandler.format(ls.getCostPerTicket()))
					.replace("%firstnumbertochoosefrom%", String.valueOf(ls.getFirstNumberToChooseFrom()))
					.replace("%lastnumbertochoosefrom%", String.valueOf(ls.getLastNumberToChooseFrom()))
					.replace("%additionalfirstnumbertochoosefrom%", String.valueOf(ls.getAdditionalFirstNumberToChooseFrom()))
					.replace("%additionallastnumbertochoosefrom%", String.valueOf(ls.getAdditionalLastNumberToChooseFrom()))
					.replace("%maximumpot%", EconomyHandler.format(ls.getMaximumPot()))
					.replace("%standartpot%", EconomyHandler.format(ls.getStandartPot()))
					.replace("%winningchance%", String.valueOf(ls.getWinningChance().longValue()));
			for(WinningClassSuper wcs : ls.getWinningClassSuper())
			{
				s = s.replace("%wc"+wcs.getWinningClassLevel()+"level%", String.valueOf(wcs.getWinningClassLevel())
						.replace("%wc"+wcs.getWinningClassLevel()+"percentage%", String.valueOf(wcs.getAmount()))
						.replace("%wc"+wcs.getWinningClassLevel()+"lottopayout%", 
								EconomyHandler.format(ls.getStandartPot() * wcs.getAmount() / 100)));
			}				
			if(!ls.isDrawManually())
			{
				DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
						LLY.getPlugin().getYamlHandler().getConfig().getString("DateTimeFormatter"));
				s = s.replace("%nextdraw%", DrawTime.getNextTime(ls.getDrawTime(), null).format(dtf));
			} else
			{
				s = s.replace("%nextdraw%", LLY.getPlugin().getYamlHandler().getLang().getString("Replacer.NoDraw"));
			}
		} else
		{
			s = s.replace("%lotteryname%", "/")
					.replace("%description%", "/")
					.replace("%amountofchoosennumber%", "/")
					.replace("%additionalamountofchoosennumber%", "/")
					.replace("%amounttoaddpot%", "/")
					.replace("%costperticket%", "/")
					.replace("%fristnumbertochoosefrom%", "/")
					.replace("%lastnumbertochoosefrom%", "/")
					.replace("%additionalfirstnumbertochoosefrom%", "/")
					.replace("%additionallastnumbertochoosefrom%", "/")
					.replace("%maximumpot%", "/")
					.replace("%standartpot%", "/")
					.replace("%winningchance%", "/");
		}
		if(lsd != null)
		{
			ArrayList<String> ccn = new ArrayList<>();
			for(Iterator<Integer> iter = lsd.getChoosenNumbers().iterator(); iter.hasNext();)
			{
				ccn.add(String.valueOf(iter.next()));
			}
			ArrayList<String> ccna = new ArrayList<>();
			for(Iterator<Integer> iter = lsd.getAdditionalChoosenNumbers().iterator(); iter.hasNext();)
			{
				ccna.add(String.valueOf(iter.next()));
			}
			s = s.replace("%lotteryname%", lsd.getLotteryName())
					.replace("%drawid%", String.valueOf(lsd.getId()))
					.replace("%drawtime%", lsd.wasDrawn() ? TimeHandler.getDate(lsd.getDrawTime()) 
							: LLY.getPlugin().getYamlHandler().getLang().getString("WasntDrawn"))
					.replace("%actualpot%", EconomyHandler.format(lsd.getActualPot()))
					.replace("%drawnnumber%", "["+String.join(", ", ccn)+"]")
					.replace("%additionaldrawnnumber%", "["+String.join(", ", ccna)+"]");
			if(ls != null)
			{
				WinningClassSuper highest = null;
				for(WinningClassSuper wcs : ls.getWinningClassSuper())
				{
					if(wcs.getWinningClassLevel() == 1)
					{
						highest = wcs;
						break;
					}
				}
				if(highest != null)
				{
					s = s.replace("%highestwinningclass%", EconomyHandler.format(lsd.getActualPot() * highest.getAmount() / 100));
				}
			}		
		} else
		{
			s = s.replace("%lotteryname%", "/")
					.replace("%drawid%", "/")
					.replace("%drawtime%", "/")
					.replace("%actualpot%", "/")
					.replace("%drawnnumber%", "/")
					.replace("%highestwinningclass%", "/")
					.replace("%additionaldrawnnumber%", "/");
		}
		if(lst != null)
		{
			if(lsd != null)
			{
				ArrayList<String> ccn = new ArrayList<>();
				int matches = 0;
				for(Iterator<Integer> iter = lst.getChoosenNumbers().iterator(); iter.hasNext();)
				{
					int cn = iter.next();
					ccn.add(matchChoosenNumber(lsd.getChoosenNumbers(), cn));
					if(lsd.getChoosenNumbers().contains(cn))
					{
						matches++;
					}
				}
				ArrayList<String> ccna = new ArrayList<>();
				int matchesa = 0;
				for(Iterator<Integer> iter = lst.getAdditionalChoosenNumbers().iterator(); iter.hasNext();)
				{
					int cn = iter.next();
					ccna.add(matchChoosenNumber(lsd.getChoosenNumbers(), cn));
					if(lsd.getAdditionalChoosenNumbers().contains(cn))
					{
						matchesa++;
					}
				}
				s = s.replace("%matchchoosennumber%", "["+String.join(", ", ccn)+"]")
						.replace("%matchchoosennumberamount%", String.valueOf(matches))
						.replace("%matchadditionalchoosennumber%", "["+String.join(", ", ccna)+"]")
						.replace("%matchadditionalchoosennumberamount%", String.valueOf(matchesa));
			}
			s = s.replace("%lotteryname%", lst.getLotteryName())
					.replace("%ticketid%", String.valueOf(lst.getID()))
					.replace("%ticketplayer%", Utility.convertUUIDToName(lst.getLotteryPlayer().toString()));
		} else
		{
			s = s.replace("%lotteryname%", "/")
					.replace("%ticketid%", "/")
					.replace("%ticketplayer%", "/")
					.replace("%matchchoosennumber%", "/")
					.replace("%matchchoosennumberamount%", "/")
					.replace("%matchadditionalchoosennumber%", "/")
					.replace("%matchadditionalchoosennumberamount%", "/");
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
			if(ls != null)
			{
				if(wasJackpotBreached)
				{
					nextpot = ls.getStandartPot();
				} else
				{
					if(nextpot < ls.getStandartPot())
					{
						nextpot = ls.getStandartPot();
					}
					nextpot += ls.getAmountToAddToThePotIfNoOneIsWinning();
				}
				s = s.replace("%nextpot%", EconomyHandler.format(nextpot));
			}
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
	
	public static LottoSuperPayout getPayout(LinkedHashMap<Integer, LottoSuperPayout> list, int level)
	{
		return list.get(level);
	}
	
	public static int getIndexPayout(LinkedHashMap<Integer, LottoSuperPayout> list, 
	        int matchNumber, int additionMatchNumber, int originalAdditionMatchNumber) 
	{
	    for (LottoSuperPayout lsp : list.values()) 
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