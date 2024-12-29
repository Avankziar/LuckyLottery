package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_AddPot extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_AddPot(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /classiclotto addpot lotteryname double [-broadcast]
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws IOException 
	{
		Player player = (Player) sender;
		task(player, args);
	}
	
	private void task(Player player, String[] args)
	{
		String scl = args[1];
		Optional<ClassicLotto> ocl = LotteryHandler.getClassicLotto(scl);
		if(ocl.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.NoClassicLottoFound"));
			return;
		}
		ClassicLotto cl = ocl.get();
		if(!MatchApi.isDouble(args[2]))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoDouble")
					.replace("%value%", args[2]));
			return;
		}
		double d = Double.valueOf(args[2]);
		ClassicLottoDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		if(cld == null)
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.NoClassicLottoDrawFound"));
			return;
		}
		boolean broadcast = false;
		if(args.length >= 4)
		{
			if("-broadcast".equalsIgnoreCase(args[3]))
			{
				broadcast = true;
			}
		}
		final double oldpot = cld.getActualPot();
		cld.setActualPot(cld.getActualPot() + d);
		final double actualpot = oldpot+d;
		plugin.getMysqlHandler().updateData(cld, "`id` = ?", cld.getId());
		String msg = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.AddPot.Added")
				.replace("%oldpot%", EconomyHandler.format(oldpot))
				.replace("%actualpot%", EconomyHandler.format(actualpot))
				.replace("%added%", EconomyHandler.format(d))
				.replace("%lotteryname%", cl.getLotteryName());
		if(broadcast)
		{
			MessageHandler.sendMessage(msg);
		} else
		{
			MessageHandler.sendMessage(player, msg);
		}
	}
}