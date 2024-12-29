package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.general.objects.lottery.ticket.ClassicLottoTicket;
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
	 * => /classiclotto repeat lotteryname [ticketid]
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
		int id = 0;
		if(MatchApi.isInteger(args[2]))
		{
			id = Integer.valueOf(args[2]);
		} else
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoNumber").replace("%value%", args[2]));
			return;
		}
		ClassicLottoTicket clt = plugin.getMysqlHandler().getData(cl.getTicketMysql(), "`id` = ?", id);
		if(clt == null)
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Repeat.DontExist")
					.replace("%lotteryname%", cl.getLotteryName())
					.replace("%id%", String.valueOf(id)));
			return;
		}
		if(!clt.getLotteryPlayer().toString().equals(player.getUniqueId().toString()))
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Repeat.DontYours")
					.replace("%lotteryname%", cl.getLotteryName())
					.replace("%id%", String.valueOf(id)));
			return;
		}
		ClassicLottoDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`id` = ?", clt.getDrawID());
		String msg = "";
		if(cld == null)
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Repeat.DontExist")
					.replace("%lotteryname%", cl.getLotteryName())
					.replace("%id%", String.valueOf(id)));
			return;
		} else
		{
			if(cld.wasDrawn())
			{
				msg = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Repeat.TicketCreated");
				if(!EconomyHandler.hasBalance(player.getUniqueId(), cl.getCostPerTicket()))
				{
					MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NotEnoughMoney"));
					return;
				}
				ClassicLottoDraw cldNext = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
				if(cldNext == null)
				{
					MessageHandler.sendMessage(player.getUniqueId(), plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.LotteryNotOpen"));
					return;
				}
				MessageHandler.sendMessage(player, msg);
				plugin.getMysqlHandler().create(new ClassicLottoTicket(id, cldNext.getId(), cl.getLotteryName(),
						player.getUniqueId(), true, 0, 0, clt.getChoosenNumbers()));
				String cat = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.Category").replace("%lotteryname%", cl.getLotteryName());
				String comment = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.Comment");
				EconomyHandler.withdraw(player.getUniqueId(), cl.getCostPerTicket(), cat, comment);
				MessageHandler.sendMessage(player, 
						ARG_Play.replace(clt.getChoosenNumbers(), plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.TicketBought")));
				
				return;
			} else
			{
				final boolean repeat = clt.shouldRepeate();
				clt.setShouldRepeate(!repeat);
				plugin.getMysqlHandler().updateData(clt, "`id` = ?", id);
				msg = repeat 
						? plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Repeat.DontRepeat")
								.replace("%lotteryname%", cl.getLotteryName())
								.replace("%id%", String.valueOf(id))
						: plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Repeat.Repeat")
								.replace("%lotteryname%", cl.getLotteryName())
								.replace("%id%", String.valueOf(id));
			}
		}
		MessageHandler.sendMessage(player, msg);
	}
}