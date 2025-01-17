package me.avankziar.lly.spigot.cmd.lottosuper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

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
import me.avankziar.lly.spigot.handler.lottery.LottoSuperHandler;

public class ARG_Play extends ArgumentModule
{
	private LLY plugin;
	private LinkedHashMap<UUID, LottoSuperTicket> playingNumber = new LinkedHashMap<>();
	
	public ARG_Play(ArgumentConstructor ac)
	{
		super(ac);
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /lottosuper play lotteryname numbers... anumbers -r|-repeat|-w|-wiederholen
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
		LottoSuperDraw lsd = plugin.getMysqlHandler().getData(ls.getDrawMysql(), "`was_drawn` = ?", false);
		if(ls.getMaximalAmountOfTicketWhichCanAPlayerBuy() > 0)
		{
			int count = plugin.getMysqlHandler().getCount(new LottoSuperTicket(ls.getLotteryName()),
					"`player_uuid` = ? AND `draw_id` = ?", player.getUniqueId().toString(), lsd.getId());
			if(count >=  ls.getMaximalAmountOfTicketWhichCanAPlayerBuy())
			{
				MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.TooManyTickets")
						.replace("%lotteryname%", ls.getLotteryName())
						.replace("%amount%", String.valueOf(count))
						.replace("%maximum%", String.valueOf(ls.getMaximalAmountOfTicketWhichCanAPlayerBuy())));
				return;
			}
		}
		boolean confirm = false;
		if(args.length <= 2)
		{
			sendMessageToChoose(player, ls, lsd, getSet(player.getUniqueId(), ls, lsd));
			return;
		}
		String value = args[2];
		if(lsd == null)
		{
			MessageHandler.sendMessage(player.getUniqueId(), plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.LotteryNotOpen"));
			return;
		}
		if(MatchApi.isInteger(value))
		{
			int n = Integer.valueOf(value);
			if(n >= ls.getFirstNumberToChooseFrom() && n <= ls.getLastNumberToChooseFrom())
			{
				addRemoveNumber(player.getUniqueId(), ls, lsd, n);
			}
		}
		if(value.startsWith("a"))
		{
			String a = value.substring(1);
			if(MatchApi.isInteger(a))
			{
				int n = Integer.valueOf(a);
				if(n >= ls.getAdditionalFirstNumberToChooseFrom() && n <= ls.getAdditionalLastNumberToChooseFrom())
				{
					addRemoveAdditionalNumber(player.getUniqueId(), ls, lsd, n);
				}
			}
		}
		if(MatchApi.isBoolean(value))
		{
			setRepeat(player.getUniqueId(), ls, lsd, Boolean.valueOf(value));
		} else if("-r".equalsIgnoreCase(value) || "repeat".equalsIgnoreCase(value)
				|| "-w".equalsIgnoreCase(value) || "wiederholen".equalsIgnoreCase(value))
		{
			setRepeat(player.getUniqueId(), ls, lsd, true);
		}
		if("random".equalsIgnoreCase(value))
		{
			LinkedHashSet<Integer> set = LottoSuperHandler.sortDrawnNumber(
					LottoSuperHandler.drawLotteryNumber(
					ls.getFirstNumberToChooseFrom(), ls.getLastNumberToChooseFrom(), ls.getAmountOfChoosedNumber()));
			set.stream().forEach(x -> addRemoveNumber(player.getUniqueId(), ls, lsd, x));
			LinkedHashSet<Integer> aset = LottoSuperHandler.sortDrawnNumber(
					LottoSuperHandler.drawLotteryNumber(
					ls.getAdditionalFirstNumberToChooseFrom(), ls.getAdditionalLastNumberToChooseFrom(), ls.getAdditionalAmountOfChoosenNumber()));
			aset.stream().forEach(x -> addRemoveAdditionalNumber(player.getUniqueId(), ls, lsd, x));
		}
		if("confirm".equalsIgnoreCase(value) || "bestätigen".equalsIgnoreCase(value))
		{
			confirm = true;
		}
		LottoSuperTicket lst = getSet(player.getUniqueId(), ls, lsd);
		if(lst.getChoosenNumbers().size() >= ls.getAmountOfChoosedNumber()
				&& lst.getAdditionalChoosenNumbers().size() >= ls.getAdditionalAmountOfChoosenNumber())
		{
			//Choosen all requiert number
			if(!confirm)
			{
				ArrayList<String> l = new ArrayList<>();
				for(String s : plugin.getYamlHandler().getLang().getStringList("LottoSuper.Arg.Play.MayYouConfirm"))
				{
					if(s.contains("{") || s.contains("}"))
					{
						l.add(getCmdForConfirm(ls, lst.getChoosenNumbers(), lst.shouldRepeate(), s));
					} else
					{
						l.add(replace(lst.getChoosenNumbers(), lst.getAdditionalChoosenNumbers(),
								s.replace("%costperticket%", EconomyHandler.format(ls.getCostPerTicket()))));
					}
					
				}
				MessageHandler.sendMessage(player, l.toArray(new String[l.size()]));
				return;
			}
			if(!EconomyHandler.hasBalance(player.getUniqueId(), ls.getCostPerTicket()))
			{
				MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NotEnoughMoney"));
				return;
			}
			String cat = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Category").replace("%lotteryname%", ls.getLotteryName());
			String comment = plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.Comment");
			EconomyHandler.withdraw(player.getUniqueId(), ls.getCostPerTicket(), cat, comment);
			MessageHandler.sendMessage(player, 
					replace(lst.getChoosenNumbers(), lst.getAdditionalChoosenNumbers(),
							plugin.getYamlHandler().getLang().getString("LottoSuper.Arg.Play.TicketBought")));
			plugin.getMysqlHandler().create(lst);
			playingNumber.remove(player.getUniqueId());
			return;
		}
		sendMessageToChoose(player, ls, lsd, lst);
	}
	
