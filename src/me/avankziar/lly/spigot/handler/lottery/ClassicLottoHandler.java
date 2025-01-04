package me.avankziar.lly.spigot.handler.lottery;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.assistance.TimeHandler;
import me.avankziar.lly.general.assistance.Utility;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.database.ServerType;
import me.avankziar.lly.general.objects.Advertising;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.PlayerData;
import me.avankziar.lly.general.objects.WinningClass;
import me.avankziar.lly.general.objects.WinningClass.PayoutType;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.general.objects.lottery.payout.ClassicLottoPayout;
import me.avankziar.lly.general.objects.lottery.ticket.ClassicLottoTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;

public class ClassicLottoHandler 
{
	protected static ArrayList<ClassicLotto> classicLotto = new ArrayList<>();
	
	public static void initalizedYamls()
	{
		for(YamlDocument y : LLY.getPlugin().getYamlHandler().getClassicLotto())
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
				LinkedHashSet<WinningClass> WinningClass = new LinkedHashSet<>();
				boolean check = false;
				for(int i = 1; i <= amountOfChoosedNumber; i++)
				{
					if(!y.contains("WinningClass."+i+".Payout"))
					{
						check = true;
						LLY.log.warning(lottoname+" WinningClass number "+i+" is missing! "+
								lottoname+" will not be registered!");
						break;
					}
					PayoutType payt = PayoutType.valueOf(y.getString("WinningClass."+i+".PayoutType", PayoutType.LUMP_SUM.toString()));
					double payout = y.getDouble("WinningClass."+i+".Payout");
					int numberMatchToWin = y.getInt("WinningClass."+i+".NumberMatchToWin");
					WinningClass wc = new WinningClass(i, payt, payout, numberMatchToWin);
					WinningClass.add(wc);
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
				ClassicLotto cl = new ClassicLotto(lottoname, description, GameType.X_FROM_Y,
						standartPot, maximumPot, amountToAddToThePotIfNoOneIsWinning, costPerTicket, 
						maximalAmountOfTicketWhichCanAPlayerBuy,
						fristNumberToChooseFrom, lastNumberToChooseFrom, amountOfChoosedNumber, 
						drawTime, WinningClass, drawOnServer, drawManually, advertising);
				LLY.log.info("ClassicLotto "+lottoname+" loaded!");
				classicLotto.add(cl);
				ClassicLottoTicket clt = new ClassicLottoTicket(lottoname);
				clt.setupMysql(LLY.getPlugin().getMysqlSetup(), ServerType.ALL);
				ClassicLottoDraw cld = new ClassicLottoDraw(lottoname);
				cld.setupMysql(LLY.getPlugin().getMysqlSetup(), ServerType.ALL);
				checkIfDrawIsRegistered(cl);
			} catch(Exception e)
			{
				continue;
			}
		}
	}
	
	private static void checkIfDrawIsRegistered(final ClassicLotto cl)
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				ClassicLottoDraw cld = new ClassicLottoDraw(
						0, cl.getLotteryName(), false, 0, cl.getStandartPot(), new LinkedHashSet<Integer>());
				if(LLY.getPlugin().getMysqlHandler().exist(cld, "`was_drawn` = ?", false))
				{
					return;
				}
				if(cl.isDrawManually())
				{
					return;
				}
				LLY.getPlugin().getMysqlHandler().create(cld);
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
				LLY.log.info("Now: "+DrawTime.getNow()); //REMOVEME
				for(ClassicLotto cl : LotteryHandler.getClassicLotto())
				{
					LLY.log.info(" Cl: "+cl.getLotteryName()); //REMOVEME
					for(Advertising ad : cl.getAdvertising())
					{
						if(!ad.isActive())
						{
							continue;
						}
						LLY.log.info("  Advertising is active."); //REMOVEME
						for(DrawTime dr : ad.getDrawTime())
						{
							if(dr.isNow(ldt))
							{
								LLY.log.info("  Ad is now! >>> "+dr.toString()); //REMOVEME
								advertise(cl, ad);
							}
						}
					}
					if(cl.isDrawManually())
					{
						LLY.log.info("  isDrawManually"); //REMOVEME
						continue;
					}
					if(!cl.getDrawOnServer().equals(server))
					{
						LLY.log.info("  DrawOnServer isnt "+cl.getDrawOnServer() + " : "+server); //REMOVEME
						continue;
					}
					for(DrawTime dr : cl.getDrawTime())
					{
						if(dr.isNow(ldt))
						{
							LLY.log.info("  DrawLott is now! >>> "+dr.toString()); //REMOVEME
							drawLotto(cl, new LinkedHashSet<Integer>(), true);
						}
					}
				}
			}
		}.runTaskAsynchronously(LLY.getPlugin());
	}
	
	private static void advertise(final ClassicLotto cl, Advertising ad)
	{
		ClassicLottoDraw cld = LLY.getPlugin().getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		String[] sa = replacer(cl, cld, null, null, null, null, false,
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
	
	public static void drawLotto(final ClassicLotto cl, LinkedHashSet<Integer> manuallyDrawnNumber, Boolean reopen)
	{
		LLY plugin = LLY.getPlugin();
		ClassicLottoDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		long drawTime = System.currentTimeMillis();
		if(cld == null)
		{
			LLY.log.warning("Attention! The MysqlObject of ClassicLottoDraw is missing! Draw of "
					+cl.getLotteryName()+" canceled! Open a ClassicLottoDraw with a ingame cmd!");
			return;
		}
		//Lottery Draw
		LinkedHashSet<Integer> drawnNumber = manuallyDrawnNumber;
		if(manuallyDrawnNumber.size() <= 0)
		{
			drawnNumber = drawLotteryNumber(cl.getFirstNumberToChooseFrom(),
					cl.getLastNumberToChooseFrom(), cl.getAmountOfChoosedNumber());
		} else
		{
			if(drawnNumber.size() < cl.getAmountOfChoosedNumber())
			{
				while(drawnNumber.size() < cl.getAmountOfChoosedNumber())
				{
					Random r = new Random();
					int i = r.nextInt(cl.getLastNumberToChooseFrom()) + cl.getFirstNumberToChooseFrom();
					drawnNumber.add(i);
				}
			}
			//Sorting DrawnNumber
			drawnNumber = sortDrawnNumber(drawnNumber);
		}
		//Update Object
		cld.setChoosenNumbers(drawnNumber);
		cld.setDrawTime(drawTime);
		cld.setWasDrawn(true);
		//Lotto Ticket call
		ArrayList<ClassicLottoTicket> cltA = plugin.getMysqlHandler().getFullList(
				cl.getTicketMysql(), "`id` ASC", "`draw_id` = ?", cld.getId());
		if(cltA.isEmpty())
		{
			//No Tickets was bought
			List<String> msgl = replacer(cl, cld, null, null, null, null, false,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("ClassicLotto.Draw.NoTicketAreBought"));
			MessageHandler.sendMessage(msgl.toArray(new String[msgl.size()]));
			double nextpot = cld.getActualPot();
			if(nextpot < cl.getStandartPot())
			{
				nextpot = cl.getStandartPot();
			}
			nextpot += cl.getAmountToAddToThePotIfNoOneIsWinning();
			if(nextpot > cl.getMaximumPot())
			{
				nextpot = cl.getMaximumPot();
			}
			plugin.getMysqlHandler().updateData(cld, "`id` = ?", cld.getId());
			if(reopen || !cl.isDrawManually())
			{
				ClassicLottoDraw cldNext = new ClassicLottoDraw(0, cl.getLotteryName(), false, 0, nextpot, new LinkedHashSet<>());
				plugin.getMysqlHandler().create(cldNext);
			}
			return;
		}
		//Get Next ClassicLottoDraw ID
		int cldNextId = plugin.getMysqlHandler().lastID(cld)+1;
		//Adding all WinningClass to a payout object
		ArrayList<ClassicLottoPayout> clpA = new ArrayList<>();
		//Create all WinningClasss
		int highestWC = 1;
		int lowestWC = 1;
		for(WinningClass wc : cl.getWinningClass())
		{
			double payout = 0.0;
			switch(wc.getPayoutType())
			{
			case LUMP_SUM: payout = wc.getAmount(); break;
			case PERCENTAGE: payout = cld.getActualPot() * wc.getAmount() / 100; break;
			}
			clpA.add(new ClassicLottoPayout(wc.getWinningClassLevel(),
					wc.getPayoutType(),
					payout,
					new HashSet<UUID>(), 0, wc.getNumberMatchToWin()));
			if(lowestWC < wc.getWinningClassLevel())
			{
				lowestWC = wc.getWinningClassLevel();
			}
		}
		//Added WinningClass for all player that lost, the int is the highest level
		clpA.add(new ClassicLottoPayout(lowestWC+1, PayoutType.LUMP_SUM, 0, new HashSet<UUID>(), 0, 0));
		//Lotto Ticket Evaluation
		String repeatCategory = plugin.getYamlHandler().getLang().getString("ClassicLotto.Draw.RepeatTicket.Category")
				.replace("%lotteryname%", cl.getLotteryName());
		String repeatComment = plugin.getYamlHandler().getLang().getString("ClassicLotto.Draw.RepeatTicket.Comment");
		LinkedHashMap<Integer, ArrayList<ClassicLottoTicket>> payoutToTicket = new LinkedHashMap<>();
		for(ClassicLottoTicket clt : cltA)
		{
			//Amount of number which match
			int matchNumber = matchChoosenNumber(drawnNumber, clt.getChoosenNumbers());
			//Payout Index which is WinningClass
			int index = getIndexPayout(clpA, matchNumber);
			//Getting the PayoutObject
			ClassicLottoPayout clp = getPayout(clpA, index);
			clp.getUUIDs().add(clt.getLotteryPlayer());
			clp.setWinnersAmount(clp.getWinnersAmount()+1);
			clpA.set(index, clp);
			if(clt.shouldRepeate())
			{
				//If ticket should repeat, create new one and update the old one.
				double price = cl.getCostPerTicket();
				if(EconomyHandler.hasBalance(clt.getLotteryPlayer(), price))
				{
					if(price > 0.0)
					{
						EconomyHandler.withdraw(clt.getLotteryPlayer(), price, repeatCategory, repeatComment);
					}
					ClassicLottoTicket cltRepeat = new ClassicLottoTicket(0, cldNextId, cl.getLotteryName(), clt.getLotteryPlayer(),
							true, 0, 0, clt.getChoosenNumbers());
					plugin.getMysqlHandler().create(cltRepeat);
				}
				clt.setShouldRepeate(false);
				plugin.getMysqlHandler().updateData(clt, "`id` = ?", clt.getID());
			}
			//Adding all Tickets to a list which is put in a LinkedHashMap sorted after CLP Level.
			ArrayList<ClassicLottoTicket> cltATPo = new ArrayList<>();
			if(payoutToTicket.containsKey(clp.getWinningClassLevel()))
			{
				cltATPo = payoutToTicket.get(clp.getWinningClassLevel());
			}
			cltATPo.add(clt);
			payoutToTicket.put(clp.getWinningClassLevel(), cltATPo);
		}
		//Highest WinningClass scored?
		ClassicLottoPayout hclp = getPayout(clpA, highestWC);
		ArrayList<String> jackpotWinners = new ArrayList<>();
		ArrayList<String> globalMsg = new ArrayList<>();
		if(hclp.getUUIDs().size() > 0)
		{
			hclp.getUUIDs().stream().forEach(x -> jackpotWinners.add(Utility.convertUUIDToName(x.toString())));
			double payout = cld.getActualPot();
			globalMsg = replacer(cl, cld, null, clpA, payout, String.join(", ", jackpotWinners), hclp.getUUIDs().size() > 0,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("ClassicLotto.Draw.JackpotWasBreached"));
		} else
		{
			double payout = cld.getActualPot();
			globalMsg = replacer(cl, cld, null, clpA, payout, String.join(", ", jackpotWinners), hclp.getUUIDs().size() > 0,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("ClassicLotto.Draw.JackpotIsUntouched"));
		}
		//Send all OnlinePlayers the normal message
		MessageHandler.sendMessage(globalMsg.toArray(new String[globalMsg.size()]));
		//Put all Player in a hashset to check if there won to not send a "you lost" msg.
		//Therefor reverse the sorting of the clp.
		clpA.sort(Comparator.comparingInt(ClassicLottoPayout::getWinningClassLevel).reversed());
		//Give Players a the price and send a message
		double nextpot = 0.0;
		for(ClassicLottoPayout clp : clpA)
		{
			int lv = clp.getWinningClassLevel();
			if(clp.getUUIDs().size() == 0)
			{
				//No one won in this payout/WinningClass. Added in Next pot if the payouttype is percentage.
				if(clp.getPayoutType() == PayoutType.PERCENTAGE)
				{
					nextpot += clp.getPayout();
				}
				continue;
			}
			if(clp.getPayout() <= 0.0)
			{
				//CLP Lv 0 for all player which has Lost AND where other ticket of the same player didnt also not win.
				for(ClassicLottoTicket clt : payoutToTicket.get(lv))
				{
					ArrayList<String> nowinmsg = new ArrayList<>();
					nowinmsg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Draw.NotWon"));
					nowinmsg = replacer(cl, cld, clt, clpA, null, null, hclp.getUUIDs().size() > 0,
							nowinmsg);
					MessageHandler.sendMessage(clt.getLotteryPlayer(), nowinmsg.toArray(new String[nowinmsg.size()]));
					clt.setWinningClassLevel(lv);
					plugin.getMysqlHandler().updateData(clt, "`id` = ?", clt.getID());
				}
				continue;
			}
			double payout = clp.getFinalPayoutPerUUID();
			String wincategory = plugin.getYamlHandler().getLang().getString("ClassicLotto.Draw.Win.Category");
			String wincomment = plugin.getYamlHandler().getLang().getString("ClassicLotto.Draw.Win.Comment")
					.replace("%lotteryname%", cl.getLotteryName())
					.replace("%level%", String.valueOf(lv));
			for(ClassicLottoTicket clt : payoutToTicket.get(lv))
			{
				ArrayList<String> winmsg = replacer(cl, cld, clt, clpA, payout, null, hclp.getUUIDs().size() > 0,
						(ArrayList<String>) plugin.getYamlHandler().getLang().getStringList("ClassicLotto.Draw.Won"));
				EconomyHandler.deposit(clt.getLotteryPlayer(), payout, 
						wincategory, 
						wincomment);
				MessageHandler.sendMessage(clt.getLotteryPlayer(), winmsg.toArray(new String[winmsg.size()]));
				clt.setWinningClassLevel(lv);
				clt.setWinningPrize(payout);
				plugin.getMysqlHandler().updateData(clt, "`id` = ?", clt.getID());
			}
		}
		plugin.getMysqlHandler().updateData(cld, "`id` = ?", cld.getId());
		//Determine next pot
		if(hclp.getUUIDs().size() > 0)
		{
			nextpot = cl.getStandartPot();
		} else
		{
			if(nextpot < cl.getStandartPot())
			{
				nextpot = cl.getStandartPot();
			}
			nextpot += cl.getAmountToAddToThePotIfNoOneIsWinning();
			if(nextpot > cl.getMaximumPot())
			{
				nextpot = cl.getMaximumPot();
			}
		}
		//Create next lotto draw.
		if(reopen || !cl.isDrawManually())
		{
			ClassicLottoDraw cldNext = new ClassicLottoDraw(0, cl.getLotteryName(), false, 0, nextpot, new LinkedHashSet<>());
			plugin.getMysqlHandler().create(cldNext);
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
	public static ArrayList<String> replacer(ClassicLotto cl, ClassicLottoDraw cld, ClassicLottoTicket clt,
			ArrayList<ClassicLottoPayout> clpA, Double payout, String jackpotwinners, boolean wasJackpotBreached,
			ArrayList<String> list)
	{
		ArrayList<String> li = new ArrayList<>();
		for(String s : list)
		{
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
			if(s.contains("%classiclottocmd%"))
			{
				s = s.replace("%classiclottocmd%", CommandSuggest.getCmdString(CommandSuggest.Type.CLASSICLOTTO));
			}
			if(s.contains("%classiclottobet%"))
			{
				s = s.replace("%classiclottobet%", CommandSuggest.getCmdString(CommandSuggest.Type.CLASSICLOTTO_PLAY));
			}
			if(cl != null)
			{
				s = s.replace("%lotteryname%", cl.getLotteryName())
						.replace("%description%", cl.getDescription())
						.replace("%amountofchoosennumber%", String.valueOf(cl.getAmountOfChoosedNumber()))
						.replace("%amounttoaddpot%", EconomyHandler.format(cl.getAmountToAddToThePotIfNoOneIsWinning()))
						.replace("%costperticket%", EconomyHandler.format(cl.getCostPerTicket()))
						.replace("%fristnumbertochoosefrom%", String.valueOf(cl.getFirstNumberToChooseFrom()))
						.replace("%lastnumbertochoosefrom%", String.valueOf(cl.getLastNumberToChooseFrom()))
						.replace("%maximumpot%", EconomyHandler.format(cl.getMaximumPot()))
						.replace("%standartpot%", EconomyHandler.format(cl.getStandartPot()))
						.replace("%winningchance%", String.valueOf(cl.getWinningChance().longValue()));
				for(WinningClass wc : cl.getWinningClass())
				{
					s = s.replace("%wc"+wc.getWinningClassLevel()+"level%", String.valueOf(wc.getWinningClassLevel())
							.replace("%wc"+wc.getWinningClassLevel()+"percentage%", String.valueOf(wc.getAmount()))
							.replace("%wc"+wc.getWinningClassLevel()+"lottopayout%", 
									EconomyHandler.format(cl.getStandartPot() * wc.getAmount() / 100)));
				}				
				if(!cl.isDrawManually())
				{
					DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
							LLY.getPlugin().getYamlHandler().getConfig().getString("DateTimeFormatter"));
					s = s.replace("%nextdraw%", DrawTime.getNextTime(cl.getDrawTime(), null).format(dtf));
				} else
				{
					s = s.replace("%nextdraw%", LLY.getPlugin().getYamlHandler().getLang().getString("Replacer.NoDraw"));
				}
			} else
			{
				s = s.replace("%lotteryname%", "/")
						.replace("%description%", "/")
						.replace("%amountofchoosennumber%", "/")
						.replace("%amounttoaddpot%", "/")
						.replace("%costperticket%", "/")
						.replace("%fristnumbertochoosefrom%", "/")
						.replace("%lastnumbertochoosefrom%", "/")
						.replace("%maximumpot%", "/")
						.replace("%standartpot%", "/")
						.replace("%winningchance%", "/");
			}
			if(cld != null)
			{
				ArrayList<String> ccn = new ArrayList<>();
				for(Iterator<Integer> iter = cld.getChoosenNumbers().iterator(); iter.hasNext();)
				{
					int cn = iter.next();
					ccn.add(String.valueOf(cn));
				}
				s = s.replace("%lotteryname%", cld.getLotteryName())
						.replace("%drawid%", String.valueOf(cld.getId()))
						.replace("%drawtime%", cld.wasDrawn() ? TimeHandler.getDate(cld.getDrawTime()) 
								: LLY.getPlugin().getYamlHandler().getLang().getString("WasntDrawn"))
						.replace("%actualpot%", EconomyHandler.format(cld.getActualPot()))
						.replace("%drawnnumber%", "["+String.join(", ", ccn)+"]");
				if(cl != null)
				{
					WinningClass highest = null;
					for(WinningClass wc : cl.getWinningClass())
					{
						if(wc.getWinningClassLevel() == 1)
						{
							highest = wc;
							break;
						}
					}
					if(highest != null)
					{
						s = s.replace("%highestwinningclass%", EconomyHandler.format(cld.getActualPot() * highest.getAmount() / 100));
					}
				}		
			} else
			{
				s = s.replace("%lotteryname%", "/")
						.replace("%drawid%", "/")
						.replace("%drawtime%", "/")
						.replace("%actualpot%", "/")
						.replace("%drawnnumber%", "/")
						.replace("%highestwinningclass%", "/");
			}
			if(clt != null)
			{
				if(cld != null)
				{
					ArrayList<String> ccn = new ArrayList<>();
					int matches = 0;
					for(Iterator<Integer> iter = clt.getChoosenNumbers().iterator(); iter.hasNext();)
					{
						int cn = iter.next();
						ccn.add(matchChoosenNumber(cld.getChoosenNumbers(), cn));
						if(cld.getChoosenNumbers().contains(cn))
						{
							matches++;
						}
					}
					s = s.replace("%matchchoosennumber%", "["+String.join(", ", ccn)+"]")
							.replace("%matchchoosennumberamount%", String.valueOf(matches));
				}
				s = s.replace("%lotteryname%", clt.getLotteryName())
						.replace("%ticketid%", String.valueOf(clt.getID()))
						.replace("%ticketplayer%", Utility.convertUUIDToName(clt.getLotteryPlayer().toString()));
			} else
			{
				s = s.replace("%lotteryname%", "/")
						.replace("%ticketid%", "/")
						.replace("%ticketplayer%", "/")
						.replace("%matchchoosennumber%", "/")
						.replace("%matchchoosennumberamount%", "/");
			}
			if(clpA != null)
			{
				StringBuilder wcwinneramount = new StringBuilder();
				double nextpot = 0.0;
				for(int i = 0; i < clpA.size(); i++)
				{
					ClassicLottoPayout clp = clpA.get(i);
					wcwinneramount.append(LLY.getPlugin().getYamlHandler().getLang()
							.getString("ClassicLotto.Draw.WinningClassReplacer")
							.replace("%level%", String.valueOf(clp.getWinningClassLevel()))
							.replace("%winneramount%", String.valueOf(clp.getWinnersAmount())));
					if(i+1 < clpA.size())
					{
						wcwinneramount.append(LLY.getPlugin().getYamlHandler().getLang()
								.getString("ClassicLotto.Draw.WinningClassReplacerSeperator"));
					}
					if(clp.getUUIDs().size() <= 0)
					{
						nextpot += clp.getPayout();
					}
				}
				s = s.replace("%winningclasswinneramount%", wcwinneramount.toString());
				if(cl != null)
				{
					if(wasJackpotBreached)
					{
						nextpot = cl.getStandartPot();
					} else
					{
						if(nextpot < cl.getStandartPot())
						{
							nextpot = cl.getStandartPot();
						}
						nextpot += cl.getAmountToAddToThePotIfNoOneIsWinning();
					}
					s = s.replace("%nextpot%", EconomyHandler.format(nextpot));
				}
			} else
			{
				s = s.replace("%winningclasswinneramount%", "/")
						.replace("%nextpot%", "/");
			}
			li.add(s);
		}
		return li;
	}
	
	public static String matchChoosenNumber(LinkedHashSet<Integer> set, int i)
	{
		return set.contains(i) 
				? LLY.getPlugin().getYamlHandler().getLang().getString("WasChoosen")
						.replace("%number%", String.valueOf(i))
				: LLY.getPlugin().getYamlHandler().getLang().getString("WasntChoosen")
						.replace("%number%", String.valueOf(i)); 
	}
	
	public static ClassicLottoPayout getPayout(ArrayList<ClassicLottoPayout> list, int level)
	{
		Optional<ClassicLottoPayout> op = list.stream()
				.filter(x -> x.getWinningClassLevel() == level)
				.findFirst();
		return op.isPresent() ? op.get() : null;
	}
	
	public static int getIndexPayout(ArrayList<ClassicLottoPayout> list, int matchNumber)
	{
		for(ClassicLottoPayout clp : list)
		{
			if(clp.getNumberMatchToWin() == matchNumber)
			{
				return clp.getWinningClassLevel();
			}
		}
		//If no CLP matched, reduce the matchNumber by one.
		//This should done, to catch WinningClass gaps.
		int mn = matchNumber-1;
		if(mn < 0)
		{
			//Exist and Error, because it should ALWAYS be a CLP with 0 matches!
			return 0;
		} else
		{
			return getIndexPayout(list, mn);
		}
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
	
	//------------------------------
	
	public static void main(String[] args) {
	    LocalDateTime now = LocalDateTime.of(2025, 2, 4, 8, 21);

	    ArrayList<DrawTime> allDrawTimes = new ArrayList<>();
	    allDrawTimes.add(new DrawTime(1, DayOfWeek.MONDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(1, DayOfWeek.WEDNESDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(1, DayOfWeek.SUNDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(2, DayOfWeek.MONDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(2, DayOfWeek.WEDNESDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(2, DayOfWeek.SUNDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(3, DayOfWeek.MONDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(3, DayOfWeek.WEDNESDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(3, DayOfWeek.SUNDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(4, DayOfWeek.MONDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(4, DayOfWeek.WEDNESDAY, 20, 00));
	    allDrawTimes.add(new DrawTime(4, DayOfWeek.SUNDAY, 20, 00));
	    
	    LocalDateTime nextDraw = DrawTime.getNextTime(allDrawTimes, now);
	    ArrayList<LocalDateTime> allTimes = DrawTime.getNextTimes(allDrawTimes, now);

	    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
	    System.out.println("Aktuelle Zeit: "+now.format(dtf)+" | " +DrawTime.getNow(now));
	    System.out.println("Nächste Ziehung: "+nextDraw.format(dtf)+" | "+DrawTime.getNow(nextDraw));
	    allTimes.stream().forEach(x -> System.out.println("Ziehung: "+x.format(dtf)));
	    
	}
}
