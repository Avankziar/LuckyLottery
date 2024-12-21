package me.avankziar.base.spigot.database;

import me.avankziar.base.general.database.MysqlBaseHandler;
import me.avankziar.base.spigot.LLY;

public class MysqlHandler extends MysqlBaseHandler
{	
	public MysqlHandler(LLY plugin)
	{
		super(plugin.getLogger(), plugin.getMysqlSetup());
	}
}
