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

public class ARG_SetPot extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_SetPot(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /lottosuper setpot lotteryname double [-broadcast]
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
		Optional<LottoSuper> ocl = LotteryHandler.getLottoSuper(scl);
		if(ocl.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.NoLottoSuperFound"));
			return;
		}
		LottoSuper cl = ocl.get();
		if(!MatchApi.isDouble(args[2]))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoDouble")
					.replace("%value%", args[2]));
			return;
		}
		double d = Double.valueOf(args[2]);
		LottoSuperDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		if(cld == null)
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.NoLottoSuperDrawFound"));
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
		cld.setActualPot(d);
		plugin.getMysqlHandler().updateData(cld, "`id` = ?", cld.getId());
		String msg = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.SetPot.Set")
				.replace("%oldpot%", EconomyHandler.format(oldpot))
				.replace("%actualpot%", EconomyHandler.format(d))
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