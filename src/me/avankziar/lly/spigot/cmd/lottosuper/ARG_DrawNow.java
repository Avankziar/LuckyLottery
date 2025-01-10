package me.avankziar.lly.spigot.cmd.lottosuper;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Optional;

import org.bukkit.command.CommandSender;

import me.avankziar.lly.general.assistance.MatchApi;
import me.avankziar.lly.general.cmdtree.ArgumentConstructor;
import me.avankziar.lly.general.objects.lottery.LottoSuper;
import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.cmdtree.ArgumentModule;
import me.avankziar.lly.spigot.handler.MessageHandler;
import me.avankziar.lly.spigot.handler.lottery.LotteryHandler;
import me.avankziar.lly.spigot.handler.lottery.LottoSuperHandler;

public class ARG_DrawNow extends ArgumentModule
{
	private LLY plugin;
	//private ArgumentConstructor ac;
	private long cooldown = 0L;
	
	public ARG_DrawNow(ArgumentConstructor ac)
	{
		super(ac);
		//this.ac = ac;
		this.plugin = LLY.getPlugin();
	}

	/**
	 * => /lottosuper drawnow lotteryname [numbers|anumbers|reopen]
	 */
	@Override
	public void run(CommandSender sender, String[] args) throws IOException 
	{
		task(sender, args);		
	}
	
	private void task(CommandSender sender, String[] args)
	{
		String scl = args[1];
		Optional<LottoSuper> ocl = LotteryHandler.getLottoSuper(scl);
		if(ocl.isEmpty())
		{
			MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("LottoSuper.NoLottoSuperFound"));
			return;
		}
		LottoSuper cl = ocl.get();
		if(cooldown > System.currentTimeMillis())
		{
			MessageHandler.sendMessage(sender, plugin.getYamlHandler().getLang().getString("LottoSuper.DrawNow.OnCooldown")
					.replace("%lotteryname%", cl.getLotteryName()));
			return;
		}
		cooldown = System.currentTimeMillis() + 30*1000L;
		LinkedHashSet<Integer> choosenNumber = new LinkedHashSet<>();
		LinkedHashSet<Integer> additionalChoosenNumber = new LinkedHashSet<>();
		boolean reopen = !cl.isDrawManually();
		for(int i = 2; i < args.length; i++)
		{
			if(MatchApi.isInteger(args[i]) && choosenNumber.size() < cl.getAmountOfChoosedNumber())
			{
				int n = Integer.valueOf(args[i]);
				if(n >= cl.getFirstNumberToChooseFrom() && n <= cl.getLastNumberToChooseFrom())
				{
					choosenNumber.add(n);
				}
			}
			if(args[i].startsWith("a"))
			{
				String a = args[i].substring(1);
				if(MatchApi.isInteger(a) && additionalChoosenNumber.size() < cl.getAdditionalAmountOfChoosenNumber())
				{
					int n = Integer.valueOf(a);
					if(n >= cl.getAdditionalFirstNumberToChooseFrom() && n <= cl.getAdditionalLastNumberToChooseFrom())
					{
						additionalChoosenNumber.add(n);
					}
				}
			}
			if(MatchApi.isBoolean(args[i]) || args[i].equals("reopen"))
			{
				reopen = MatchApi.isBoolean(args[i]) ? Boolean.valueOf(args[i]) : args[i].equals("reopen");
			}
		}
		LottoSuperHandler.drawLotto(cl, choosenNumber, additionalChoosenNumber, reopen);
	}
}