	private LottoSuperTicket getSet(UUID uuid, LottoSuper cl, LottoSuperDraw cld)
	{
		return playingNumber.containsKey(uuid) 
				? playingNumber.get(uuid) 
				: new LottoSuperTicket(0, cld.getId(), cl.getLotteryName(), uuid, false, 0, 0.0,
						new LinkedHashSet<Integer>(), new LinkedHashSet<Integer>());
	}
	
	private void addRemoveNumber(UUID uuid, LottoSuper cl, LottoSuperDraw cld, int i)
	{
		LottoSuperTicket clt = getSet(uuid, cl, cld);
		if(clt.getChoosenNumbers().contains(i))
		{
			clt.getChoosenNumbers().remove(i);
		} else
		{
			if(clt.getChoosenNumbers().size() < cl.getAmountOfChoosedNumber())
			{
				clt.getChoosenNumbers().add(i);
			}
		}
		LottoSuperHandler.sortDrawnNumber(clt.getChoosenNumbers());
		playingNumber.put(uuid, clt);
	}
	
	private void addRemoveAdditionalNumber(UUID uuid, LottoSuper cl, LottoSuperDraw cld, int i)
	{
		LottoSuperTicket clt = getSet(uuid, cl, cld);
		if(clt.getAdditionalChoosenNumbers().contains(i))
		{
			clt.getChoosenNumbers().remove(i);
		} else
		{
			if(clt.getAdditionalChoosenNumbers().size() < cl.getAdditionalAmountOfChoosenNumber())
			{
				clt.getAdditionalChoosenNumbers().add(i);
			}
		}
		LottoSuperHandler.sortDrawnNumber(clt.getAdditionalChoosenNumbers());
		playingNumber.put(uuid, clt);
	}
	
