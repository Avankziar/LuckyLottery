package me.avankziar.lly.spigot.cmd.lottosuper;

import java.io.IOException;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
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
	 * => /lottosuper addpot lotteryname double [-broadcast]
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
		Optional<LottoSuper> ols = LotteryHandler.getLottoSuper(scl);
		if(ols.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.NoClassicLottoFound"));
			return;
		}
		LottoSuper ls = ols.get();
		if(!MatchApi.isDouble(args[2]))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoDouble")
					.replace("%value%", args[2]));
			return;
		}
		double d = Double.valueOf(args[2]);
		LottoSuperDraw lsd = plugin.getMysqlHandler().getData(ls.getDrawMysql(), "`was_drawn` = ?", false);
		if(lsd == null)
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.NoClassicLottoDrawFound"));
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
		final double oldpot = lsd.getActualPot();
		lsd.setActualPot(lsd.getActualPot() + d);
		final double actualpot = oldpot+d;
		plugin.getMysqlHandler().updateData(lsd, "`id` = ?", lsd.getId());
		String msg = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.AddPot.Added")
				.replace("%oldpot%", EconomyHandler.format(oldpot))
				.replace("%actualpot%", EconomyHandler.format(actualpot))
				.replace("%added%", EconomyHandler.format(d))
				.replace("%lotteryname%", ls.getLotteryName());
		if(broadcast)
		{
			MessageHandler.sendMessage(msg);
		} else
		{
			MessageHandler.sendMessage(player, msg);
		}
	}
}