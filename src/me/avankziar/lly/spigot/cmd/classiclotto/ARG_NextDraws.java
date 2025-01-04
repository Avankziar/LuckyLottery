package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Optional;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.ClassicLottoHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_NextDraws extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_NextDraws(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /classiclotto nextdraws lotteryname
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
		if(cl.isDrawManually())
		{
			MessageHandler.sendMessage(player, plugin.getYamlHandler().getLang()
					.getString("ClassicLotto.Arg.NextDraw.NoDraws"));
			return;
		}
		ArrayList<LocalDateTime> ldtA = DrawTime.getNextTimes(cl.getDrawTime(), null);
		DateTimeFormatter dtf = DateTimeFormatter.ofPattern(
				LLY.getPlugin().getYamlHandler().getConfig().getString("DateTimeFormatter"));
		ArrayList<ArrayList<String>> draws = new ArrayList<>();
		ArrayList<String> arr = new ArrayList<>();
		int i = 0;
		for(LocalDateTime ldt : ldtA)
		{
			if(i < 4)
			{
				arr.add(ldt.format(dtf));
				i++;
			} else
			{
				i = 0;
				arr.add(ldt.format(dtf));
				draws.add(arr);
				arr = new ArrayList<>();
			}
		}
		ArrayList<String> msg = new ArrayList<>();
		msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.NextDraw.NextDraws"));
		for(ArrayList<String> a : draws)
		{
			msg.add(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.NextDraw.NextDrawsReplacer")+
					String.join(plugin.getYamlHandler().getLang().getString("ClassicLotto.Arg.NextDraw.SeperatorReplacer"), a));
		}
		msg = ClassicLottoHandler.replacer(cl, null, null, null, null, null, false, msg);
		MessageHandler.sendMessage(player, msg.toArray(new String[arr.size()]));
	}
}
