package me.avankziar.lly.spigot.handler.lottery;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import org.bukkit.scheduler.BukkitRunnable;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.WinningCategory;
import me.avankziar.lly.general.objects.WinningCategory.PayoutType;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.general.objects.lottery.ticket.LottoSuperTicket;
import me.avankziar.lly.spigot.LLY;

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
						|| y.contains("WinningCategory.1.PayoutPercentage"))
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
					if(a.length != 3)
					{
						LLY.log.warning(lottoname+" has by DrawTime a Problem! "
										+ "DrawTime "+s+" missed 2 of `-` Character! "
										+ "DrawTime was not registered!");
						continue;
					}
					try
					{
						DayOfWeek dayOfWeek = DayOfWeek.valueOf(a[0]);
						int hour = Integer.valueOf(a[1]);
						int min = Integer.valueOf(a[2]);
						drawTime.add(new DrawTime(dayOfWeek, hour, min));
					} catch(Exception e)
					{
						LLY.log.warning(lottoname+" has by DrawTime a Problem! "
								+ "DrawTime "+s+" is incorrect!");
						continue;
					}
				}
				String drawOnServer = y.getString("DrawOnServer", "hub");
				boolean drawManually = y.getBoolean("DrawManually", false);
				LinkedHashSet<WinningCategory> winningCategory = new LinkedHashSet<>();
				int i = 1;
				boolean check = false;
				while(i <= amountOfChoosedNumber * (additionalAmountOfChoosedNumber+1))
				{
					if(!y.contains("WinningCategory."+i+".PayoutPercentage"))
					{
						check = true;
						LLY.log.warning(lottoname+" WinningCategory number "+i+" is missing! "+
								lottoname+" will not be registered!");
						break;
					}
					double payoutpercentage = y.getDouble("WinningCategory."+i+".PayoutPercentage");
					WinningCategory wc = new WinningCategory(i, PayoutType.PERCENTAGE, payoutpercentage);
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
						if(dr.isNow(LocalDateTime.now()))
						{
							drawLotto(ls, new LinkedHashSet<Integer>());
						}
					}
				}
			}
		}.runTaskAsynchronously(LLY.getPlugin());
	}
}