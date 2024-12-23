package me.avankziar.lly.spigot.handler.lottery;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.WinningCategory;
import me.avankziar.lly.general.objects.WinningCategory.PayoutType;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lotterydraw.ClassicLottoDraw;
import me.avankziar.lly.general.objects.lotteryticket.ClassicLottoTicket;
import me.avankziar.lly.spigot.LLY;

public class LotteryHandler 
{
	private static Collection<ClassicLotto> classicLotto = new HashSet<>();
	
	public static Collection<ClassicLotto> getClassicLottery()
	{
		return classicLotto;
	}
	
	public static Optional<ClassicLotto> getClassicLottery(String uniquename)
	{
		return classicLotto.stream()
				.filter(x -> x.getLotteryName().equals(uniquename))
				.findFirst();
	}
	
	public static void initalized()
	{
		for(YamlDocument y : LLY.getPlugin().getYamlHandler().getClassicLotto())
		{
			try
			{
				if(y.contains("LotteryName") || y.contains("Description") || y.contains("DrawTime")
						|| y.contains("WinningCategory.1.PayoutPercentage"))
				{
					continue;
				}
				String lottoname = y.getString("LotteryName");
				String description = y.getString("Description");
				double standartPot = y.getDouble("StandartPot", 1_000_000.0);
				double maximumPot = y.getDouble("MaximumPot", 10_000_000.0);
				double amountToAddToThePotIfNoOneIsWinning = y.getDouble("AmountToAddToThePotIfNoOneIsWinning", 500_000.0);
				double costPerTicket = y.getDouble("CostPerTicket", 2.5);
				int fristNumberToChooseFrom = y.getInt("FristNumberToChooseFrom", 1);
				int lastNumberToChooseFrom = y.getInt("LastNumberToChooseFrom", 49);
				int amountOfChoosedNumber = y.getInt("AmountOfChoosedNumber", 6);
				LinkedHashSet<DrawTime> drawTime = new LinkedHashSet<>();
				for(String s : y.getStringList("DrawTime"))
				{
					String[] a = s.split("-");
					if(a.length != 3)
					{
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
						continue;
					}
				}
				LinkedHashSet<WinningCategory> winningCategory = new LinkedHashSet<>();
				int i = 1;
				while(i <= 1000)
				{
					if(!y.contains("WinningCategory."+i+".PayoutPercentage"))
					{
						break;
					}
					double payoutpercentage = y.getDouble("WinningCategory."+i+".PayoutPercentage");
					WinningCategory wc = new WinningCategory(i, PayoutType.PERCENTAGE, payoutpercentage);
					winningCategory.add(wc);
					i++;
				}
				ClassicLotto cl = new ClassicLotto(lottoname, description, GameType.X_FROM_Y,
						standartPot, maximumPot, amountToAddToThePotIfNoOneIsWinning, costPerTicket, 
						fristNumberToChooseFrom, lastNumberToChooseFrom, amountOfChoosedNumber, 
						drawTime, winningCategory);
				LLY.logger.info("ClassicLottery "+lottoname+" loaded!");
				classicLotto.add(cl);
				ClassicLottoTicket clt = new ClassicLottoTicket(lottoname);
				clt.setupMysql(LLY.getPlugin().getMysqlSetup());
				ClassicLottoDraw cld = new ClassicLottoDraw(lottoname);
				
			} catch(Exception e)
			{
				continue;
			}
		}
	}
}
