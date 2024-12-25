package me.avankziar.lly.general.cmdtree;

import java.util.LinkedHashMap;

public class CommandSuggest
{
	/**
	 * All Commands and their following arguments
	 */
	public enum Type 
	{
		LLY,
		LLY_BASE,
		CLASSICLOTTO,
		CLASSICLOTTO_PLAY
	}
	
	public static LinkedHashMap<CommandSuggest.Type, BaseConstructor> map = new LinkedHashMap<>();
	
	public static void set(CommandSuggest.Type cst, BaseConstructor bc)
	{
		map.put(cst, bc);
	}
	
	public static BaseConstructor get(CommandSuggest.Type ces)
	{
		return map.get(ces);
	}
	
	public static String getCmdString(CommandSuggest.Type ces)
	{
		BaseConstructor bc = map.get(ces);
		return bc != null ? bc.getCommandString() : null;
	}
	
	
}
