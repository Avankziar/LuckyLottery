package me.avankziar.lly.spigot.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import me.avankziar.lly.general.objects.PlayerData;
import me.avankziar.lly.spigot.LLY;

public class JoinLeaveListener implements Listener
{
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event)
	{
		Player player = event.getPlayer();
		PlayerData pd = LLY.getPlugin().getMysqlHandler().getData(new PlayerData(), "`player_uuid` = ?", player.getUniqueId().toString());
		if(pd != null)
		{
			pd.setName(player.getName());
			LLY.getPlugin().getMysqlHandler().updateData(pd, "`player_uuid` = ?", pd.getUUID().toString());
		} else
		{
			pd = new PlayerData(0, player.getUniqueId(), player.getName(), false);
			LLY.getPlugin().getMysqlHandler().create(pd);
		}
	}
}
