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
import java.util.stream.Collectors;

import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.assistance.TimeHandler;
import me.avankziar.lly.general.assistance.Utility;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.WinningClass.PayoutType;
import me.avankziar.lly.general.objects.WinningClassSuper;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
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
				if(y.contains("LotteryName") || y.contains("Description") || y.contains("DrawTime")
						|| y.contains("WinningClass.1.Payout"))
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
				int additionalLastNumberToChooseFrom = y.getInt("AdditionalLastNumberToChooseFrom", 10);
				int additionalAmountOfChoosedNumber = y.getInt("AdditionalAmountOfChoosedNumber", 2);
				LinkedHashSet<DrawTime> drawTime = new LinkedHashSet<>();
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
						DayOfWeek dayOfWeek = DayOfWeek.valueOf(a[0]);
						int hour = Integer.valueOf(a[1]);
						int min = Integer.valueOf(a[2]);
						drawTime.add(new DrawTime(weekOfMonth, dayOfWeek, hour, min));
					} catch(Exception e)
					{
						LLY.log.warning(lottoname+" has by DrawTime a Problem! "
								+ "DrawTime "+s+" is incorrect! DrawTime will be ignored!");
						continue;
					}
				}
				String drawOnServer = y.getString("DrawOnServer", "hub");
				boolean drawManually = y.getBoolean("DrawManually", false);
				LinkedHashSet<WinningClassSuper> winningCategory = new LinkedHashSet<>();
				int i = 1;
				boolean check = false;
				while(i <= amountOfChoosedNumber * (additionalAmountOfChoosedNumber+1))
				{
					if(!y.contains("WinningClass."+i+".PayoutPercentage"))
					{
						check = true;
						LLY.log.warning(lottoname+" WinningClass number "+i+" is missing! "+
								lottoname+" will not be registered!");
						break;
					}
					PayoutType payt = PayoutType.valueOf(y.getString("WinningClass."+i+".PayoutType", PayoutType.LUMP_SUM.toString()));
					double payout = y.getDouble("WinningClass."+i+".Payout");
					int numberMatchToWin = y.getInt("WinningClass."+i+".NumberMatchToWin");
					int addtionalNumberMatchToWin = y.getInt("WinningClass."+i+".AddtionalNumberMatchToWin");
					WinningClassSuper wc = new WinningClassSuper(i, payt, payout,
							numberMatchToWin, addtionalNumberMatchToWin);
					winningCategory.add(wc);
					i++;
				}
				if(check)
				{
					continue;
				}
				LottoSuper ls = new LottoSuper(lottoname, description, GameType.X_FROM_Y_AND_Z_FROM_U,
						standartPot, maximumPot, amountToAddToThePotIfNoOneIsWinning, costPerTicket, 
						maximalAmountOfTicketWhichCanAPlayerBuy,
						fristNumberToChooseFrom, lastNumberToChooseFrom, amountOfChoosedNumber, 
						drawTime, winningCategory, drawOnServer, drawManually,
						additionalFristNumberToChooseFrom,
						additionalLastNumberToChooseFrom,
						additionalAmountOfChoosedNumber);
				LLY.log.info("LottoSuper "+lottoname+" loaded!");
				lottosuper.add(ls);
				LottoSuperTicket clt = new LottoSuperTicket(lottoname);
				clt.setupMysql(LLY.getPlugin().getMysqlSetup());
				LottoSuperDraw cld = new LottoSuperDraw(lottoname);
				cld.setupMysql(LLY.getPlugin().getMysqlSetup());
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
		}.runTaskTimer(LLY.getPlugin(), 0L, 60+20L);
	}
	
	private static void asyncDraw()
	{
		final String server = LLY.getPlugin().getServername();
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				LocalDateTime ldt = LocalDateTime.now();
				for(LottoSuper ls : LotteryHandler.getLottoSuper())
				{
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
							drawLotto(ls, new LinkedHashSet<Integer>(), new LinkedHashSet<Integer>());
						}
					}
				}
			}
		}.runTaskAsynchronously(LLY.getPlugin());
	}
	
	public static void drawLotto(final LottoSuper ls, LinkedHashSet<Integer> manuallyDrawnNumber,
			LinkedHashSet<Integer> manuallyAddtionalDrawnNumber)
	{
		LLY plugin = LLY.getPlugin();
		LottoSuperDraw lsd = plugin.getMysqlHandler().getData(ls.getDrawMysql(), "`was_drawn` = ?", false);
		if(lsd == null)
		{
			LLY.log.warning("Attention! The MysqlObject of LottoSuperDraw is missing! Draw of "
					+ls.getLotteryName()+" canceled!");
			return;
		}
		//Lottery Draw
		LinkedHashSet<Integer> drawnNumber = manuallyDrawnNumber;
		if(manuallyDrawnNumber.size() <= 0)
		{
			drawnNumber = drawLotteryNumber(ls.getFirstNumberToChooseFrom(),
					ls.getLastNumberToChooseFrom(), ls.getAmountOfChoosedNumber());
		}
		LinkedHashSet<Integer> drawnAdditionalNumber = manuallyAddtionalDrawnNumber;
		if(manuallyAddtionalDrawnNumber.size() <= 0)
		{
			drawnAdditionalNumber = drawLotteryNumber(ls.getAdditionalFirstNumberToChooseFrom(),
					ls.getAdditionalLastNumberToChooseFrom(), ls.getAdditionalAmountOfChoosenNumber());
		}
		//Sorting DrawnNumber
		drawnNumber = sortDrawnNumber(drawnNumber);
		drawnAdditionalNumber = sortDrawnNumber(drawnAdditionalNumber);
		//Update Object
		lsd.setChoosenNumbers(drawnNumber);
		lsd.setAdditionalChoosenNumbers(drawnAdditionalNumber);
		//Lotto Ticket call
		ArrayList<LottoSuperTicket> lstA = plugin.getMysqlHandler().getFullList(ls.getTicketMysql(),
				"`id` ASC", "`draw_id` = ?", lsd.getId());
		if(lstA.isEmpty())
		{
			//No Tickets was bought
			Collection<UUID> uuids = Bukkit.getOnlinePlayers().stream()
					.map(x -> x.getUniqueId()).collect(Collectors.toSet());
			if(plugin.getProxyOnlinePlayers() != null)
			{
				uuids = plugin.getProxyOnlinePlayers().getProxyOnlinePlayers().keySet();
			}
			List<String> msgl = replacer(ls, null, null, null, null, null, false,
					(ArrayList<String>) plugin.getYamlHandler().getLang()
					.getStringList("LottoSuper.Draw.NoTicketAreBought"));
			MessageHandler.sendMessage(uuids, msgl.toArray(new String[msgl.size()]));
			return;
		}
		//Get Next LottoSuperDraw ID
		int lsdNextId = plugin.getMysqlHandler().lastID(lsd)+1;
		//Adding all WinningClass to a payout object
		ArrayList<LottoSuperPayout> lspA = new ArrayList<>();
		//Added WinningClass 0, for all player that lost
		lspA.add(new LottoSuperPayout(0, PayoutType.LUMP_SUM, 0, new HashSet<UUID>(), 0, 0));
		//Create all WinningClasss
		int highestWC = 1;
		for(WinningClassSuper wcs : ls.getWinningClassSuper())
		{
			double payout = 0.0;
			switch(wcs.getPayoutType())
			{
			case LUMP_SUM: payout = wcs.getAmount();
			case PERCENTAGE: payout = lsd.getActualPot() * wcs.getAmount() / 100;
			}
			lspA.add(new LottoSuperPayout(wcs.getWinningClassLevel(),
					wcs.getPayoutType(), payout,
					new HashSet<UUID>(), wcs.getNumberMatchToWin(), wcs.getAddtionalNumberMatchToWin()));
			if(wcs.getWinningClassLevel() > highestWC)
			{
				highestWC = wcs.getWinningClassLevel();
			}
		}
		//Lotto Ticket Evaluation
		String repeatCategory = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.RepeatTicket.Category")
				.replace("%lotteryname%", ls.getLotteryName());
		String repeatComment = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.RepeatTicket.Comment");
		LinkedHashMap<Integer, ArrayList<LottoSuperTicket>> payoutToTicket = new LinkedHashMap<>();
		for(LottoSuperTicket lst : lstA)
		{
			int matchNumber = matchChoosenNumber(drawnNumber, lst.getChoosenNumbers());
			int matchAddtionalNumber = matchChoosenNumber(drawnAdditionalNumber, lst.getAdditionalChoosenNumbers());
			int index = getWinningClassLevelPayout(lspA, matchNumber, matchAddtionalNumber);
			LottoSuperPayout lsp = lspA.get(index);
			lsp.getUUIDs().add(lst.getLotteryPlayer());
			lspA.set(index, lsp);
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
					LottoSuperTicket cltRepeat = lst;
					cltRepeat.setDrawID(lsdNextId);
					plugin.getMysqlHandler().create(cltRepeat);
				}
				lst.setShouldRepeate(false);
				plugin.getMysqlHandler().updateData(lst, "`id` = ?", lst.getID());
			}
			//Adding all Tickets to a list which is put in a LinkedHashMap sorted after CLP Level.
			ArrayList<LottoSuperTicket> lstATPo = new ArrayList<>();
			if(payoutToTicket.containsKey(index))
			{
				lstATPo = payoutToTicket.get(index);
			}
			lstATPo.add(lst);
			payoutToTicket.put(index, lstATPo);
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
		lspA.sort(Comparator.comparingInt(LottoSuperPayout::getWinningClassLevel).reversed());
		HashSet<UUID> winnercheck = new HashSet<>();
		//Give Players a the price and send a message
		double nextpot = 0.0;
		for(LottoSuperPayout lsp : lspA)
		{
			int lv = lsp.getWinningClassLevel();
			if(lv == 0)
			{
				//CLP Lv 0 for all player which has Lost AND where other ticket of the same player didnt also not win.
				for(UUID uuid : lsp.getUUIDs())
				{
					if(!winnercheck.contains(uuid))
					{
						ArrayList<String> nowinmsg = replacer(ls, lsd, null, lspA, null, null, hlsp.getUUIDs().size() > 0,
								(ArrayList<String>) plugin.getYamlHandler().getLang().getStringList("LottoSuper.Draw.NotWon"));
						MessageHandler.sendMessage(uuid, nowinmsg.toArray(new String[nowinmsg.size()]));
					}
				}
				break;
			}
			if(lsp.getUUIDs().size() == 0)
			{
				//No one won in this payout/winningclass. Added in Next pot.
				nextpot += lsp.getPayout();
				continue;
			}
			double payout = lsp.getFinalPayoutPerUUID();
			String wincategory = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.Win.Category");
			String wincomment = plugin.getYamlHandler().getLang().getString("LottoSuper.Draw.Win.Comment")
					.replace("%level%", String.valueOf(lv));
			for(LottoSuperTicket lst : payoutToTicket.get(lv))
			{
				ArrayList<String> winmsg = replacer(ls, lsd, lst, lspA, payout, null, hlsp.getUUIDs().size() > 0,
						(ArrayList<String>) plugin.getYamlHandler().getLang().getStringList("LottoSuper.Draw.Won"));
				EconomyHandler.deposit(lst.getLotteryPlayer(), payout, 
						wincategory, 
						wincomment);
				MessageHandler.sendMessage(lst.getLotteryPlayer(), winmsg.toArray(new String[winmsg.size()]));
			}	
			winnercheck.addAll(lsp.getUUIDs());
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
		LottoSuperDraw lsdNext = new LottoSuperDraw(0, ls.getLotteryName(), false, 0, nextpot,
				new LinkedHashSet<>(), new LinkedHashSet<>());
		plugin.getMysqlHandler().create(lsdNext);
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
		List<Integer> sort = List.of();
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
			ArrayList<LottoSuperPayout> lspA, Double payout, String jackpotwinners, boolean wasJackpotBreached,
			ArrayList<String> list)
	{
		ArrayList<String> l = new ArrayList<>();
		for(String s : l)
		{
			if(payout != null)
			{
				s = s.replace("%payout%", EconomyHandler.format(payout));
			}
			if(jackpotwinners != null)
			{
				s = s.replace("%winners%", jackpotwinners);
			}
			if(s.contains("%lottosupercmd%"))
			{
				s = s.replace("lottosupercmd%", CommandSuggest.getCmdString(CommandSuggest.Type.LOTTOSUPER));
			}
			if(ls != null)
			{
				s = s.replace("%lotteryname%", ls.getLotteryName())
						.replace("%description%", ls.getDescription())
						.replace("%amountofchoosennumber%", String.valueOf(ls.getAmountOfChoosedNumber()))
						.replace("%additionalamountofchoosennumber%", String.valueOf(ls.getAdditionalAmountOfChoosenNumber()))
						.replace("%amounttoaddpot%", EconomyHandler.format(ls.getAmountToAddToThePotIfNoOneIsWinning()))
						.replace("%costperticket%", EconomyHandler.format(ls.getCostPerTicket()))
						.replace("%fristnumbertochoosefrom%", String.valueOf(ls.getFirstNumberToChooseFrom()))
						.replace("%lastnumbertochoosefrom%", String.valueOf(ls.getLastNumberToChooseFrom()))
						.replace("%additionalfristnumbertochoosefrom%", String.valueOf(ls.getAdditionalFirstNumberToChooseFrom()))
						.replace("%additionallastnumbertochoosefrom%", String.valueOf(ls.getAdditionalLastNumberToChooseFrom()))
						.replace("%maximumpot%", EconomyHandler.format(ls.getMaximumPot()))
						.replace("%standartpot%", EconomyHandler.format(ls.getStandartPot()))
						.replace("%winningchance%", String.valueOf(ls.getWinningChance().longValue()));
				for(WinningClassSuper wc : ls.getWinningClassSuper())
				{
					s = s.replace("%wc"+wc.getWinningClassLevel()+"level%", String.valueOf(wc.getWinningClassLevel())
							.replace("%wc"+wc.getWinningClassLevel()+"percentage%", String.valueOf(wc.getAmount()))
							.replace("%wc"+wc.getWinningClassLevel()+"lottopayout%", 
									EconomyHandler.format(ls.getStandartPot() * wc.getAmount() / 100)));
				}
			}
			if(lsd != null)
			{
				ArrayList<String> ccn = new ArrayList<>();
				for(Iterator<Integer> iter = lsd.getChoosenNumbers().iterator(); iter.hasNext();)
				{
					int cn = iter.next();
					ccn.add(String.valueOf(cn));
				}
				ArrayList<String> ccan = new ArrayList<>();
				for(Iterator<Integer> iter = lsd.getAdditionalChoosenNumbers().iterator(); iter.hasNext();)
				{
					int cn = iter.next();
					ccan.add(String.valueOf(cn));
				}
				s = s.replace("%lotteryname%", lsd.getLotteryName())
						.replace("%drawid%", String.valueOf(lsd.getId()))
						.replace("%drawtime%", lsd.wasDrawn() ? TimeHandler.getDate(lsd.getDrawTime()) 
								: LLY.getPlugin().getYamlHandler().getLang().getString("WasntDrawn"))
						.replace("%actualpot%", EconomyHandler.format(lsd.getActualPot()))
						.replace("%drawnnumber%", "["+String.join(", ", ccn)+"]")
						.replace("%drawnadditionalnumber%", "["+String.join(", ", ccan)+"]");
			}
			if(lst != null)
			{
				if(lsd != null)
				{
					ArrayList<String> ccn = new ArrayList<>();
					for(Iterator<Integer> iter = lst.getChoosenNumbers().iterator(); iter.hasNext();)
					{
						int cn = iter.next();
						ccn.add(matchChoosenNumber(lsd.getChoosenNumbers(), cn));
					}
					ArrayList<String> ccan = new ArrayList<>();
					for(Iterator<Integer> iter = lsd.getAdditionalChoosenNumbers().iterator(); iter.hasNext();)
					{
						int cn = iter.next();
						ccan.add(matchChoosenNumber(lsd.getAdditionalChoosenNumbers(), cn));
					}
					s = s.replace("%matchchoosennumber%", "["+String.join(", ", ccn)+"]")
							.replace("%matchchoosennumberamount%", String.valueOf(ccn.size()))
							.replace("%matchadditionalchoosennumber%", "["+String.join(", ", ccan)+"]")
							.replace("%matchadditionalchoosennumberamount%", String.valueOf(ccan.size()));
				}
				s = s.replace("%lotteryname%", lst.getLotteryName())
						.replace("%ticketid%", String.valueOf(lst.getID()))
						.replace("%ticketplayer%", Utility.convertUUIDToName(lst.getLotteryPlayer().toString()));
			}
			if(lspA != null)
			{
				StringBuilder wcwinneramount = new StringBuilder();
				double nextpot = 0.0;
				for(int i = 0; i < lspA.size(); i++)
				{
					LottoSuperPayout clp = lspA.get(i);
					wcwinneramount.append(LLY.getPlugin().getYamlHandler().getLang()
							.getString("LottoSuper.Draw.WinningClassReplacer")
							.replace("%level%", String.valueOf(clp.getWinningClassLevel()))
							.replace("%winneramount%", String.valueOf(clp.getUUIDs().size())));
					if(i+1 < lspA.size())
					{
						wcwinneramount.append(LLY.getPlugin().getYamlHandler().getLang()
								.getString("LottoSuper.Draw.WinningClassReplacerSeperator"));
					}
					if(clp.getUUIDs().size() <= 0)
					{
						nextpot += clp.getPayout();
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
			}
			l.add(s);
		}
		return l;
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
	
	public static int getWinningClassLevelPayout(ArrayList<LottoSuperPayout> list, int level, int additionalLevel)
	{
		for(LottoSuperPayout clp : list)
		{
			if(clp.getNumberMatchToWin() == level && clp.getAddtionalNumberMatchToWin() == additionalLevel)
			{
				return clp.getWinningClassLevel();
			}
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