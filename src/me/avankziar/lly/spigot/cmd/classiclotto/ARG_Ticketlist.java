package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.ChatApi;
import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.cmdtree.CommandSuggest.Type;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.general.objects.lottery.ticket.ClassicLottoTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_Ticketlist extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_Ticketlist(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /classiclotto giveticket lotteryname [pagenumber]
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
		int page = 0;
		if(args.length >= 3)
		{
			if(MatchApi.isInteger(args[2]))
			{
				page = Integer.valueOf(args[2]);
				if(page < 0)
				{
					page = 0;
				}
			}
		}
		int count = plugin.getMysqlHandler().getCount(cl.getTicketMysql(), "`player_uuid` = ?", player.getUniqueId().toString());
		double pagecount = count/10.0 - 1;
		int lastpage = (int) Math.ceil(pagecount);
		ArrayList<ClassicLottoTicket> cltA = plugin.getMysqlHandler().getList(cl.getTicketMysql(), "`id` DESC", 10*page, 10,
				"`player_uuid` = ?", player.getUniqueId().toString());
		if(cltA.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.NoTickets"));
			return;
		}
		ArrayList<String> msg = new ArrayList<>();
		msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.Headline")
				.replace("%lotteryname%", cl.getLotteryName()));
		int lastwinningclasslevel = cl.getWinningClass().size()+1;
		for(ClassicLottoTicket clt : cltA)
		{
			ClassicLottoDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`id` = ?", clt.getDrawID());
			if(cld == null)
			{
				continue;
			}
			ArrayList<String> ccn = new ArrayList<>();
			clt.getChoosenNumbers()
			.stream()
			.forEach(x -> ccn.add(
					matchChoosenNumber(cld.getChoosenNumbers(), x, ARG_Play.getSpacing(x, cl.getLastNumberToChooseFrom()))));
			String status = cld.wasDrawn() 
					? (clt.getWinningClassLevel() < lastwinningclasslevel 
							? plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.StatusWon")
									.replace("%winningclass%", String.valueOf(clt.getWinningClassLevel()))
									.replace("%prize%", EconomyHandler.format(clt.getWinningPrize()))
									.replace("%numbers%", "["+String.join(", ", ccn)+"]")
							: plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.StatusDontWon")
							.replace("%numbers%", "["+String.join(", ", ccn)+"]")) 
					: plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.StatusPending")
							.replace("%numbers%", "["+String.join(", ", ccn)+"]");
			String repeat = clt.shouldRepeate()
					? plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.DontRepeat")
							.replace("%classiclottorepeat%", CommandSuggest.getCmdString(Type.CLASSICLOTTO_REPEAT))
							.replace("%lotteryname%", cl.getLotteryName())
							.replace("%id%", String.valueOf(clt.getID()))
					: plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.Repeat")
							.replace("%classiclottorepeat%", CommandSuggest.getCmdString(Type.CLASSICLOTTO_REPEAT))
							.replace("%lotteryname%", cl.getLotteryName())
							.replace("%id%", String.valueOf(clt.getID()));
			msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.TicketList.Listed")
					.replace("%repeat%", repeat)
					.replace("%id%", String.valueOf(clt.getID()))
					.replace("%status%", status)
					);
		}
		StringBuilder sb = new StringBuilder();
		if(page > 0)
		{
			sb.append(ChatApi.click(plugin.getYamlHandler().getLang().getString("Past"), "RUN_COMMAND",
					CommandSuggest.getCmdString(Type.CLASSICLOTTO_TICKETLIST) + cl.getLotteryName() + " " + (page-1)));
		}
		if(page >= 0 && page < lastpage && sb.length() > 0)
		{
			sb.append(plugin.getYamlHandler().getLang().getString("NextPast"));
		}
		if(page >= 0 && page < lastpage)
		{
			sb.append(ChatApi.click(plugin.getYamlHandler().getLang().getString("Next"), "RUN_COMMAND",
					CommandSuggest.getCmdString(Type.CLASSICLOTTO_TICKETLIST) + cl.getLotteryName() + " " + (page+1)));
		}
		if(!sb.toString().isEmpty())
		{
			msg.add(sb.toString());
		}	
		MessageHandler.sendMessage(player, msg.toArray(new String[msg.size()]));
	}
	
	public static String matchChoosenNumber(LinkedHashSet<Integer> set, int i, String s)
	{
		return set.contains(i) 
				? LLY.getPlugin().getYamlHandler().getLang().getString("WasChoosen")
						.replace("%number%", s)
				: LLY.getPlugin().getYamlHandler().getLang().getString("WasntChoosen")
						.replace("%number%", s); 
	}
}