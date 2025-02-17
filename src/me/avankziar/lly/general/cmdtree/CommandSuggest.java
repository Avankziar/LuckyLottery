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
		CLASSICLOTTO_DRAWNOW,
		CLASSICLOTTO_GIVETICKET,
		CLASSICLOTTO_PLAY,
		CLASSICLOTTO_ADDPOT,
		CLASSICLOTTO_SETPOT,
		CLASSICLOTTO_OPEN,
		CLASSICLOTTO_TICKETLIST,
		CLASSICLOTTO_REPEAT,
		CLASSICLOTTO_NEXTDRAWS,
		
		LOTTOSUPER,
		LOTTOSUPER_DRAWNOW,
		LOTTOSUPER_GIVETICKET,
		LOTTOSUPER_PLAY,
		LOTTOSUPER_ADDPOT,
		LOTTOSUPER_SETPOT,
		LOTTOSUPER_OPEN,
		LOTTOSUPER_TICKETLIST,
		LOTTOSUPER_REPEAT,
		LOTTOSUPER_NEXTDRAWS,
		
		SCRATCHCARD,
		SCRATCHCARD_GIVETICKET,
		SCRATCHCARD_PLAY,
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
