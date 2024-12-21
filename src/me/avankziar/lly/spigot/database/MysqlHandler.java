package me.avankziar.lly.spigot.database;

import me.avankziar.lly.general.database.MysqlBaseHandler;
import me.avankziar.lly.spigot.LLY;

public class MysqlHandler extends MysqlBaseHandler
{	
	public MysqlHandler(LLY plugin)
	{
		super(plugin.getLogger(), plugin.getMysqlSetup());
	}
}
