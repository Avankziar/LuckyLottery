package me.avankziar.lly.spigot.cmd.scratchcard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
import me.avankziar.lly.general.objects.lottery.ticket.ScratchCardTicket;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;
import me.avankziar.lly.spigot.handler.lottery.LottoSuperHandler;

public class ARG_Play extends ArgumentModule
{
	private LLY plugin;
	private LinkedHashMap<UUID, LinkedHashMap<Integer, ScratchCardField>> playingNumber = new LinkedHashMap<>();
	
	public ARG_Play(ArgumentConstructor ac)
	{
		super(ac);
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /scratchcard play lotteryname [fieldnumber|-a|-start]
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
			sendInfoMessageToScratch(player, ls, getSet(player.getUniqueId()));
			return;
		}
		String value = args[2];
		if(value.startsWith("-a"))
		{
			if(!hasStarted(player.getUniqueId(), ls))
			{
				start(player.getUniqueId(), ls);
			}
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			for(int i = 1; i <= ls.getAmountOfFields(); i--)
			{
				scratchField(player.getUniqueId(), ls, i);
			}
			iscf = getSet(player.getUniqueId());
			sendFinishMessage(player, ls, iscf);
			return;
		}
		if(!playingNumber.containsKey(player.getUniqueId()))
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
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.TicketBought"));
			start(player.getUniqueId(), ls);
			LinkedHashMap<Integer, ScratchCardField> iscf = getSet(player.getUniqueId());
			sendMessageWithFieldToScratch(player, ls, iscf);
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
			sendFinishMessage(player, ls, iscf);
			playingNumber.remove(player.getUniqueId());
			return;
		}
	}
	
	private LinkedHashMap<Integer, ScratchCardField> getSet(UUID uuid)
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
	
	private void start(UUID uuid, ScratchCard sc)
	{
		LinkedHashMap<Integer, ScratchCardField> map = getSet(uuid);
		for(int i = 1; i <= sc.getAmountOfFields(); i++)
		{
			map.put(i*-1, ScratchCardField.roll(sc.getScratchCardFields(), 50));
		}
	}
	
	private void scratchField(UUID uuid, ScratchCard sc, int i)
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
	
	public static String replace(LinkedHashSet<Integer> set, LinkedHashSet<Integer> aset, String r)
	{
		LinkedHashSet<Integer> sets = LottoSuperHandler.sortDrawnNumber(set);
		ArrayList<String> l = new ArrayList<>();
		sets.stream().forEach(x -> l.add(String.valueOf(x)));
		LinkedHashSet<Integer> asets = LottoSuperHandler.sortDrawnNumber(set);
		ArrayList<String> al = new ArrayList<>();
		asets.stream().forEach(x -> al.add(String.valueOf(x)));
		String s = r;
		if(set.size() > 0 && asets.size() > 0)
		{
			s = s.replace("%choosennumber%", String.join(", ", l)).replace("%additionalchoosennumber%", String.join(", ", l));
		} else if(set.size() > 0 && asets.size() == 0)
		{
			s = s.replace("%choosennumber%", String.join(", ", l)).replace("%additionalchoosennumber%", "/");
		} else if(set.size() == 0 && asets.size() > 0)
		{
			s = s.replace("%choosennumber%", "/").replace("%additionalchoosennumber%", String.join(", ", l));
		} else
		{
			s = s.replace("%choosennumber%", "/").replace("%additionalchoosennumber%", "/");
		}
		return s;
	}
	
	private void sendInfoMessageToScratch(Player player, ScratchCard cl, LinkedHashMap<Integer, ScratchCardField> clt)
	{
		ArrayList<String> msg = new ArrayList<String>();
		if(clt.getChoosenNumbers().size() > 0)
		{
			msg.add(replace(LottoSuperHandler.sortDrawnNumber(clt.getChoosenNumbers()), 
					LottoSuperHandler.sortDrawnNumber(clt.getAdditionalChoosenNumbers()), 
					plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.AlreadyChoosenNumber")));
		} else
		{
			msg.add(getCmdForRandom(cl, "random", plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.RandomChoose")));
		}
		msg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.ShouldRepeat")
				.replace("%shouldrepeat%", 
						getCmdForRepeat(cl, !clt.shouldRepeate(),
								(clt.shouldRepeate() 
										? plugin.getYamlHandler().getLang().getString("IsTrue")
										: plugin.getYamlHandler().getLang().getString("IsFalse")))));
		msg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.ChooseNumber")
				.replace("%amountofchoosennumber%", String.valueOf(cl.getAmountOfChoosedNumber())));
		int start = cl.getFirstNumberToChooseFrom();
		int end = cl.getLastNumberToChooseFrom();
		int k = 1;
		StringBuilder sb = new StringBuilder();
		for(int j = start; j <= end; j++)
		{
			if(clt.getChoosenNumbers().contains(j))
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasChoosen")
						.replace("%number%", getCmdForContainingNumbers(cl, "a"+getSpacing(j, cl.getLastNumberToChooseFrom()))));
			} else
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasntNeutralChoosen")
						.replace("%number%", getCmdForContainingNumbers(cl, "a"+getSpacing(j, cl.getLastNumberToChooseFrom()))));
			}
			if(j < end)
			{
				sb.append(plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Seperator"));
			}
			if(k == 10 || j == end)
			{
				msg.add(sb.toString());
				sb = new StringBuilder();
				k = 1;
				continue;
			}
			k++;
		}
		msg.add(plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.AdditionalChooseNumber")
				.replace("%additionalamountofchoosennumber%", String.valueOf(cl.getAdditionalAmountOfChoosenNumber())));
		start = cl.getAdditionalFirstNumberToChooseFrom();
		end = cl.getAdditionalLastNumberToChooseFrom();
		k = 1;
		sb = new StringBuilder();
		for(int j = start; j <= end; j++)
		{
			if(clt.getAdditionalChoosenNumbers().contains(j))
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasChoosen")
						.replace("%number%", getCmdForContainingNumbers(cl, getSpacing(j, cl.getAdditionalLastNumberToChooseFrom()))));
			} else
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasntNeutralChoosen")
						.replace("%number%", getCmdForContainingNumbers(cl, getSpacing(j, cl.getAdditionalLastNumberToChooseFrom()))));
			}
			if(j < end)
			{
				sb.append(plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Seperator"));
			}
			if(k == 10 || j == end)
			{
				msg.add(sb.toString());
				sb = new StringBuilder();
				k = 1;
				continue;
			}
			k++;
		}
		MessageHandler.sendMessage(player, msg.toArray(new String[msg.size()]));
	}
	
	private void sendMessageWithFieldToScratch(Player player, ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		ArrayList<String> msg = new ArrayList<>();
		
	}
	
	private void sendFinishMessage(Player player, ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		ArrayList<String> samefield = new ArrayList<>();
		double winamount = 0.0;
		ArrayList<Double> fields = new ArrayList<>();
		HashSet<Integer> alreadycheck = new HashSet<>();
		for(Entry<Integer, ScratchCardField> e : map.entrySet())
		{
			if(!alreadycheck.contains(e.getKey()))
			{
				alreadycheck.add(e.getKey());
				int c = 1;
				for(Entry<Integer, ScratchCardField> ee : map.entrySet())
				{
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
				if(c >= sc.getAmountOfSameFieldToWin())
				{
					int d = c - sc.getAmountOfSameFieldToWin() + 1;
					double result = d * e.getValue().getWinningAmount();
					winamount += result;
					samefield.add(plugin.getYamlHandler().getLang().getString("ScratchCard.Arg.Play.FieldWin")
							.replace("%d%", String.valueOf(d))
							.replace("%amount%", String.valueOf(e.getValue().getDisplay()))
							.replace("%result%", EconomyHandler.format(result)));
				}
			}
			fields.add(e.getValue().getWinningAmount());
		}
		if(samefield.size() > 0)
		{
			MessageHandler.sendMessage(player, samefield.toArray(new String[samefield.size()]));
		}
		ScratchCardTicket sct = new ScratchCardTicket(0, sc.getLotteryName(), player.getUniqueId(), winamount, fields);
		plugin.getMysqlHandler().create(sct);
	}
	
	private ArrayList<String> sendScratchFields(ScratchCard sc, LinkedHashMap<Integer, ScratchCardField> map)
	{
		ArrayList<String> msg = new ArrayList<>();
		int c = sc.getFieldPerLine();
		int d = 0;
		ArrayList<StringBuilder> sbA = new ArrayList<>();
		for(int i = 0; i < sc.getAmountOfFields(); i++)
		{
			if(d >= c)
			{
				d = 0;
				sbA.forEach(x -> msg.add(x.toString()));
				sbA = new ArrayList<>();
			}
			if(map.containsKey(i*-1))
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