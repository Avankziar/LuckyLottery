package me.avankziar.lly.spigot.cmd.lottosuper;

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
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.general.objects.lottery.ticket.LottoSuperTicket;
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
	 * => /lottosuper ticketlist lotteryname [pagenumber]
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
		int count = plugin.getMysqlHandler().getCount(ls.getTicketMysql(), "`player_uuid` = ?", player.getUniqueId().toString());
		double pagecount = count/10.0;
		int lastpage = (int) Math.ceil(pagecount);
		ArrayList<LottoSuperTicket> lstA = plugin.getMysqlHandler().getList(ls.getTicketMysql(), "`id` DESC", 10*page, 10,
				"`player_uuid` = ?", player.getUniqueId().toString());
		if(lstA.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.NoTickets"));
			return;
		}
		ArrayList<String> msg = new ArrayList<>();
		msg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.Headline")
				.replace("%lotteryname%", ls.getLotteryName()));
		for(LottoSuperTicket lst : lstA)
		{
			LottoSuperDraw lsd = plugin.getMysqlHandler().getData(ls.getDrawMysql(), "`id` = ?", lst.getDrawID());
			if(lsd == null)
			{
				continue;
			}
			ArrayList<String> ccn = new ArrayList<>();
			lst.getChoosenNumbers()
			.stream()
			.forEach(x -> ccn.add(
					matchChoosenNumber(lsd.getChoosenNumbers(), x, ARG_Play.getSpacing(x, ls.getLastNumberToChooseFrom()))));
			ArrayList<String> ccna = new ArrayList<>();
			lst.getAdditionalChoosenNumbers()
			.stream()
			.forEach(x -> ccna.add(
					matchChoosenNumber(lsd.getAdditionalChoosenNumbers(), x, ARG_Play.getSpacing(x, ls.getAdditionalLastNumberToChooseFrom()))));
			String status = lsd.wasDrawn() 
					? (lst.getWinningClassLevel() > 0 
							? plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.StatusWon")
									.replace("%winningclass%", String.valueOf(lst.getWinningClassLevel()))
									.replace("%prize%", EconomyHandler.format(lst.getWinningPrize()))
									.replace("%numbers%", "["+String.join(", ", ccn)+"]")
									.replace("%additionalnumbers%", "["+String.join(", ", ccna)+"]")
							: plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.StatusDontWon")
							.replace("%numbers%", "["+String.join(", ", ccn)+"]")
							.replace("%additionalnumbers%", "["+String.join(", ", ccna)+"]"))
					: plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.StatusPending")
							.replace("%numbers%", "["+String.join(", ", ccn)+"]")
							.replace("%additionalnumbers%", "["+String.join(", ", ccna)+"]");
			String repeat = lst.shouldRepeate()
					? plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.DontRepeat")
							.replace("%classiclottorepeat%", CommandSuggest.getCmdString(Type.CLASSICLOTTO_REPEAT))
							.replace("%lotteryname%", ls.getLotteryName())
							.replace("%id%", String.valueOf(lst.getID()))
					: plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.Repeat")
							.replace("%classiclottorepeat%", CommandSuggest.getCmdString(Type.CLASSICLOTTO_REPEAT))
							.replace("%lotteryname%", ls.getLotteryName())
							.replace("%id%", String.valueOf(lst.getID()));
			msg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.TicketList.Listed")
					.replace("%repeat%", repeat)
					.replace("%id%", String.valueOf(lst.getID()))
					.replace("%status%", status)
					);
		}
		StringBuilder sb = new StringBuilder();
		if(page > 0 && page < lastpage)
		{
			sb.append(ChatApi.click(plugin.getYamlHandler().getLang().getString("Next"), "RUN_COMMAND",
					CommandSuggest.getCmdString(Type.CLASSICLOTTO_TICKETLIST) + (page-1)));
		}
		if(page > 0 && sb.length() > 0)
		{
			sb.append(plugin.getYamlHandler().getLang().getString("NextPast"));
		}
		if(page > 0)
		{
			sb.append(ChatApi.click(plugin.getYamlHandler().getLang().getString("Past"), "RUN_COMMAND",
					CommandSuggest.getCmdString(Type.CLASSICLOTTO_TICKETLIST) + (page+1)));
		}
		msg.add(sb.toString());
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