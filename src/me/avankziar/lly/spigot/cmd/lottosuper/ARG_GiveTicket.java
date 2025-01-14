package me.avankziar.lly.spigot.cmd.lottosuper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.PlayerData;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.general.objects.lottery.ticket.LottoSuperTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;
import me.avankziar.lly.spigot.handler.lottery.LottoSuperHandler;

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
	 * => /lottosuper giveticket lotteryname [amount of tickets]
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws IOException 
	{
		task(sender, args);
	}
	
	private void task(CommandSender sender, String[] args)
	{
		String scl = args[1];
		Optional<LottoSuper> ocl = LotteryHandler.getLottoSuper(scl);
		if(ocl.isEmpty())
		{
			MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("LottoSuper.NoLottoSuperFound"));
			return;
		}
		LottoSuper cl = ocl.get();
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
		if(args.length >= 4)
		{
			PlayerData pd = plugin.getMysqlHandler().getData(new PlayerData(), "`player_name` = ?", args[3]);
			if(pd == null)
			{
				MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("NoPlayerExist"));
				return;
			}
			onlineplayer.add(pd.getUUID());
		} else
		{
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
		}
		LottoSuperDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		if(cld == null)
		{
			cld = new LottoSuperDraw(0, cl.getLotteryName(), false, 0, cl.getStandartPot(), new LinkedHashSet<>(), new LinkedHashSet<>());
			plugin.getMysqlHandler().create(cld);
			cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		}
		MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.GiveTicket.Given")
				.replace("%lotteryname%", cl.getLotteryName())
				.replace("%value%", String.valueOf(amountOfTickets))
				.replace("%amount%", String.valueOf(onlineplayer.size())));
		for(UUID uuid : onlineplayer)
		{
			for(int i = 0; i < amountOfTickets; i++)
			{
				LinkedHashSet<Integer> choosenNumber = new LinkedHashSet<>();
				choosenNumber = LottoSuperHandler.sortDrawnNumber(LottoSuperHandler.drawLotteryNumber(cl.getFirstNumberToChooseFrom(),
						cl.getLastNumberToChooseFrom(), cl.getAmountOfChoosedNumber()));
				LinkedHashSet<Integer> additionalChoosenNumber = new LinkedHashSet<>();
				additionalChoosenNumber = LottoSuperHandler.sortDrawnNumber(LottoSuperHandler.drawLotteryNumber(
						cl.getAdditionalFirstNumberToChooseFrom(),
						cl.getAdditionalLastNumberToChooseFrom(), cl.getAdditionalAmountOfChoosenNumber()));
				LottoSuperTicket clt = new LottoSuperTicket(0, cld.getId(), cl.getLotteryName(), uuid, false, 0, 0.0, choosenNumber,
						additionalChoosenNumber);
				plugin.getMysqlHandler().create(clt);
				ArrayList<String> cn = new ArrayList<>();
				choosenNumber.stream().forEach(x -> cn.add(String.valueOf(x)));
				ArrayList<String> cna = new ArrayList<>();
				additionalChoosenNumber.stream().forEach(x -> cna.add(String.valueOf(x)));
				MessageHandler.sendMessage(uuid, 
						plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.GiveTicket.Give")
						.replace("%player%", sender instanceof CommandSender ? "Console" : sender.getName())
						.replace("%lotteryname%", cl.getLotteryName())
						.replace("%choosennumber%", String.join(" ", cn))
						.replace("%additionalchoosennumber%", String.join(" ", cna)));
			}
		}
	}
}