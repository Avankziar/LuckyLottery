package me.avankziar.lly.spigot.cmd.scratchcard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
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
		ScratchCard ls = ols.get();
		if(args.length <= 2 && !playingNumber.containsKey(player.getUniqueId()))
		{
			sendInfoMessageToScratch(player, ls);
			return;
		}
		if(!hasStarted(player.getUniqueId(), ls))
		{
			//Send Start Message
			if(!EconomyHandler.hasBalance(player.getUniqueId(), ls.getCostPerTicket()))
			{
				MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NotEnoughMoney"));
				return;
			}
			String cat = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Category").replace("%lotteryname%", ls.getLotteryName());
			String comment = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Comment");
			EconomyHandler.withdraw(player.getUniqueId(), ls.getCostPerTicket(), cat, comment);
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("ScratchCard.Arg.Play.TicketBought"));
			start(player.getUniqueId(), ls);
		}
		String value = args[2];
		if(value.startsWith("-a"))
		{
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			for(int i = 1; i <= ls.getAmountOfFields(); i--)
			{
				scratchField(player.getUniqueId(), ls, i);
			}
			iscf = getSet(player.getUniqueId());
			sendFinishMessage(player.getUniqueId(), ls, iscf);
			playingNumber.remove(player.getUniqueId());
			return;
		}
		if(MatchApi.isInteger(value))
		{
			scratchField(player.getUniqueId(), ls, Integer.valueOf(value));
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			sendMessageWithFieldToScratch(player, ls, iscf);
			return;
		} else if(allScratched(player.getUniqueId(), ls))
		{
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			sendFinishMessage(player.getUniqueId(), ls, iscf);
			playingNumber.remove(player.getUniqueId());
			return;
		} else
		{
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			sendMessageWithFieldToScratch(player, ls, iscf);
			return;
		}
	}
	
	public static void doGivedTickets(UUID uuid, ScratchCard sc)
	{
		start(uuid, sc);
		LinkedHashMap<Integer, ScratchCardField> iscf = getSet(uuid);
		for(int i = 1; i <= sc.getAmountOfFields(); i--)
		{
			scratchField(uuid, sc, i);
		}
		iscf = getSet(uuid);
		sendFinishMessage(uuid, sc, iscf);
		playingNumber.remove(uuid);
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
		if(map.isEmpty())
		{
			return false;
		}
		int c = 0;
		for(int i = 1; i <= sc.getAmountOfFields(); i++)
		{
			if(map.containsKey(i*-1))
			{
				c++;
			}
		}
		return c > 0 && c < sc.getAmountOfFields();
	}
	
	private static void start(UUID uuid, ScratchCard sc)
	{
		LinkedHashMap<Integer, ScratchCardField> map = getSet(uuid);
		for(int i = 1; i <= sc.getAmountOfFields(); i++)
		{
			map.put(i*-1, ScratchCardField.roll(sc.getScratchCardFields(), 50));
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
	
	private void sendMessageWithFieldToScratch(Player player, ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		ArrayList<String> msg = new ArrayList<>();
		msg.addAll(plugin.getYamlHandler().getLang().getStringList("ScratchCard.Arg.Play.ScratchInfo"));
		msg.addAll(sendScratchFields(sc, map));
		MessageHandler.sendMessage(player, msg.toArray(new String[msg.size()]));
	}
	
	private static void sendFinishMessage(UUID uuid, ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		ArrayList<String> msg = new ArrayList<>();
		ArrayList<String> samefield = new ArrayList<>();
		double winamount = 0.0;
		ArrayList<Double> fields = new ArrayList<>();
		HashSet<Integer> alreadycheck = new HashSet<>();
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
			ArrayList<ScratchCardPayout> scpA = new ArrayList<>();
			ScratchCardPayout highestscp = null;
			for(Entry<Integer, ScratchCardField> e : map.entrySet())
			{
				if(e.getValue().getWinningAmount() == sc.getJoker().getWinningAmount())
				{
					continue;
				}
				if(!alreadycheck.contains(e.getKey()))
				{
					alreadycheck.add(e.getKey());
					int c = 1;
					for(Entry<Integer, ScratchCardField> ee : map.entrySet())
					{
						if(ee.getValue().getWinningAmount() == sc.getJoker().getWinningAmount())
						{
							continue;
						}
						if(alreadycheck.contains(ee.getKey()))
						{
							continue;
						}
						alreadycheck.add(ee.getKey());
						if(e.getValue().getWinningAmount() != ee.getValue().getChance())
						{
							continue;
						}
						c++;
					}
					int d = c - sc.getAmountOfSameFieldToWin() + 1;
					ScratchCardPayout scp = new ScratchCardPayout(e.getValue(), d);
					if(d > 0)
					{
						hasWon = true;
						scpA.add(scp);
						if(highestscp == null 
								|| highestscp.getScratchCardField().getWinningAmount() < e.getValue().getWinningAmount())
						{
							highestscp = scp;
							
						}
					}
					if(d <= 0 && jokerIndices.size() > 0)
					{
						if(highestscp == null 
								|| highestscp.getScratchCardField().getWinningAmount() < e.getValue().getWinningAmount())
						{	
							hasWon = true;
							highestscp = scp;
						}
					}					
				}
				fields.add(e.getValue().getWinningAmount());
			}
			if(hasWon)
			{
				int d = highestscp.getAmountDrawn() + jokerIndices.size();
				double result = d * highestscp.getScratchCardField().getWinningAmount();
				winamount += result;
				samefield.add(LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Arg.Play.FieldWin")
						.replace("%d%", String.valueOf(d))
						.replace("%amount%", String.valueOf(highestscp.getScratchCardField().getDisplay()))
						.replace("%result%", EconomyHandler.format(result)));
				for(ScratchCardPayout scp : scpA)
				{
					if(scp.getScratchCardField().getWinningAmount() == highestscp.getScratchCardField().getWinningAmount())
					{
						continue;
					}
					d = scp.getAmountDrawn();
					result = d * scp.getScratchCardField().getWinningAmount();
					winamount += result;
					samefield.add(LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Arg.Play.FieldWin")
							.replace("%d%", String.valueOf(d))
							.replace("%amount%", String.valueOf(scp.getScratchCardField().getDisplay()))
							.replace("%result%", EconomyHandler.format(result)));
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
		} else
		{
			msg.add(LLY.getPlugin().getYamlHandler().getLang().getString("ScratchCard.Arg.Play.NotWon"));
			MessageHandler.sendMessage(uuid, msg.toArray(new String[msg.size()]));
		}
		ScratchCardTicket sct = new ScratchCardTicket(0, sc.getLotteryName(), uuid, winamount, fields);
		LLY.getPlugin().getMysqlHandler().create(sct);
	}
	
	private ArrayList<String> sendScratchFields(ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		for(Entry<Integer, ScratchCardField> e : map.entrySet())
		{
			LLY.log.info("i: "+e.getKey()+" | "+e.getValue().getDisplay()+" | "+e.getValue().getWinningAmount());//REMOVEME
		}
		ArrayList<String> msg = new ArrayList<>();
		int c = sc.getFieldPerLine();
		int d = 0;
		ArrayList<StringBuilder> sbA = new ArrayList<>();
		for(int i =1 ; i <= sc.getAmountOfFields(); i++)
		{
			if(d >= c)
			{
				d = 0;
				sbA.forEach(x -> msg.add(x.toString()));
				sbA = new ArrayList<>();
			}
			int ii = i*-1;
			if(map.containsKey(ii))
			{
				int a = 0;
				for(String s : sc.getDisplayFieldUnscratched())
				{
					StringBuilder sb = new StringBuilder();
					if(sbA.size() > a)
					{
						sb = sbA.get(a);
						sb.append(getCmdScratchField(sc, String.valueOf(i), s));
						sbA.set(a, sb);
					} else
					{
						sb.append(getCmdScratchField(sc, String.valueOf(i), s));
						sbA.add(sb);
					}
					a++;
				}
			} else
			{
				ScratchCardField scf = map.get(i);
				int a = 0;
				for(String s : sc.getDisplayFieldScratched())
				{
					StringBuilder sb = new StringBuilder();
					if(sbA.size() > a)
					{
						sb = sbA.get(a);
						sb.append(s.replace("X", scf.getDisplay()));
						sbA.set(a, sb);
					} else
					{
						sb.append(s.replace("X", scf.getDisplay()));
						sbA.add(sb);
					}
					a++;
				}
			}
			if((i+1) >= sc.getAmountOfFields())
			{
				sbA.forEach(x -> msg.add(x.toString()));
				sbA = new ArrayList<>();
				break;
			}
			d++;
		}
		return msg;
	}
	
	private String getCmdScratchField(ScratchCard sc, String number, String display)
	{
		return "<click:run_command:'"+
				CommandSuggest.get(Type.SCRATCHCARD_PLAY).getCommandString()+
				sc.getLotteryName()+" "+number+
				"'>"+display+"</click>";
	}
}