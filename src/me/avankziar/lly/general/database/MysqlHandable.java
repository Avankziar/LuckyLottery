package me.avankziar.lly.general.database;

public interface MysqlHandable<T> extends MysqlTable<T>
{	
	public boolean setupMysql(MysqlBaseSetup mysqlSetup, ServerType serverType);
}