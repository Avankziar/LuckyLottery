package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.general.objects.lottery.ticket.ClassicLottoTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.ClassicLottoHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_GiveTicket extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_GiveTicket(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /classiclotto giveticket lotteryname [amount of tickets]
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
		int amountOfTickets = 1;
		if(args.length >= 3)
		{
			if(MatchApi.isInteger(args[2]))
			{
				amountOfTickets = Integer.valueOf(args[2]);
				if(amountOfTickets <= 0)
				{
					amountOfTickets = 1;
				}
			}
		}
		HashSet<UUID> onlineplayer = new HashSet<>();
		if(plugin.getProxyOnlinePlayers() != null)
		{
			plugin.getProxyOnlinePlayers().getProxyOnlinePlayers().keySet().stream()
				.forEach(x -> onlineplayer.add(x));
		} else
		{
			Bukkit.getOnlinePlayers().stream()
				.map(x -> x.getUniqueId())
				.forEach(x -> onlineplayer.add(x));
		}
		ClassicLottoDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		if(cld == null)
		{
			cld = new ClassicLottoDraw(0, cl.getLotteryName(), false, 0, cl.getStandartPot(), new LinkedHashSet<>());
			plugin.getMysqlHandler().create(cld);
			cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		}
		MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.GiveTicket.Given")
				.replace("%lotteryname%", cl.getLotteryName())
				.replace("%value%", String.valueOf(amountOfTickets))
				.replace("%amount%", String.valueOf(onlineplayer.size())));
		for(UUID uuid : onlineplayer)
		{
			for(int i = 0; i < amountOfTickets; i++)
			{
				LinkedHashSet<Integer> choosenNumber = new LinkedHashSet<>();
				choosenNumber = ClassicLottoHandler.sortDrawnNumber(ClassicLottoHandler.drawLotteryNumber(cl.getFirstNumberToChooseFrom(),
						cl.getLastNumberToChooseFrom(), cl.getAmountOfChoosedNumber()));
				ClassicLottoTicket clt = new ClassicLottoTicket(0, cld.getId(), cl.getLotteryName(), uuid, false, 0, 0.0, choosenNumber);
				plugin.getMysqlHandler().create(clt);
				ArrayList<String> cn = new ArrayList<>();
				choosenNumber.stream().forEach(x -> cn.add(String.valueOf(x)));
				MessageHandler.sendMessage(uuid, 
						plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.GiveTicket.Give")
						.replace("%player%", player.getName())
						.replace("%lotteryname%", cl.getLotteryName())
						.replace("%choosennumber%", String.join(" ", cn)));
			}
		}
	}
}