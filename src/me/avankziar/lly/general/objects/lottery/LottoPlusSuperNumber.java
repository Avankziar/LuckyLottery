package me.avankziar.lly.general.objects.lottery;

import java.util.HashSet;

import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.WinningCategory;

public class LottoPlusSuperNumber extends ClassicLotto
{
	private int additionalFristNumberToChooseFrom;
	private int additionalLastNumberToChooseFrom;
	private int additionalAmountOfChoosenNumber;
	
	public LottoPlusSuperNumber(String lotteryName, String description, GameType gameType,
			double standartPot,	double maximumPot, double amountToAddToThePotIfNoOneIsWinning, double costPerTicket,
			int firstNumberToChooseFrom, int lastNumberToChooseFrom, int amountOfChoosenNumber,
			DrawTime drawTime, HashSet<WinningCategory> winningCategorys) 
	{
		super(lotteryName, description, gameType, standartPot, maximumPot, amountToAddToThePotIfNoOneIsWinning, costPerTicket,
				firstNumberToChooseFrom, lastNumberToChooseFrom, amountOfChoosenNumber, drawTime, winningCategorys);
		setAdditionalFristNumberToChooseFrom(additionalFristNumberToChooseFrom);
		setAdditionalLastNumberToChooseFrom(additionalLastNumberToChooseFrom);
		setAdditionalAmountOfChoosenNumber(additionalAmountOfChoosenNumber);
	}

	public int getAdditionalFristNumberToChooseFrom() {
		return additionalFristNumberToChooseFrom;
	}

	public void setAdditionalFristNumberToChooseFrom(int additionalFristNumberToChooseFrom) {
		this.additionalFristNumberToChooseFrom = additionalFristNumberToChooseFrom;
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

}
