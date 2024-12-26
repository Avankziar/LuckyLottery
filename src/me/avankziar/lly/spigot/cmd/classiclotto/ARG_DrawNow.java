package me.avankziar.lly.spigot.cmd.classiclotto;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.ClassicLotto;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.ClassicLottoHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;

public class ARG_DrawNow extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	
	public ARG_DrawNow(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
	}

	/**
	 * => /classiclotto drawnow lotteryname [numbers]
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
		LinkedHashSet<Integer> choosenNumber = new LinkedHashSet<>();
		for(int i = 2; i < args.length; i++)
		{
			if(MatchApi.isInteger(args[i]) && choosenNumber.size() < cl.getAmountOfChoosedNumber())
			{
				choosenNumber.add(Integer.valueOf(args[i]));
			}
		}
		ClassicLottoHandler.drawLotto(cl, choosenNumber);
	}
}
