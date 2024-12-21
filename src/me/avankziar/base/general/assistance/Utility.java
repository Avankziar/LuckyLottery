package me.avankziar.base.general.assistance;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.UUID;

import javax.annotation.Nullable;

import me.avankziar.base.general.database.MysqlBaseHandler;
import me.avankziar.base.general.database.MysqlType;
import me.avankziar.base.general.objects.PlayerData;

public class Utility
{
	@Nullable
	private static MysqlBaseHandler mysqlBaseHandler;
	
	public Utility(MysqlBaseHandler mysqlBaseHandler)
	{
		Utility.mysqlBaseHandler = mysqlBaseHandler;
	}
	
	public static double getNumberFormat(double d)
	{
		BigDecimal bd = new BigDecimal(d).setScale(1, RoundingMode.HALF_UP);
		double newd = bd.doubleValue();
		return newd;
	}
	
	public static double getNumberFormat(double d, int scale)
	{
		BigDecimal bd = new BigDecimal(d).setScale(scale, RoundingMode.HALF_UP);
		double newd = bd.doubleValue();
		return newd;
	}
	
	public static String convertUUIDToName(String uuid)
	{
		if(mysqlBaseHandler.exist(MysqlType.PLAYERDATA, "player_uuid = ?", uuid))
		{
			return ((PlayerData) mysqlBaseHandler.getData(MysqlType.PLAYERDATA, "player_uuid = ?", uuid)).getName();
		}
		return null;
	}
	
	public static UUID convertNameToUUID(String playername)
	{
		if(mysqlBaseHandler.exist(MysqlType.PLAYERDATA, "`player_name` = ?", playername))
		{
			return ((PlayerData) mysqlBaseHandler.getData(MysqlType.PLAYERDATA, "`player_name` = ?", playername)).getUUID();
		}
		return null;
	}
	
	public boolean existMethod(Class<?> externclass, String method)
	{
	    try 
	    {
	    	Method[] mtds = externclass.getMethods();
	    	for(Method methods : mtds)
	    	{
	    		if(methods.getName().equalsIgnoreCase(method))
	    		{
	    	    	return true;
	    		}
	    	}
	    	return false;
	    } catch (Exception e) 
	    {
	    	return false;
	    }
	}
	
	public static double round(double value, int places) 
	{
	    if (places < 0) throw new IllegalArgumentException();
	    try
	    {
	    	BigDecimal bd = BigDecimal.valueOf(value);
		    bd = bd.setScale(places, RoundingMode.HALF_UP);
		    return bd.doubleValue();
	    } catch (NumberFormatException e)
	    {
	    	return 0;
	    }
	}
}
