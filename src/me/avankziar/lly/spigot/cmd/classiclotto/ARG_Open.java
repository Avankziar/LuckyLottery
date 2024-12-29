package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.stream.Collectors;

import org.bukkit.command.CommandSender;

import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.WinningClass;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.EconomyHandler;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_Open extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_Open(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /classiclotto open lotteryname
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws IOException 
	{
		task(sender, args);		
	}
	
	private void task(CommandSender sender, String[] args)
	{
		String scl = args[1];
		Optional<ClassicLotto> ocl = LotteryHandler.getClassicLotto(scl);
		if(ocl.isEmpty())
		{
			MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("ClassicLotto.NoClassicLottoFound"));
			return;
		}
		ClassicLotto cl = ocl.get();
		ClassicLottoDraw cld = LLY.getPlugin().getMysqlHandler().getData(cl.getDrawMysql(), "`was_drawn` = ?", false);
		if(cld != null)
		{
			MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Open.AlreayOpen")
					.replace("%lotteryname%", cl.getLotteryName()));
			return;
		}
		int last = LLY.getPlugin().getMysqlHandler().lastID(cl.getDrawMysql());
		cld = LLY.getPlugin().getMysqlHandler().getData(cl.getDrawMysql(), "`id` = ?", last);
		if(cld == null)
		{
			cld = new ClassicLottoDraw(0, cl.getLotteryName(), false, 0, cl.getStandartPot(), new LinkedHashSet<Integer>());
			plugin.getMysqlHandler().create(cld);
			ArrayList<String> msg = new ArrayList<>();
			msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.Open.Open")
					.replace("%lotteryname%", cl.getLotteryName())
					.replace("%actualpot%", EconomyHandler.format(cld.getActualPot())));
			MessageHandler.sendMessage(msg.toArray(new String[msg.size()]));
			return;
		}
		double nextpot = cld.getActualPot();
		if(LLY.getPlugin().getMysqlHandler().exist(cl.getTicketMysql(), "`winning_class_level` = ?", 1))
		{
			nextpot = cl.getStandartPot();
		} else
		{
			ArrayList<WinningClass> wcA = (ArrayList<WinningClass>) cl.getWinningClass().stream().collect(Collectors.toList());
			wcA.sort(Comparator.comparingInt(WinningClass::getWinningClassLevel));
			for(WinningClass wc : wcA)
			{
				if(wc.getWinningClassLevel() == 1)
				{
					continue;
				}
				if(!plugin.getMysqlHandler().exist(cl.getTicketMysql(), 
						"`draw_id` = ? AND `winning_class_level` = ?", cld.getId(), wc.getWinningClassLevel()))
				{
					continue;
				}
				switch(wc.getPayoutType())
				{
				case LUMP_SUM: break;
				case PERCENTAGE: 
					//Subtract the percentage amount of the last pot
					nextpot = nextpot - (nextpot * (wc.getAmount() / 100)); break;
				}
			}
			if(nextpot < cl.getStandartPot())
			{
				nextpot = cl.getStandartPot();
			} else
			{
				nextpot += cl.getAmountToAddToThePotIfNoOneIsWinning();
			}
		}
		ClassicLottoDraw cldNext = new ClassicLottoDraw(0, cl.getLotteryName(), false, 0, nextpot, new LinkedHashSet<>());
		plugin.getMysqlHandler().create(cldNext);
		ArrayList<String> msg = new ArrayList<>();
		for(String s : plugin.getYamlHandler().getLang().getStringList("ClassicLotto.Arg.Open.Open"))
		{
			String r = s.replace("%lotteryname%", cl.getLotteryName())
					.replace("%actualpot%", EconomyHandler.format(nextpot));
			msg.add(r);
		}
		MessageHandler.sendMessage(msg.toArray(new String[msg.size()]));
	}
}