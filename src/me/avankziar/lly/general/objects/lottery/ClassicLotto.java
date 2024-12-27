package me.avankziar.lly.general.objects.lottery;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import me.avankziar.lly.general.assistance.Utility;
import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.WinningClass;
import me.avankziar.lly.general.objects.lottery.draw.ClassicLottoDraw;
import me.avankziar.lly.general.objects.lottery.ticket.ClassicLottoTicket;

public class ClassicLotto extends Lottery
{
	/**
	 * The amount of Money, which is always in the pot.
	 */
	private double standartPot;
	/**
	 * The maximum pot to win.<br>
	 * It can be not goes higher through the plugin as that.
	 */
	private double maximumPot;
	/**
	 * The money amount to add to the pot if no one wins the highest winningcategory.
	 */
	private double amountToAddToThePotIfNoOneIsWinning;
	/**
	 * The amount of money to buy on lottery ticket.
	 */
	private double costPerTicket;
	/**
	 * The Maximal amount of ticket which a player can buy for one draw of the lottery.
	 * -1 is equal to buy infinte amount.
	 */
	private int maximalAmountOfTicketWhichCanAPlayerBuy;
	/**
	 * The frist number to choose from which makes the numberpool.
	 */
	private int fristNumberToChooseFrom;
	/**
	 * The last number to choose from which makes the numberpool.
	 */
	private int lastNumberToChooseFrom;
	/**
	 * The amount of numbers to choose from the pool of the numbers.
	 */
	private int amountOfChoosedNumber;
	
	/**
	 * Time to draw the winners of the lottery.
	 */
	private LinkedHashSet<DrawTime> drawTime = new LinkedHashSet<>();
	/**
	 * The winningClass of the lotto.<br>
	 * The higher the level of the winningclass is, the higher the price should be.
	 */
	private Collection<WinningClass> winningClass = new HashSet<>();
	/**
	 * If true, the classiclotto will not draw automaticlly on the drawtimes.
	 */
	private boolean drawManually;
	
	private String drawOnServer;
	
	public ClassicLotto(String lotteryName, String description, GameType gameType,
			double standartPot, double maximumPot, double amountToAddToThePotIfNoOneIsWinning,
			double costPerTicket, int maximalAmountOfTicketWhichCanAPlayerBuy,
			int firstNumberToChooseFrom, int lastNumberToChooseFrom, int amountOfChoosenNumber,
			LinkedHashSet<DrawTime> drawTime, HashSet<WinningClass> winningClass,
			String drawOnServer, boolean drawManually)
	{
		super(lotteryName, description, gameType);
		setStandartPot(standartPot);
		setMaximumPot(maximumPot);
		setAmountToAddToThePotIfNoOneIsWinning(amountToAddToThePotIfNoOneIsWinning);
		setCostPerTicket(costPerTicket);
		setMaximalAmountOfTicketWhichCanAPlayerBuy(maximalAmountOfTicketWhichCanAPlayerBuy);
		setFristNumberToChooseFrom(firstNumberToChooseFrom);
		setLastNumberToChooseFrom(lastNumberToChooseFrom);
		setAmountOfChoosedNumber(amountOfChoosenNumber);
		setDrawTime(drawTime);
		setWinningClass(winningClass);
		setDrawOnServer(drawOnServer);
		setDrawManually(drawManually);
	}

	public double getStandartPot() {
		return standartPot;
	}

	public void setStandartPot(double standartPot) {
		this.standartPot = standartPot;
	}

	public double getMaximumPot() {
		return maximumPot;
	}

	public void setMaximumPot(double maximumPot) {
		this.maximumPot = maximumPot;
	}

	public double getAmountToAddToThePotIfNoOneIsWinning() {
		return amountToAddToThePotIfNoOneIsWinning;
	}

	public void setAmountToAddToThePotIfNoOneIsWinning(double amountToAddToThePotIfNoOneIsWinning) {
		this.amountToAddToThePotIfNoOneIsWinning = amountToAddToThePotIfNoOneIsWinning;
	}

	public double getCostPerTicket() {
		return costPerTicket;
	}

	public void setCostPerTicket(double costPerTicket) {
		this.costPerTicket = costPerTicket;
	}

	public int getMaximalAmountOfTicketWhichCanAPlayerBuy() {
		return maximalAmountOfTicketWhichCanAPlayerBuy;
	}

	public void setMaximalAmountOfTicketWhichCanAPlayerBuy(int maximalAmountOfTicketWhichCanAPlayerBuy) {
		this.maximalAmountOfTicketWhichCanAPlayerBuy = maximalAmountOfTicketWhichCanAPlayerBuy;
	}

	public LinkedHashSet<DrawTime> getDrawTime() {
		return drawTime;
	}

	public void setDrawTime(LinkedHashSet<DrawTime> drawTime) {
		this.drawTime = drawTime;
	}

	public Collection<WinningClass> getWinningClass() {
		return winningClass;
	}

	public void setWinningClass(Collection<WinningClass> winningClass) {
		this.winningClass = winningClass;
	}

	public int getFristNumberToChooseFrom() {
		return fristNumberToChooseFrom;
	}

	public void setFristNumberToChooseFrom(int fristNumberToChooseFrom) {
		this.fristNumberToChooseFrom = fristNumberToChooseFrom;
	}

	public int getLastNumberToChooseFrom() {
		return lastNumberToChooseFrom;
	}

	public void setLastNumberToChooseFrom(int lastNumberToChooseFrom) {
		this.lastNumberToChooseFrom = lastNumberToChooseFrom;
	}

	public int getAmountOfChoosedNumber() {
		return amountOfChoosedNumber;
	}

	public void setAmountOfChoosedNumber(int amountOfChoosedNumber) {
		this.amountOfChoosedNumber = amountOfChoosedNumber;
	}
	
	public BigInteger getWinningChance()
	{
		//n! / k! (n-k)!
		BigInteger n = Utility.factorial(BigInteger.valueOf(getLastNumberToChooseFrom()));
		BigInteger k = Utility.factorial(BigInteger.valueOf(getAmountOfChoosedNumber()));
		BigInteger d = Utility.factorial(BigInteger.valueOf(getLastNumberToChooseFrom()-getAmountOfChoosedNumber()));
		return n.divide(k.multiply(d));
	}

	public String getDrawOnServer() {
		return drawOnServer;
	}

	public void setDrawOnServer(String drawOnServer) {
		this.drawOnServer = drawOnServer;
	}
	
	public boolean isDrawManually() {
		return drawManually;
	}

	public void setDrawManually(boolean drawManually) {
		this.drawManually = drawManually;
	}

	public ClassicLottoDraw getDrawMysql()
	{
		return new ClassicLottoDraw(getLotteryName());
	}
	
	public ClassicLottoTicket getTicketMysql()
	{
		return new ClassicLottoTicket(getLotteryName());
	}
	
	/*public static void main(String[] args) {
		BigInteger n = Utility.factorial(BigInteger.valueOf(49));
		BigInteger k = Utility.factorial(BigInteger.valueOf(6));
		BigInteger d = Utility.factorial(BigInteger.valueOf(49-6));
		DecimalFormat df2 = new DecimalFormat( "#,###,###,##0.00" );
		System.out.println("Ergebnis: "+df2.format(n.divide(k.multiply(d)).doubleValue()));
	}*/
}