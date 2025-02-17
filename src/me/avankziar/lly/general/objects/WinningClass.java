package me.avankziar.lly.general.objects;

import java.util.ArrayList;

public class WinningClass 
{
	public enum PayoutType
	{
		/**
		 * All winners divide the winning, which is a percentage of the pot.
		 */
		PERCENTAGE,
		/**
		 * All winners become a lump sum.<br>
		 * Is for lottery without a pot. (Scratch cards)
		 */
		LUMP_SUM
	}
	
	/**
	 * The level or step of the winning ladder the winningclass is.<br>
	 * The lower the level, the lower the price. Minimum is 1.
	 */
	private int winningClassLevel;
	private PayoutType payoutType;
	/**
	 * The percentage displace as 30% = 0.3 or the lump sum.
	 */
	private double amount;
	/**
	 * The lottorule, how much number you must have match to win this winningclass.
	 */
	private int numberMatchToWin;
	/**
	 * All commands, will be executed if the player wins the winningclass.
	 */
	private ArrayList<ExecutableCommand> executableCommands = new ArrayList<>();
	
	private KeepsakeItem keepsakeItem;
	
	public WinningClass(int winningClassLevel, PayoutType payoutType, double amount, int numberMatchToWin,
			ArrayList<ExecutableCommand> executableCommands, KeepsakeItem keepsakeItem)
	{
		setWinningClassLevel(winningClassLevel);
		setPayoutType(payoutType);
		setAmount(amount);
		setNumberMatchToWin(numberMatchToWin);
		setExecutableCommands(executableCommands);
		setKeepsakeItem(keepsakeItem);
	}

	public int getWinningClassLevel() {
		return winningClassLevel;
	}

	public void setWinningClassLevel(int winningCategoryLevel) {
		this.winningClassLevel = winningCategoryLevel;
	}

	public PayoutType getPayoutType() {
		return payoutType;
	}

	public void setPayoutType(PayoutType payoutType) {
		this.payoutType = payoutType;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public int getNumberMatchToWin() {
		return numberMatchToWin;
	}

	public void setNumberMatchToWin(int numberMatchToWin) {
		this.numberMatchToWin = numberMatchToWin;
	}

	public ArrayList<ExecutableCommand> getExecutableCommands() {
		return executableCommands;
	}

	public void setExecutableCommands(ArrayList<ExecutableCommand> executableCommands) {
		this.executableCommands = executableCommands;
	}

	public KeepsakeItem getKeepsakeItem() {
		return keepsakeItem;
	}

	public void setKeepsakeItem(KeepsakeItem keepsakeItem) {
		this.keepsakeItem = keepsakeItem;
	}
}
