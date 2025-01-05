package me.avankziar.lly.general.objects;

import java.util.Random;
import java.util.UUID;

import org.bukkit.Bukkit;

import me.avankziar.lly.spigot.LLY;

public class ExecutableCommand 
{
	public enum ServerType
	{
		SPIGOT,
		PROXY;
	}
	
	/**
	 * The ServerType where the command should be executed from the console.
	 */
	private ServerType serverType;
	/**
	 * The Command without / at the beginning.
	 */
	private String command;
	/**
	 * The optional chance, when the command should be triggert. 100 equals always.
	 */
	private int chanceToExecute;
	/**
	 * The startpoint of the randomiser, if it will be wishes, that a number in the command should be randomised.
	 */
	private int startRandom;
	/**
	 * The endpoint of the randomiser.
	 */
	private int endRandom;
	/**
	 * if true, the random number will be a instance of Double, else it will be Integer.
	 */
	private boolean shouldRandomAsDouble;
	
	public ExecutableCommand(ServerType serverType, String command, int chanceToExecute,
			int startRandom, int endRandom, boolean shouldRandomAsDouble)
	{
		setServerType(serverType);
		setCommand(command);
		setChanceToExecute(chanceToExecute);
		setStartRandom(startRandom);
		setEndRandom(endRandom);
		setShouldRandomAsDouble(shouldRandomAsDouble);
	}

	public ServerType getServerType() {
		return serverType;
	}

	public void setServerType(ServerType serverType) {
		this.serverType = serverType;
	}

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public int getChanceToExecute() {
		return chanceToExecute;
	}

	public void setChanceToExecute(int chanceToExecute) {
		this.chanceToExecute = chanceToExecute;
	}

	public int getStartRandom() {
		return startRandom;
	}

	public void setStartRandom(int startRandom) {
		this.startRandom = startRandom;
	}

	public int getEndRandom() {
		return endRandom;
	}

	public void setEndRandom(int endRandom) {
		this.endRandom = endRandom;
	}

	public boolean shouldRandomAsDouble() {
		return shouldRandomAsDouble;
	}

	public void setShouldRandomAsDouble(boolean shouldRandomAsDouble) {
		this.shouldRandomAsDouble = shouldRandomAsDouble;
	}
	
	public void execute(UUID playeruuid)
	{
		PlayerData pd = LLY.getPlugin().getMysqlHandler().getData(
				new PlayerData(), "`player_uuid` = ?", playeruuid.toString());
		if(pd == null)
		{
			return;
		}
		execute(pd.getName());
	}
	
	public void execute(String playername)
	{
		if(command.startsWith("dummy"))
		{
			return;
		}
		if(chanceToExecute < 100)
		{
			Random r = new Random();
			// 40 < 50
			if(r.nextInt(0, 100) < chanceToExecute)
			{
				return;
			}			
		}
		Random r = new Random();
		int result = startRandom == endRandom ? startRandom 
				: (startRandom < endRandom ? r.nextInt(startRandom, endRandom+1) : r.nextInt(endRandom, startRandom+1));
		String command = this.command.replace("%playername%", playername)
				.replace("%random%", shouldRandomAsDouble ? String.valueOf(Double.valueOf(result)) : String.valueOf(result));
		if(serverType == ServerType.PROXY)
		{
			if(LLY.getPlugin().getCtV() == null)
			{
				return;
			}
			LLY.getPlugin().getCtV().executeAsConsole(command);
			return;
		}
		Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
	}
}
