package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_GiveTicket extends ArgumentModule
{
	private LLY plugin;
	private ArgumentConstructor ac;
	
	public ARG_GiveTicket(ArgumentConstructor ac)
	{
		super(ac);
		this.ac = ac;
	}

	/**
	 * => /classiclotto giveticket lotteryname 
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws IOException 
	{
		Player player = (Player) sender;
		new BukkitRunnable() 
		{	
			@Override
			public void run() 
			{
				task(player, args);
			}
		}.runTaskAsynchronously(plugin);
		
	}
	
	private void task(Player player, String[] args)
	{
		String scl = args[1];
		Optional<ClassicLotto> ocl = LotteryHandler.getClassicLottery(scl);
		if(ocl.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.NoClassicLottoFound"));
			return;
		}
		ClassicLotto cl = ocl.get();
	}
}
