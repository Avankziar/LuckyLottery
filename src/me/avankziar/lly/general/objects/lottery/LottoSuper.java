package me.avankziar.lly.general.objects.lottery;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;

import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.WinningClassSuper;
import me.avankziar.lly.general.objects.lottery.draw.LottoSuperDraw;
import me.avankziar.lly.general.objects.lottery.ticket.LottoSuperTicket;

public class LottoSuper extends Lottery
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
	private int firstNumberToChooseFrom;
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
	 * The winningCategorys of the lotto.<br>
	 * The higher the level of the winningcategory is, the higher the price should be.
	 */
	private Collection<WinningClassSuper> winningClassSuper = new HashSet<>();
	/**
	 * If true, the classiclotto will not draw automaticlly on the drawtimes.
	 */
	private boolean drawManually;
	
	private String drawOnServer;
	private int additionalFirstNumberToChooseFrom;
	private int additionalLastNumberToChooseFrom;
	private int additionalAmountOfChoosenNumber;
	
	public LottoSuper(String lotteryName, String description, GameType gameType,
			double standartPot,	double maximumPot, double amountToAddToThePotIfNoOneIsWinning, double costPerTicket,
			int maximalAmountOfTicketWhichCanAPlayerBuy,
			int firstNumberToChooseFrom, int lastNumberToChooseFrom, int amountOfChoosenNumber,
			LinkedHashSet<DrawTime> drawTime, HashSet<WinningClassSuper> winningClassSuper,
			String drawOnServer, boolean drawManually,
			int additionalFirstNumberToChooseFrom,
			int additionalLastNumberToChooseFrom,
			int additionalAmountOfChoosenNumber) 
	{
		super(lotteryName, description, gameType);
		setStandartPot(standartPot);
		setMaximumPot(maximumPot);
		setAmountToAddToThePotIfNoOneIsWinning(amountToAddToThePotIfNoOneIsWinning);
		setCostPerTicket(costPerTicket);
		setMaximalAmountOfTicketWhichCanAPlayerBuy(maximalAmountOfTicketWhichCanAPlayerBuy);
		setFirstNumberToChooseFrom(firstNumberToChooseFrom);
		setLastNumberToChooseFrom(lastNumberToChooseFrom);
		setAmountOfChoosedNumber(amountOfChoosenNumber);
		setDrawTime(drawTime);
		setWinningClassSuper(winningClassSuper);
		setDrawOnServer(drawOnServer);
		setDrawManually(drawManually);
		setAdditionalFirstNumberToChooseFrom(additionalFirstNumberToChooseFrom);
		setAdditionalLastNumberToChooseFrom(additionalLastNumberToChooseFrom);
		setAdditionalAmountOfChoosenNumber(additionalAmountOfChoosenNumber);
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

	public Collection<WinningClassSuper> getWinningClassSuper() {
		return winningClassSuper;
	}

	public void setWinningClassSuper(Collection<WinningClassSuper> winningClassSuper) {
		this.winningClassSuper = winningClassSuper;
	}

	public int getFirstNumberToChooseFrom() {
		return firstNumberToChooseFrom;
	}

	public void setFirstNumberToChooseFrom(int firstNumberToChooseFrom) {
		this.firstNumberToChooseFrom = firstNumberToChooseFrom;
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

	public void setAmountOfChoosedNumber(int amountOfChoosedNumber) 
	{
		this.amountOfChoosedNumber = amountOfChoosedNumber;
	}
	
	public BigInteger getWinningChance()
	{
		int mainMatches = amountOfChoosedNumber;
		int totalMainNumbers = lastNumberToChooseFrom - (firstNumberToChooseFrom == 1 ? 0 : firstNumberToChooseFrom-1);
		int requiredMainNumbers = amountOfChoosedNumber;
		int additionalMatches = additionalAmountOfChoosenNumber;
		int totalAdditionalNumbers = additionalLastNumberToChooseFrom - (additionalFirstNumberToChooseFrom == 1 ? 0 : additionalFirstNumberToChooseFrom-1);
		int requiredAdditionalNumbers = additionalAmountOfChoosenNumber;
		// Günstige Kombinationen für Hauptzahlen
	    return calculateOdds(totalMainNumbers, totalAdditionalNumbers, 
	    		requiredMainNumbers, requiredAdditionalNumbers, mainMatches, additionalMatches);
	}
	
	public BigInteger calculateOdds(
	        int totalMainNumbers, int totalAdditionalNumbers, 
	        int requiredMainNumbers, int requiredAdditionalNumbers, 
	        int mainMatches, int additionalMatches) {
	    
	    // Günstige Kombinationen für Hauptzahlen
	    BigInteger favorableMainCombinations = binomialCoefficient(mainMatches, mainMatches);
	    BigInteger remainingMainCombinations = binomialCoefficient(totalMainNumbers - mainMatches, requiredMainNumbers - mainMatches);

	    // Günstige Kombinationen für Zusatzzahlen
	    BigInteger favorableAdditionalCombinations = binomialCoefficient(additionalMatches, additionalMatches);
	    BigInteger remainingAdditionalCombinations = binomialCoefficient(totalAdditionalNumbers - additionalMatches, requiredAdditionalNumbers - additionalMatches);

	    // Gesamtanzahl aller Kombinationen
	    BigInteger totalCombinations = binomialCoefficient(totalMainNumbers, requiredMainNumbers)
	                                   .multiply(binomialCoefficient(totalAdditionalNumbers, requiredAdditionalNumbers));

	    // Günstige Ergebnisse
	    BigInteger favorableResults = favorableMainCombinations
	                                    .multiply(favorableAdditionalCombinations)
	                                    .multiply(remainingMainCombinations)
	                                    .multiply(remainingAdditionalCombinations);

	    // Berechnung der Gewinnchancen
	    return totalCombinations.divide(favorableResults);
	}
	
	// Methode zur Berechnung des Binomialkoeffizienten (n über k)
	public static BigInteger binomialCoefficient(int n, int k) 
	{
		if (k > n || k < 0) 
		{
		    return BigInteger.ZERO;
		}
        BigInteger numerator = BigInteger.ONE;
        BigInteger denominator = BigInteger.ONE;

        // Numerator = n * (n-1) * ... * (n-k+1)
        for (int i = 0; i < k; i++) {
            numerator = numerator.multiply(BigInteger.valueOf(n - i));
            denominator = denominator.multiply(BigInteger.valueOf(i + 1));
        }

        return numerator.divide(denominator);
    }
    
    /*public static void main(String[] args) 
    {
    	LottoSuper ls = new LottoSuper("", "", null, 0, 0, 0, 0, 0, 1, 50, 5, null, null, null, false, 1, 10, 2);
    	System.out.println("Gewinnchance 1:"+ls.getWinningChance());
    	int totalMainNumbers = 50;
        int totalAdditionalNumbers = 10;

        // Gewinnklassen des Eurojackpots
        int[][] euroJackpotClasses = {
            {5, 2}, // Gewinnklasse 1
            {5, 1}, // Gewinnklasse 2
            {5, 0}, // Gewinnklasse 3
            {4, 2}, // Gewinnklasse 4
            {4, 1}, // Gewinnklasse 5
            {4, 0}, // Gewinnklasse 6
            {3, 2}, // Gewinnklasse 7
            {3, 1}, // Gewinnklasse 8
            {3, 0}, // Gewinnklasse 9
            {2, 2}, // Gewinnklasse 10
            {2, 1}, // Gewinnklasse 11
            {2, 0}  // Gewinnklasse 12
        };

        int mainMatches = 5;
        int additionalMatches = 2;
        System.out.println("Gewinnchancen für EuroJackpot:");
        BigInteger odds = ls.calculateOdds(totalMainNumbers, totalAdditionalNumbers,
        		mainMatches, additionalMatches,mainMatches, additionalMatches
        		);
        System.out.printf("Gewinnklasse %d (%d Hauptzahlen, %d Zusatzzahlen): 1 zu %s%n", 
                          1, mainMatches, additionalMatches, odds.toString());
    }*/

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

	public int getAdditionalFirstNumberToChooseFrom() {
		return additionalFirstNumberToChooseFrom;
	}

	public void setAdditionalFirstNumberToChooseFrom(int additionalFirstNumberToChooseFrom) {
		this.additionalFirstNumberToChooseFrom = additionalFirstNumberToChooseFrom;
	}

	public int getAdditionalLastNumberToChooseFrom() {
		return additionalLastNumberToChooseFrom;
	}

	public void setAdditionalLastNumberToChooseFrom(int additionalLastNumberToChooseFrom) {
		this.additionalLastNumberToChooseFrom = additionalLastNumberToChooseFrom;
	}

	public int getAdditionalAmountOfChoosenNumber() {
		return additionalAmountOfChoosenNumber;
	}

	public void setAdditionalAmountOfChoosenNumber(int additionalAmountOfChoosenNumber) {
		this.additionalAmountOfChoosenNumber = additionalAmountOfChoosenNumber;
	}
	
	public LottoSuperDraw getDrawMysql()
	{
		return new LottoSuperDraw(getLotteryName());
	}
	
	public LottoSuperTicket getTicketMysql()
	{
		return new LottoSuperTicket(getLotteryName());
	}
	
}
