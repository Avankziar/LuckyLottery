package me.avankziar.lly.spigot.cmd.classiclotto;

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
	private LinkedHashMap<UUID, ClassicLottoTicket> playingNumber = new LinkedHashMap<>();
	
	public ARG_Play(ArgumentConstructor ac)
	{
		super(ac);
		this.plugin = LLY.getPlugin();
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
		ClassicLottoDraw cld = plugin.getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		if(cl.getMaximalAmountOfTicketWhichCanAPlayerBuy() > 0)
		{
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
		boolean confirm = false;
		if(args.length <= 2)
		{
			sendMessageToChoose(player, cl, cld, getSet(player.getUniqueId(), cl, cld));
			return;
		}
		String value = args[2];
		if(cld == null)
		{
			MessageHandler.sendMessage(player.getUniqueId(), plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.LotteryNotOpen"));
			return;
		}
		if(MatchApi.isInteger(value))
		{
			int n = Integer.valueOf(value);
			if(n >= cl.getFirstNumberToChooseFrom() && n <= cl.getLastNumberToChooseFrom())
			{
				addRemoveNumber(player.getUniqueId(), cl, cld, Integer.valueOf(value));
			}
		}
		if(MatchApi.isBoolean(value))
		{
			setRepeat(player.getUniqueId(), cl, cld, Boolean.valueOf(value));
		} else if("-r".equalsIgnoreCase(value) || "repeat".equalsIgnoreCase(value)
				|| "-w".equalsIgnoreCase(value) || "wiederholen".equalsIgnoreCase(value))
		{
			setRepeat(player.getUniqueId(), cl, cld, true);
		}
		if("random".equalsIgnoreCase(value))
		{
			LinkedHashSet<Integer> set = ClassicLottoHandler.sortDrawnNumber(
					ClassicLottoHandler.drawLotteryNumber(
					cl.getFirstNumberToChooseFrom(), cl.getLastNumberToChooseFrom(), cl.getAmountOfChoosedNumber()));
			set.stream().forEach(x -> addRemoveNumber(player.getUniqueId(), cl, cld, x));
		}
		if("confirm".equalsIgnoreCase(value) || "bestätigen".equalsIgnoreCase(value))
		{
			confirm = true;
		}
		ClassicLottoTicket clt = getSet(player.getUniqueId(), cl, cld);
		if(clt.getChoosenNumbers().size() >= cl.getAmountOfChoosedNumber())
		{
			//Choosen all requiert number
			if(!confirm)
			{
				ArrayList<String> l = new ArrayList<>();
				for(String s : plugin.getYamlHandler().getLang().getStringList("ClassicLotto.Arg.Play.MayYouConfirm"))
				{
					if(s.contains("{") || s.contains("}"))
					{
						l.add(getCmdForConfirm(cl, clt.getChoosenNumbers(), clt.shouldRepeate(), s));
					} else
					{
						l.add(replace(clt.getChoosenNumbers(), s.replace("%costperticket%", EconomyHandler.format(cl.getCostPerTicket()))));
					}
					
				}
				MessageHandler.sendMessage(player, l.toArray(new String[l.size()]));
				return;
			}
			if(!EconomyHandler.hasBalance(player.getUniqueId(), cl.getCostPerTicket()))
			{
				MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang().getString("NotEnoughMoney"));
				return;
			}
			String cat = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.Category").replace("%lotteryname%", cl.getLotteryName());
			String comment = plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.Comment");
			EconomyHandler.withdraw(player.getUniqueId(), cl.getCostPerTicket(), cat, comment);
			MessageHandler.sendMessage(player, 
					replace(clt.getChoosenNumbers(), plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.TicketBought")));
			plugin.getMysqlHandler().create(clt);
			playingNumber.remove(player.getUniqueId());
			return;
		}
		sendMessageToChoose(player, cl, cld, clt);
	}
	
	private ClassicLottoTicket getSet(UUID uuid, ClassicLotto cl, ClassicLottoDraw cld)
	{
		return playingNumber.containsKey(uuid) 
				? playingNumber.get(uuid) 
				: new ClassicLottoTicket(0, cld.getId(), cl.getLotteryName(), uuid, false, 0, 0.0, new LinkedHashSet<Integer>());
	}
	
	private void addRemoveNumber(UUID uuid, ClassicLotto cl, ClassicLottoDraw cld, int i)
	{
		ClassicLottoTicket clt = getSet(uuid, cl, cld);
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
		ClassicLottoHandler.sortDrawnNumber(clt.getChoosenNumbers());
		playingNumber.put(uuid, clt);
	}
	
	private void setRepeat(UUID uuid, ClassicLotto cl, ClassicLottoDraw cld, boolean b)
	{
		ClassicLottoTicket clt = getSet(uuid, cl, cld);
		clt.setShouldRepeate(b);
		playingNumber.put(uuid, clt);
	}
	
	public static String replace(LinkedHashSet<Integer> set, String r)
	{
		LinkedHashSet<Integer> sets = ClassicLottoHandler.sortDrawnNumber(set);
		ArrayList<String> l = new ArrayList<>();
		sets.stream().forEach(x -> l.add(String.valueOf(x)));
		String s = r;
		if(set.size() > 0)
		{
			s = s.replace("%choosennumber%", String.join(", ", l));
		} else
		{
			s = s.replace("%choosennumber%", "/");
		}
		return s;
	}
	
	private void sendMessageToChoose(Player player, ClassicLotto cl, ClassicLottoDraw cld, ClassicLottoTicket clt)
	{
		ArrayList<String> msg = new ArrayList<String>();
		if(clt.getChoosenNumbers().size() > 0)
		{
			msg.add(replace(ClassicLottoHandler.sortDrawnNumber(clt.getChoosenNumbers()), 
					plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.AlreadyChoosenNumber")));
		} else
		{
			msg.add(getCmdForRandom(cl, "random", plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.RandomChoose")));
		}
		msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.ShouldRepeat")
				.replace("%shouldrepeat%", 
						getCmdForRepeat(cl, !clt.shouldRepeate(),
								(clt.shouldRepeate() 
										? plugin.getYamlHandler().getLang().getString("IsTrue")
										: plugin.getYamlHandler().getLang().getString("IsFalse")))));
		msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.ChooseNumber")
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
				sb.append(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Play.Seperator"));
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
	
	private String getCmdForContainingNumbers(ClassicLotto cl, String j)
	{
		return "<click:run_command:'"+
				CommandSuggest.get(Type.CLASSICLOTTO_PLAY).getCommandString()+
				cl.getLotteryName()+" "+
				j+
				"'>"+j+"</click>";
	}
	
	private String getCmdForRandom(ClassicLotto cl, String i, String j)
	{
		return "<click:run_command:'"+
				CommandSuggest.get(Type.CLASSICLOTTO_PLAY).getCommandString()+
				cl.getLotteryName()+" "+
				i+
				"'>"+j+"</click>";
	}
	
	private String getCmdForRepeat(ClassicLotto cl, boolean repeat, String s)
	{
		return "<click:run_command:'"
				+CommandSuggest.get(Type.CLASSICLOTTO_PLAY).getCommandString()
				+cl.getLotteryName()+" "
				+String.valueOf(repeat)
				+"'>"+s+"</click>";
	}
	
	private String getCmdForConfirm(ClassicLotto cl, LinkedHashSet<Integer> set, boolean repeat, String s)
	{
		return "<click:run_command:'"
				+CommandSuggest.get(Type.CLASSICLOTTO_PLAY).getCommandString()
				+cl.getLotteryName()
				+" confirm"
				+"'>"+s+"</click>";
	}
}