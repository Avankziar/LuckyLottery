package me.avankziar.lly.spigot.cmd;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import me.avankziar.lly.general.assistance.ChatApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.cmdtree.CommandConstructor;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.ModifierValueEntry.ModifierValueEntry;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;
import me.avankziar.lly.spigot.handler.lottery.LottoSuperHandler;

public class LottoSuperCommandExecutor implements CommandExecutor
{
	private LLY plugin;
	private static CommandConstructor cc;
	
	public LottoSuperCommandExecutor(LLY plugin, CommandConstructor cc)
	{
		this.plugin = plugin;
		LottoSuperCommandExecutor.cc = cc;
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String lable, String[] args) 
	{
		if(cc == null)
		{
			return false;
		}
		if (!(sender instanceof Player)) 
		{
			plugin.getLogger().info("Cmd is only for Player!");
			return false;
		}
		if(args.length == 0)
		{
			Player player = (Player) sender;
			new BukkitRunnable() 
			{
				@Override
				public void run() 
				{
					getLottoSuperInfo(player);
				}
			}.runTaskAsynchronously(plugin);
			return true;
		}
		if(args.length == 1)
		{
			Player player = (Player) sender;
			Optional<LottoSuper> ocl = LotteryHandler.getLottoSuper(args[0]);
			if(ocl.isPresent())
			{
				LottoSuper cl = ocl.get();
				new BukkitRunnable() 
				{
					@Override
					public void run() 
					{
						getLottoSuperInfo(player, cl);
					}
				}.runTaskAsynchronously(plugin);
				return true;
			}
		}
		int length = args.length-1;
		ArrayList<ArgumentConstructor> aclist = cc.subcommands;
		for(int i = 0; i <= length; i++)
		{
			for(ArgumentConstructor ac : aclist)
			{
				if(args[i].equalsIgnoreCase(ac.getName()))
				{
					if(length >= ac.minArgsConstructor && length <= ac.maxArgsConstructor)
					{
						if (sender instanceof Player)
						{
							Player player = (Player) sender;
							if(ModifierValueEntry.hasPermission(player, ac))
							{
								ArgumentModule am = plugin.getArgumentMap().get(ac.getPath());
								if(am != null)
								{
									try
									{
										am.run(sender, args);
									} catch (IOException e)
									{
										e.printStackTrace();
									}
								} else
								{
									plugin.getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName()));
									MessageHandler.sendMessage(player, 
											"ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
											.replace("%ac%", ac.getName()));
									return false;
								}
								return false;
							} else
							{
								MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NoPermission"));
								return false;
							}
						} else
						{
							ArgumentModule am = plugin.getArgumentMap().get(ac.getPath());
							if(am != null)
							{
								new BukkitRunnable() 
								{
									@Override
									public void run() 
									{
										try
										{
											am.run(sender, args);
										} catch (IOException e)
										{
											e.printStackTrace();
										}
									}
								}.runTaskAsynchronously(plugin);
							} else
							{
								plugin.getLogger().info("ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
										.replace("%ac%", ac.getName()));
								MessageHandler.sendMessage(sender, 
										"ArgumentModule from ArgumentConstructor %ac% not found! ERROR!"
										.replace("%ac%", ac.getName()));
								return false;
							}
							return false;
						}
					} else
					{
						aclist = ac.subargument;
						break;
					}
				}
			}
		}
		MessageHandler.sendMessage(sender, ChatApi.click(plugin.getYamlHandler().getLang().getString("InputIsWrong"),
				"RUN_COMMAND", CommandSuggest.getCmdString(CommandSuggest.Type.LLY)));
		return false;
	}
	
	private void getLottoSuperInfo(Player player)
	{
		ArrayList<String> msg = new ArrayList<>();
		msg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Cmd.Headline"));
		for(LottoSuper cl : LotteryHandler.getLottoSuper())
		{
			LottoSuperDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
			if(cld == null)
			{
				player.sendMessage("CL "+cl.getLotteryName()+" Draw == null");
				continue;
			}
			ArrayList<String> l = LottoSuperHandler.replacer(cl, cld, null, null, null, null, false,
					(ArrayList<String>) plugin.getYamlHandler().getLang().getStringList("LottoSuper.Cmd.GeneralInfo"));
			msg.addAll(l);
		}
		msg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Cmd.BottomLine"));
		MessageHandler.sendMessage(player.getUniqueId(), msg.toArray(new String[msg.size()]));
	}
	
	private void getLottoSuperInfo(Player player, final LottoSuper cl)
	{
		ArrayList<String> msg = new ArrayList<>();
		LottoSuperDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		if(cld == null)
		{
			return;
		}
		ArrayList<String> l = LottoSuperHandler.replacer(cl, cld, null, null, null, null, false,
				(ArrayList<String>) plugin.getYamlHandler().getLang().getStringList("LottoSuper.Cmd.DetailInfo"));
		msg.addAll(l);
		MessageHandler.sendMessage(player.getUniqueId(), msg.toArray(new String[msg.size()]));
	}
}