package me.avankziar.lly.spigot.handler.lottery;

import java.time.LocalDateTime;

import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.DrawTime;
import me.avankziar.lly.spigot.LLY;

public class ClassicLottoHandler 
{
	public static void init()
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				for(ClassicLotto cl : LotteryHandler.getClassicLottery())
				{
					for(DrawTime dr : cl.getDrawTime())
					{
						if(dr.isNow(LocalDateTime.now()))
						{
							drawLotto(cl);
						}
					}
				}
			}
		}.runTaskTimer(LLY.getPlugin(), 0L, 60+20L);
	}
	
	public static void drawLotto(final ClassicLotto cl)
	{
		new BukkitRunnable() 
		{
			@Override
			public void run() 
			{
				
			}
		}.runTaskAsynchronously(LLY.getPlugin());
	}
}
