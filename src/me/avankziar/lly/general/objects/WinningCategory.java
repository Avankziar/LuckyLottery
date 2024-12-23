package me.avankziar.lly.general.objects;

public class WinningCategory 
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
	 * The level or step of the winning ladder the winningcategory is.<br>
	 * The lower the level, the lower the price. Minimum is 1.
	 */
	private int winningCategoryLevel;
	private PayoutType payoutType;
	/**
	 * The percentage displace as 30% = 0.3 or the lump sum.
	 */
	private double amount;
	
	public WinningCategory(int winningCategoryLevel, PayoutType payoutType, double amount)
	{
		setWinningCategoryLevel(winningCategoryLevel);
		setPayoutType(payoutType);
		setAmount(amount);
	}

	public int getWinningCategoryLevel() {
		return winningCategoryLevel;
	}

	public void setWinningCategoryLevel(int winningCategoryLevel) {
		this.winningCategoryLevel = winningCategoryLevel;
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
}
