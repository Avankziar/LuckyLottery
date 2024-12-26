package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
import me.avankziar.lly.spigot.handler.lottery.ClassicLottoHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_Play extends ArgumentModule
{
	private LLY plugin;
	private ArgumentConstructor ac;
	
	public ARG_Play(ArgumentConstructor ac)
	{
		super(ac);
		this.ac = ac;
	}

	/**
	 * => /classiclotto play lotteryname numbers... -r|-repeat|-w|-wiederholen
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
		LinkedHashSet<Integer> choosenNumber = new LinkedHashSet<Integer>();
		if(cl.getMaximalAmountOfTicketWhichCanAPlayerBuy() > 0)
		{
			ClassicLottoDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
			int count = plugin.getMysqlHandler().getCount(new ClassicLottoTicket(cl.getLotteryName()),
					"`player_uuid` = ? AND `draw_id` = ?", player.getUniqueId().toString(), cld.getId());
			if(count >=  cl.getMaximalAmountOfTicketWhichCanAPlayerBuy())
			{
				MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.TooManyTickets")
						.replace("%lotteryname%", cl.getLotteryName())
						.replace("%amount%", String.valueOf(count))
						.replace("%maximum%", String.valueOf(cl.getMaximalAmountOfTicketWhichCanAPlayerBuy())));
			}
		}
		int i = 2;
		boolean repeat = false;
		boolean confirm = false;
		while(i >= args.length)
		{
			if(i >= args.length)
			{
				break;
			}
			if(MatchApi.isInteger(args[i]) && choosenNumber.size() < cl.getAmountOfChoosedNumber())
			{
				choosenNumber.add(Integer.valueOf(args[i]));
			}
			if(MatchApi.isBoolean(args[i]))
			{
				repeat = Boolean.getBoolean(args[i]);
			} else if("-r".equalsIgnoreCase(args[i]) || "repeat".equalsIgnoreCase(args[i])
					|| "-w".equalsIgnoreCase(args[i]) || "wiederholen".equalsIgnoreCase(args[i]))
			{
				repeat = true;
			}
			if("confirm".equalsIgnoreCase(args[i]) || "bestätigen".equalsIgnoreCase(args[i]))
			{
				confirm = true;
			}
			i++;
		}
		choosenNumber = ClassicLottoHandler.sortDrawnNumber(choosenNumber);
		if(choosenNumber.size() >= cl.getAmountOfChoosedNumber())
		{
			//Choosen all requiert number
			if(!confirm)
			{
				MessageHandler.sendMessage(player, 
						getCmdForConfirm(cl, choosenNumber, repeat,
								replace(choosenNumber, 
										plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.MayYouConfirm"))));
				return;
			}
			if(!EconomyHandler.hasBalance(player.getUniqueId(), cl.getCostPerTicket()))
			{
				MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NotEnoughMoney"));
				return;
			}
			String cat = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.Category");
			String comment = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.Comment");
			EconomyHandler.withdraw(player.getUniqueId(), cl.getCostPerTicket(), cat, comment);
			MessageHandler.sendMessage(player, 
					replace(choosenNumber, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.TicketBought")));
			return;
		}
		ArrayList<String> msg = new ArrayList<String>();
		if(choosenNumber.size() > 0)
		{
			msg.add(replace(ClassicLottoHandler.sortDrawnNumber(choosenNumber), 
					plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.AlreadyChoosenNumber")));
		}
		msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.ShouldRepeat")
				.replace("%shouldrepeat%", 
						getCmdForRepeat(cl, choosenNumber, repeat,
								(repeat ? plugin.getYamlHandler().getLang().getString("IsTrue")
										: plugin.getYamlHandler().getLang().getString("IsFalse")))));
		msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.ChooseNumber"));
		int start = cl.getFristNumberToChooseFrom();
		int end = cl.getLastNumberToChooseFrom();
		int k = 1;
		StringBuilder sb = new StringBuilder();
		for(int j = start; j <= end; j++)
		{
			if(choosenNumber.contains(j))
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasChoosen")
						.replace("%number%", getCmdForContainingNumbers(cl, choosenNumber, repeat, j)));
			} else
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasntNeutralChoosen")
						.replace("%number%", getCmdForNotContainingNumbers(cl, choosenNumber, repeat, j)));
			}
			if(j < end)
			{
				sb.append(plugin.getYamlHandler().getLang().getString("ClassicLotto..Arg.Play.Seperator"));
			}
			if(k == 10 || j == end)
			{
				msg.add(sb.toString());
				sb = new StringBuilder();
				k = 1;
			}
			k++;
		}
		MessageHandler.sendMessage(player, msg.toArray(new String[msg.size()]));
	}
	
	private String replace(LinkedHashSet<Integer> set, String r)
	{
		List<String> l = List.of();
		set.stream().forEach(x -> l.add(String.valueOf(x)));
		String s = r.replace("%choosennumber%", String.join(", ", l));
		return s;
	}
	
	private String getCmdForNotContainingNumbers(ClassicLotto cl, LinkedHashSet<Integer> set, boolean repeat, int j)
	{
		return getCmdForRepeat(cl, set, repeat, String.valueOf(j));
	}
	
	private String getCmdForContainingNumbers(ClassicLotto cl, LinkedHashSet<Integer> set, boolean repeat, int j)
	{
		LinkedHashSet<Integer> s = set;
		s.remove(j);
		return getCmdForRepeat(cl, s, repeat, String.valueOf(j));
	}
	
	private String getCmdForRepeat(ClassicLotto cl, LinkedHashSet<Integer> set,  boolean repeat, String s)
	{
		List<String> l = List.of();
		set.stream().forEach(x -> l.add(String.valueOf(x)));
		return "<click:run_command:'"+
				CommandSuggest.get(Type.CLASSICLOTTO_PLAY).getCommandString()+
				cl.getLotteryName()+ac.getName()+" "+String.join(" ", l)+
				String.valueOf(repeat)+
				"'>"+s+"</click>";
	}
	
	private String getCmdForConfirm(ClassicLotto cl, LinkedHashSet<Integer> set,  boolean repeat, String s)
	{
		List<String> l = List.of();
		set.stream().forEach(x -> l.add(String.valueOf(x)));
		return "<click:run_command:'"+
				CommandSuggest.get(Type.CLASSICLOTTO_PLAY).getCommandString()+
				cl.getLotteryName()+ac.getName()+" "+String.join(" ", l)+
				String.valueOf(repeat)+" confirm"+
				"'>"+s+"</click>";
	}
}