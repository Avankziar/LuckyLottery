package me.avankziar.lly.general.database;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public interface MysqlLottery
{
	public boolean create(Connection conn);
	
	public boolean update(Connection conn, String whereColumn, Object... whereObject);
	
	public ArrayList<Object> get(Connection conn, String orderby, String limit, String whereColumn, Object... whereObject);
	
	default void log(Logger logger, Level level, String log, Exception e)
	{
		logger.log(level, log, e);
	}
}