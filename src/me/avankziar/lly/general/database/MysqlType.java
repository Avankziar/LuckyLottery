package me.avankziar.lly.general.database;

import me.avankziar.lly.general.objects.PlayerData;

public enum MysqlType
{
	PLAYERDATA("basePlayerData", new PlayerData(), ServerType.ALL,
			"CREATE TABLE IF NOT EXISTS `%%tablename%%"
			+ "` (id int AUTO_INCREMENT PRIMARY KEY,"
			+ " player_uuid char(36) NOT NULL UNIQUE,"
			+ " player_name varchar(16) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,"
			+ " searchtype text);");
	
	private MysqlType(String tableName, Object object, ServerType usedOnServer, String setupQuery)
	{
		this.tableName = tableName;
		this.object = object;
		this.usedOnServer = usedOnServer;
		this.setupQuery = setupQuery.replace("%%tablename%%", tableName);
	}
	
	private final String tableName;
	private final Object object;
	private final ServerType usedOnServer;
	private final String setupQuery;

	public String getValue()
	{
		return tableName;
	}
	
	public Object getObject()
	{
		return object;
	}
	
	public ServerType getUsedOnServer()
	{
		return usedOnServer;
	}
	
	public String getSetupQuery()
	{
		return setupQuery;
	}
}