	private void setRepeat(UUID uuid, LottoSuper cl, LottoSuperDraw cld, boolean b)
	{
		LottoSuperTicket clt = getSet(uuid, cl, cld);
		clt.setShouldRepeate(b);
		playingNumber.put(uuid, clt);
	}
	
	public static String replace(LinkedHashSet<Integer> set, LinkedHashSet<Integer> aset, String r)
	{
		LinkedHashSet<Integer> sets = LottoSuperHandler.sortDrawnNumber(set);
		ArrayList<String> l = new ArrayList<>();
		sets.stream().forEach(x -> l.add(String.valueOf(x)));
		LinkedHashSet<Integer> asets = LottoSuperHandler.sortDrawnNumber(aset);
		ArrayList<String> al = new ArrayList<>();
		asets.stream().forEach(x -> al.add(String.valueOf(x)));
		String s = r;
		if(set.size() > 0 && asets.size() > 0)
		{
			s = s.replace("%choosennumber%", String.join(", ", l)).replace("%additionalchoosennumber%", String.join(", ", al));
		} else if(set.size() > 0 && asets.size() == 0)
		{
			s = s.replace("%choosennumber%", String.join(", ", l)).replace("%additionalchoosennumber%", "/");
		} else if(set.size() == 0 && asets.size() > 0)
		{
			s = s.replace("%choosennumber%", "/").replace("%additionalchoosennumber%", String.join(", ", al));
		} else
		{
			s = s.replace("%choosennumber%", "/").replace("%additionalchoosennumber%", "/");
		}
		return s;
	}
	
	private void sendMessageToChoose(Player player, LottoSuper cl, LottoSuperDraw cld, LottoSuperTicket clt)
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
						.replace("%number%", getCmdForContainingNumbers(cl, getSpacing(j, cl.getLastNumberToChooseFrom()))));
			} else
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasntNeutralChoosen")
						.replace("%number%", getCmdForContainingNumbers(cl, getSpacing(j, cl.getLastNumberToChooseFrom()))));
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
						.replace("%number%", getCmdForContainingNumbers(cl, "a"+getSpacing(j, cl.getAdditionalLastNumberToChooseFrom()))));
			} else
			{
				sb.append(plugin.getYamlHandler().getLang().getString("WasntNeutralChoosen")
						.replace("%number%", getCmdForContainingNumbers(cl, "a"+getSpacing(j, cl.getAdditionalLastNumberToChooseFrom()))));
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
	
	public static String getSpacing(int i, int j)
	{
		int l1 = String.valueOf(i).length();
		int l2 = String.valueOf(j).length();
		return l1 < l2 ? "0".repeat(l2-l1)+String.valueOf(i) : String.valueOf(i);
			
	}
	
	private String getCmdForContainingNumbers(LottoSuper cl, String j)
	{
		return "<click:run_command:'"+
				CommandSuggest.get(Type.LOTTOSUPER_PLAY).getCommandString()+
				cl.getLotteryName()+" "+
				j+
				"'>"+j+"</click>";
	}
	
	private String getCmdForRandom(LottoSuper cl, String i, String j)
	{
		return "<click:run_command:'"+
				CommandSuggest.get(Type.LOTTOSUPER_PLAY).getCommandString()+
				cl.getLotteryName()+" "+
				i+
				"'>"+j+"</click>";
	}
	
	private String getCmdForRepeat(LottoSuper cl, boolean repeat, String s)
	{
		return "<click:run_command:'"
				+CommandSuggest.get(Type.LOTTOSUPER_PLAY).getCommandString()
				+cl.getLotteryName()+" "
				+String.valueOf(repeat)
				+"'>"+s+"</click>";
	}
	
	private String getCmdForConfirm(LottoSuper cl, LinkedHashSet<Integer> set, boolean repeat, String s)
	{
		return "<click:run_command:'"
				+CommandSuggest.get(Type.LOTTOSUPER_PLAY).getCommandString()
				+cl.getLotteryName()
				+" confirm"
				+"'>"+s+"</click>";
	}
}