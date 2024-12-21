package me.avankziar.lly.spigot.gui.listener;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;

import me.avankziar.lly.spigot.LLY;
import me.avankziar.lly.spigot.gui.GUIApi;
import me.avankziar.lly.spigot.gui.events.BottomGuiClickEvent;
import me.avankziar.lly.spigot.gui.objects.GuiType;

public class BottomListener implements Listener
{
	private LLY plugin;
	
	public BottomListener(LLY plugin)
	{
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onBottomGui(BottomGuiClickEvent event)
	{
		if(!event.getPluginName().equals(LLY.pluginname))
		{
			return;
		}
		if(event.getEvent().getCurrentItem() == null || event.getEvent().getCurrentItem().getType() == Material.AIR)
		{
			return;
		}
		final ItemStack is = event.getEvent().getCurrentItem().clone();
		is.setAmount(1);
		if(!(event.getEvent().getWhoClicked() instanceof Player))
		{
			return;
		}
		Player player = (Player) event.getEvent().getWhoClicked();
		if(!GUIApi.isInGui(player.getUniqueId()))
		{
			return;
		}
		GuiType gt = GUIApi.getGuiType(player.getUniqueId());
		switch(gt)
		{
		default:
			break;
		}
	}
}