package me.avankziar.lly.spigot.handler;

import java.util.Arrays;
import java.util.Collection;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.ChatApi;
import me.avankziar.lly.spigot.LLY;

public class MessageHandler 
{
	public static void sendMessage(CommandSender sender, String...array)
	{
		Arrays.asList(array).stream().forEach(x -> sender.spigot().sendMessage(ChatApi.tl(x)));
	}
	
	public static void sendMessage(UUID uuid, String...array)
	{
		Player player = Bukkit.getPlayer(uuid);
		if(player != null)
		{
			Arrays.asList(array).stream().forEach(x -> player.spigot().sendMessage(ChatApi.tl(x)));
			return;
		}
		if(LLY.getPlugin().getMtV() == null)
		{
			return;
		}
		LLY.getPlugin().getMtV().sendMessage(uuid, array);
	}
	
	public static void sendMessage(Collection<UUID> uuids, String...array)
	{
		uuids.stream().forEach(x -> sendMessage(x, array));
	}
	
	public static void sendMessage(String...array)
	{
		if(LLY.getPlugin().getMtV() != null)
		{
			LLY.getPlugin().getMtV().sendMessage(array);
			return;
		} else
		{
			Bukkit.getOnlinePlayers().stream().forEach(x -> sendMessage(x.getUniqueId(), array));
			return;
		}
	}
}