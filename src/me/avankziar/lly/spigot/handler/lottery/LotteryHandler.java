package me.avankziar.lly.spigot.handler.lottery;

import java.time.DayOfWeek;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;

import dev.dejvokep.boostedyaml.YamlDocument;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.DrawTime;
import me.avankziar.lly.general.objects.lottery.Lottery.GameType;
import me.avankziar.lly.general.objects.lottery.WinningCategory;
import me.avankziar.lly.general.objects.lottery.WinningCategory.PayoutType;
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
				String lottoname = y.getString("LotteryName");
				String description = y.getString("Description");
				double standartPot = y.getDouble("StandartPot");
				double maximumPot = y.getDouble("MaximumPot");
				double amountToAddToThePotIfNoOneIsWinning = y.getDouble("AmountToAddToThePotIfNoOneIsWinning");
				double costPerTicket = y.getDouble("CostPerTicket");
				int fristNumberToChooseFrom = y.getInt("FristNumberToChooseFrom");
				int lastNumberToChooseFrom = y.getInt("LastNumberToChooseFrom");
				int amountOfChoosedNumber = y.getInt("AmountOfChoosedNumber");
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
					if(y.contains("WinningCategory."+i+".PayoutPercentage"));
					{
						double payoutpercentage = y.getDouble("WinningCategory."+i+".PayoutPercentage");
						WinningCategory wc = new WinningCategory(i, PayoutType.PERCENTAGE, payoutpercentage);
						winningCategory.add(wc);
					}/* else
					{
						//TODO
					}*/
					i++;
				}
				ClassicLotto cl = new ClassicLotto(lottoname, description, GameType.X_FROM_Y,
						standartPot, maximumPot, amountToAddToThePotIfNoOneIsWinning, costPerTicket, 
						fristNumberToChooseFrom, lastNumberToChooseFrom, amountOfChoosedNumber, 
						drawTime, winningCategory);
				LLY.logger.info("ClassicLottery "+lottoname+" loaded!");
				classicLotto.add(cl);
			} catch(Exception e)
			{
				continue;
			}
		}
	}
}
