package me.avankziar.lly.spigot.cmd.scratchcard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.cmdtree.CommandSuggest;
import me.avankziar.lly.general.cmdtree.CommandSuggest.Type;
import me.avankziar.lly.general.objects.ScratchCardField;
import me.avankziar.lly.general.objects.lottery.ScratchCard;
import me.avankziar.lly.general.objects.lottery.payout.ScratchCardPayout;
import me.avankziar.lly.general.objects.lottery.ticket.ScratchCardTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_Play extends ArgumentModule
{
	private LLY plugin;
	private static LinkedHashMap<UUID, LinkedHashMap<Integer, ScratchCardField>> playingNumber = new LinkedHashMap<>();
	
	public ARG_Play(ArgumentConstructor ac)
	{
		super(ac);
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /scratchcard play lotteryname [fieldnumber|-a]
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
		Optional<ScratchCard> ols = LotteryHandler.getScratchCard(sls);
		if(ols.isEmpty())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.NoLottoSuperFound"));
			return;
		}
		ScratchCard sc = ols.get();
		if(args.length <= 2 && !playingNumber.containsKey(player.getUniqueId()))
		{
			sendInfoMessageToScratch(player, sc);
			return;
		}
		if(!hasStarted(player.getUniqueId(), sc))
		{
			//Send Start Message
			if(!EconomyHandler.hasBalance(player.getUniqueId(), sc.getCostPerTicket()))
			{
				MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NotEnoughMoney"));
				return;
			}
			String cat = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Category").replace("%lotteryname%", sc.getLotteryName());
			String comment = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Comment");
			EconomyHandler.withdraw(player.getUniqueId(), sc.getCostPerTicket(), cat, comment);
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ScratchCard.Arg.Play.TicketBought"));
			start(player.getUniqueId(), sc);
		}
		String value = "b";
		if(args.length >= 3)
		{
			value = args[2];
		}
		 
		if(value.startsWith("-a"))
		{
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			for(int i = 1; i <= sc.getAmountOfFields(); i++)
			{
				scratchField(player.getUniqueId(), sc, i);
			}
			sendMessageWithFieldToScratch(player.getUniqueId(), sc, iscf, false);
			iscf = getSet(player.getUniqueId());
			sendFinishMessage(player.getUniqueId(), sc, iscf);
			playingNumber.remove(player.getUniqueId());
			return;
		}
		if(MatchApi.isInteger(value))
		{
			scratchField(player.getUniqueId(), sc, Integer.valueOf(value));
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			sendMessageWithFieldToScratch(player.getUniqueId(), sc, iscf, true);
		} else
		{
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			sendMessageWithFieldToScratch(player.getUniqueId(), sc, iscf, true);
		}
		if(allScratched(player.getUniqueId(), sc))
		{
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			sendFinishMessage(player.getUniqueId(), sc, iscf);
			playingNumber.remove(player.getUniqueId());
			return;
		}
	}
	
	public static void doGivedTickets(UUID uuid, ScratchCard sc, boolean scratched)
	{
		start(uuid, sc);
		LinkedHashMap<Integer, ScratchCardField> iscf = getSet(uuid);
		if(scratched)
		{
			for(int i = 1; i <= sc.getAmountOfFields(); i++)
			{
				scratchField(uuid, sc, i);
			}
			iscf = getSet(uuid);
			sendMessageWithFieldToScratch(uuid, sc, iscf, false);
			sendFinishMessage(uuid, sc, iscf);
			playingNumber.remove(uuid);
			
		} else
		{
			iscf = getSet(uuid);
			sendMessageWithFieldToScratch(uuid, sc, iscf, true);
		}
	}
	
	private static LinkedHashMap<Integer, ScratchCardField> getSet(UUID uuid)
	{
		if(playingNumber.containsKey(uuid))
		{
			return playingNumber.get(uuid);
		}
		LinkedHashMap<Integer, ScratchCardField> map = new LinkedHashMap<>();
		playingNumber.put(uuid, map);
		return map;
	}
	
	private boolean hasStarted(UUID uuid, ScratchCard sc)
	{
		LinkedHashMap<Integer, ScratchCardField> map = getSet(uuid);
		return !map.isEmpty();
	}
	
	private static void start(UUID uuid, ScratchCard sc)
	{
		LinkedHashMap<Integer, ScratchCardField> map = getSet(uuid);
		for(int i = 1; i <= sc.getAmountOfFields(); i++)
		{
			map.put(i*-1, ScratchCardField.roll(sc.getScratchCardFields(), 0));
		}
	}
	
	private static void scratchField(UUID uuid, ScratchCard sc, int i)
	{
		LinkedHashMap<Integer, ScratchCardField> iscf = getSet(uuid);
		if(!iscf.containsKey(i))
		{
			iscf.put(i, iscf.get(i*-1));
			iscf.remove(i*-1);
		}
		playingNumber.put(uuid, iscf);
	}
	
	private boolean allScratched(UUID uuid, ScratchCard sc)
	{
		LinkedHashMap<Integer, ScratchCardField> map = getSet(uuid);
		int c = 0;
		for(int i = 1; i <= sc.getAmountOfFields(); i++)
		{
			if(map.containsKey(i))
			{
				c++;
			}
		}
		return c >= sc.getAmountOfFields();
	}
	
	private void sendInfoMessageToScratch(Player player, ScratchCard sc)
	{
		ArrayList<String> msg = new ArrayList<String>();
		for(String s : plugin.getYamlHandler().getLang().getStringList("ScratchCard.Arg.Play.Info"))
		{
			msg.add(s.replace("%scratchcardbet%", CommandSuggest.getCmdString(Type.SCRATCHCARD_PLAY))
					.replace("%lotteryname%", sc.getLotteryName() + " b")
					.replace("%costperticket%", EconomyHandler.format(sc.getCostPerTicket())));
		}
		MessageHandler.sendMessage(player, msg.toArray(new String[msg.size()]));
	}
	
	private static void sendMessageWithFieldToScratch(UUID uuid, ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map, boolean info)
	{
		ArrayList<String> msg = new ArrayList<>();
		if(info)
		{
			msg.addAll(LLY.getPlugin().getYamlHandler().getLang().getStringList("ScratchCard.Arg.Play.ScratchInfo"));
		}
		msg.addAll(sendScratchFields(sc, map));
		MessageHandler.sendMessage(uuid, msg.toArray(new String[msg.size()]));
	}
	
	private static void sendFinishMessage(UUID uuid, ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		ArrayList<String> msg = new ArrayList<>();
		ArrayList<String> samefield = new ArrayList<>();
		double winamount = 0.0;
		ArrayList<Double> fields = new ArrayList<>();
		List<Integer> jokerIndices = new ArrayList<>();
		boolean hasWon = false;
		for (Entry<Integer, ScratchCardField> e : map.entrySet()) 
		{
		    if (e.getValue().getWinningAmount() == -1) 
		    {
		        jokerIndices.add(e.getKey());
		    }
		}
		if(jokerIndices.size() >= sc.getAmountOfFields())
		{
			//Jackpot x amount Of Fields
			int d = jokerIndices.size();
			double result = d * sc.getJackpot().getWinningAmount();
			winamount += result;
			samefield.add(LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Arg.Play.FieldWin")
					.replace("%d%", String.valueOf(d))
					.replace("%amount%", String.valueOf(sc.getJackpot().getDisplay()))
					.replace("%result%", EconomyHandler.format(result)));
			for(String s : LLY.getPlugin().getYamlHandler().getLang().getStringList("ScratchCard.Arg.Play.Won"))
			{
				msg.add(s.replace("%winamount%", EconomyHandler.format(winamount)));
			}
			MessageHandler.sendMessage(uuid, msg.toArray(new String[msg.size()]));
			if(samefield.size() > 0)
			{
				MessageHandler.sendMessage(uuid, samefield.toArray(new String[samefield.size()]));
			}
			jokerIndices.forEach(x -> fields.add(-1.0));
			ScratchCardTicket sct = new ScratchCardTicket(0, sc.getLotteryName(), uuid, winamount, fields);
			LLY.getPlugin().getMysqlHandler().create(sct);
			return;
		} else
		{
			LinkedHashMap<Double, ScratchCardPayout> scpA = new LinkedHashMap<>();
			ScratchCardPayout highestscp = null;
			for(Entry<Integer, ScratchCardField> e : map.entrySet())
			{
				fields.add(e.getValue().getWinningAmount());
				if(e.getValue().getWinningAmount() == sc.getJoker().getWinningAmount())
				{
					continue;
				}
				int c = 1;
				ScratchCardPayout scp = new ScratchCardPayout(e.getValue(), c);
				if(scpA.containsKey(e.getValue().getWinningAmount()))
				{
					scp = scpA.get(e.getValue().getWinningAmount());
					scp.setAmountDrawn(scp.getAmountDrawn()+1);
				}
				if(highestscp == null 
						|| highestscp.getScratchCardField().getWinningAmount() <= e.getValue().getWinningAmount())
				{
					highestscp = scp;	
				}
				scpA.put(e.getValue().getWinningAmount(), scp);
			}
			int d = highestscp.getAmountDrawn() + jokerIndices.size() - sc.getAmountOfSameFieldToWin() + 1;
			if(d > 0)
			{
				double result = d * highestscp.getScratchCardField().getWinningAmount();
				winamount += result;
				samefield.add(LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Arg.Play.FieldWin")
						.replace("%d%", String.valueOf(highestscp.getAmountDrawn()+jokerIndices.size()))
						.replace("%asf%", String.valueOf(sc.getAmountOfSameFieldToWin()))
						.replace("%amount%", String.valueOf(highestscp.getScratchCardField().getDisplay()))
						.replace("%result%", EconomyHandler.format(result)));
				hasWon = true;
			}
			for(ScratchCardPayout scp : scpA.values())
			{
				if(scp.getScratchCardField().getWinningAmount() == highestscp.getScratchCardField().getWinningAmount())
				{
					continue;
				}
				int dd = scp.getAmountDrawn() - sc.getAmountOfSameFieldToWin() + 1;
				if(dd > 0)
				{
					double result = dd * scp.getScratchCardField().getWinningAmount();
					winamount += result;
					samefield.add(LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Arg.Play.FieldWin")
							.replace("%d%", String.valueOf(scp.getAmountDrawn()))
							.replace("%asf%", String.valueOf(sc.getAmountOfSameFieldToWin()))
							.replace("%amount%", String.valueOf(scp.getScratchCardField().getDisplay()))
							.replace("%result%", EconomyHandler.format(result)));
					hasWon = true;
				}
			}
		}
		if(hasWon)
		{
			for(String s : LLY.getPlugin().getYamlHandler().getLang().getStringList("ScratchCard.Arg.Play.Won"))
			{
				msg.add(s.replace("%winamount%", EconomyHandler.format(winamount)));
			}
			MessageHandler.sendMessage(uuid, msg.toArray(new String[msg.size()]));
			if(samefield.size() > 0)
			{
				MessageHandler.sendMessage(uuid, samefield.toArray(new String[samefield.size()]));
			}
			String cat = LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Win.Category")
					.replace("%lotteryname%", sc.getLotteryName());
			String comment = LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Win.Comment");
			EconomyHandler.deposit(uuid, winamount, cat, comment);
		} else
		{
			msg.add(LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Arg.Play.NotWon"));
			MessageHandler.sendMessage(uuid, msg.toArray(new String[msg.size()]));
		}
		ScratchCardTicket sct = new ScratchCardTicket(0, sc.getLotteryName(), uuid, winamount, fields);
		LLY.getPlugin().getMysqlHandler().create(sct);
	}
	
	private static ArrayList<String> sendScratchFields(ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		ArrayList<String> msg = new ArrayList<>();
		msg.add(sc.getDisplayHeadLine());
		int c = sc.getFieldPerLine();
		int d = 0;
		ArrayList<StringBuilder> sbA = new ArrayList<>();
		for(int i = 1 ; i <= sc.getAmountOfFields(); i++)
		{
			if(d >= c)
			{
				d = 0;
				sbA.forEach(x -> msg.add(x.toString()));
				sbA = new ArrayList<>();
				msg.add(sc.getDisplayBetweenLine());
			}
			int ii = i*-1;
			if(map.containsKey(ii))
			{
				StringBuilder sb = new StringBuilder();
				if(sbA.size() > 0)
				{
					sb = sbA.get(0);
					sb.append(getCmdScratchField(sc, String.valueOf(i), sc.getDisplayFieldUnscratched()));
					sbA.set(0, sb);
				} else
				{
					sb.append(getCmdScratchField(sc, String.valueOf(i), sc.getDisplayFieldUnscratched()));
					sbA.add(sb);
				}
			} else
			{
				ScratchCardField scf = map.get(i);
				StringBuilder sb = new StringBuilder();
				if(sbA.size() > 0)
				{
					sb = sbA.get(0);
					sb.append(sc.getDisplayFieldScratched().replace("X", scf.getDisplay()));
					sbA.set(0, sb);
				} else
				{
					sb.append(sc.getDisplayFieldScratched().replace("X", scf.getDisplay()));
					sbA.add(sb);
				}
			}
			if((i+1) > sc.getAmountOfFields())
			{
				sbA.forEach(x -> msg.add(x.toString()));
				sbA = new ArrayList<>();
				break;
			}
			d++;
		}
		msg.add(sc.getDisplayBottomLine());
		return msg;
	}
	
	private static String getCmdScratchField(ScratchCard sc, String number, String display)
	{
		return "<click:run_command:'"+
				CommandSuggest.get(Type.SCRATCHCARD_PLAY).getCommandString()+
				sc.getLotteryName()+" "+number+
				"'>"+display+"</click>";
	}
}