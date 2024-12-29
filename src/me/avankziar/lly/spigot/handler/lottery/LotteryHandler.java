package me.avankziar.lly.spigot.handler.lottery;

import java.util.Collection;
import java.util.Optional;

import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.LottoSuper;

public class LotteryHandler 
{
	public static Collection<ClassicLotto> getClassicLotto(){return ClassicLottoHandler.classicLotto;}
	public static Optional<ClassicLotto> getClassicLotto(String uniquename)
	{
		return getClassicLotto().stream()
				.filter(x -> x.getLotteryName().equals(uniquename))
				.findAny();
	}
	
	public static Collection<LottoSuper> getLottoSuper(){return LottoSuperHandler.lottosuper;}
	public static Optional<LottoSuper> getLottoSuper(String uniquename)
	{
		return getLottoSuper().stream()
				.filter(x -> x.getLotteryName().equals(uniquename))
				.findAny();
	}
	
	public static void initalized()
	{
		ClassicLottoHandler.initalizedYamls();
		//LottoSuperHandler.initalizedYamls();
	}
	
	public static void initalizedDraws()
	{
		ClassicLottoHandler.initalizedDraws();
		///LottoSuperHandler.initalizedDraws();
	}
}
