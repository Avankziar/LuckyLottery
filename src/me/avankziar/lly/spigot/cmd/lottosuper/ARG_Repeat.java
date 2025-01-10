package me.avankziar.lly.spigot.cmd.lottosuper;

import java.io.IOException;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.general.objects.lottery.ticket.LottoSuperTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_Repeat extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_Repeat(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /lottosuper repeat lotteryname [ticketid]
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws IOException 
	{
		Player player = (Player) sender;
		task(player, args);
	}
	
	private void task(Player player, String[] args)
	{
		String sls = args[1];
		Optional<LottoSuper> ols = LotteryHandler.getLottoSuper(sls);
		if(ols.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.NoLottoSuperFound"));
			return;
		}
		LottoSuper ls = ols.get();
		int id = 0;
		if(MatchApi.isInteger(args[2]))
		{
			id = Integer.valueOf(args[2]);
		} else
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoNumber").replace("%value%", args[2]));
			return;
		}
		LottoSuperTicket lst = plugin.getMysqlHandler().getData(ls.getTicketMysql(), "`id` = ?", id);
		if(lst == null)
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Repeat.DontExist")
					.replace("%lotteryname%", ls.getLotteryName())
					.replace("%id%", String.valueOf(id)));
			return;
		}
		if(!lst.getLotteryPlayer().toString().equals(player.getUniqueId().toString()))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Repeat.DontYours")
					.replace("%lotteryname%", ls.getLotteryName())
					.replace("%id%", String.valueOf(id)));
			return;
		}
		LottoSuperDraw cld = plugin.getMysqlHandler().getData(ls.getDrawMysql(), "`id` = ?", lst.getDrawID());
		String msg = "";
		if(cld == null)
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Repeat.DontExist")
					.replace("%lotteryname%", ls.getLotteryName())
					.replace("%id%", String.valueOf(id)));
			return;
		} else
		{
			if(cld.wasDrawn())
			{
				msg = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Repeat.TicketCreated");
				if(!EconomyHandler.hasBalance(player.getUniqueId(), ls.getCostPerTicket()))
				{
					MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NotEnoughMoney"));
					return;
				}
				LottoSuperDraw cldNext = plugin.getMysqlHandler().getData(ls.getDrawMysql(), "`was_drawn` = ?", false);
				if(cldNext == null)
				{
					MessageHandler.sendMessage(player.getUniqueId(), plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.LotteryNotOpen"));
					return;
				}
				MessageHandler.sendMessage(player, msg);
				plugin.getMysqlHandler().create(new LottoSuperTicket(id, cldNext.getId(), ls.getLotteryName(),
						player.getUniqueId(), true, 0, 0, lst.getChoosenNumbers(), lst.getAdditionalChoosenNumbers()));
				String cat = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Category").replace("%lotteryname%", ls.getLotteryName());
				String comment = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Comment");
				EconomyHandler.withdraw(player.getUniqueId(), ls.getCostPerTicket(), cat, comment);
				MessageHandler.sendMessage(player, 
						ARG_Play.replace(lst.getChoosenNumbers(), lst.getAdditionalChoosenNumbers(),
								plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.TicketBought")));
				
				return;
			} else
			{
				final boolean repeat = lst.shouldRepeate();
				lst.setShouldRepeate(!repeat);
				plugin.getMysqlHandler().updateData(lst, "`id` = ?", id);
				msg = repeat 
						? plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Repeat.DontRepeat")
								.replace("%lotteryname%", ls.getLotteryName())
								.replace("%id%", String.valueOf(id))
						: plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Repeat.Repeat")
								.replace("%lotteryname%", ls.getLotteryName())
								.replace("%id%", String.valueOf(id));
			}
		}
		MessageHandler.sendMessage(player, msg);
	}
}