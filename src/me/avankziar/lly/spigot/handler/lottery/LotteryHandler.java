package me.avankziar.lly.spigot.handler.lottery;

import java.util.Collection;
import java.util.Optional;

import me.avankziar.lly.general.objects.lottery.ClassicLotto;

public class LotteryHandler 
{
	public static Collection<ClassicLotto> getClassicLottery(){return ClassicLottoHandler.classicLotto;}
	public static Optional<ClassicLotto> getClassicLottery(String uniquename)
	{
		return getClassicLottery().stream()
				.filter(x -> x.getLotteryName().equals(uniquename))
				.findFirst();
	}
	
	public static void initalized()
	{
		ClassicLottoHandler.initalizedYamls();
	}
}
