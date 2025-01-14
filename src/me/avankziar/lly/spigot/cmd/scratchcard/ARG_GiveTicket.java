package me.avankziar.lly.spigot.cmd.scratchcard;

import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.PlayerData;
import me.avankziar.lly.general.objects.lottery.ScratchCard;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
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
	 * => /scratchcard giveticket lotteryname [amount of tickets] [boolean:all scratched] [playername]
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws IOException 
	{
		task(sender, args);
	}
	
	private void task(CommandSender sender, String[] args)
	{
		String ssc = args[1];
		Optional<ScratchCard> osc = LotteryHandler.getScratchCard(ssc);
		if(osc.isEmpty())
		{
			MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("ScratchCard.NoScratchCardFound"));
			return;
		}
		ScratchCard sc = osc.get();
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
		boolean scratched = false;
		if(args.length >= 4)
		{
			if(MatchApi.isBoolean(args[3]))
			{
				scratched = Boolean.valueOf(args[3]);
			}
		}
		HashSet<UUID> onlineplayer = new HashSet<>();
		if(args.length >= 5)
		{
			PlayerData pd = plugin.getMysqlHandler().getData(new PlayerData(), "`player_name` = ?", args[4]);
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
		MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("ScratchCard.Arg.GiveTicket.Given")
				.replace("%lotteryname%", sc.getLotteryName())
				.replace("%value%", String.valueOf(amountOfTickets))
				.replace("%amount%", String.valueOf(onlineplayer.size())));
		for(UUID uuid : onlineplayer)
		{
			MessageHandler.sendMessage(uuid, 
					plugin.getYamlHandler().getLang().getString("ScratchCard.Arg.GiveTicket.Give")
					.replace("%player%", sender instanceof CommandSender ? "Console" : sender.getName())
					.replace("%amount%", String.valueOf(amountOfTickets))
					.replace("%lotteryname%", sc.getLotteryName()));
			for(int i = 0; i < amountOfTickets; i++)
			{
				me.avankziar.lly.spigot.cmd.scratchcard.ARG_Play.doGivedTickets(uuid, sc, scratched);
			}
		}
	}
}