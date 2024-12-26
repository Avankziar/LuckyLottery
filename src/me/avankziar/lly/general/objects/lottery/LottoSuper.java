package me.avankziar.lly.general.objects.lottery;

import java.util.HashSet;
import java.util.LinkedHashSet;

import me.avankziar.lly.general.objects.DrawTime;
import me.avankziar.lly.general.objects.WinningCategory;

public class LottoSuper extends ClassicLotto
{
	private int additionalFristNumberToChooseFrom;
	private int additionalLastNumberToChooseFrom;
	private int additionalAmountOfChoosenNumber;
	
	public LottoSuper(String lotteryName, String description, GameType gameType,
			double standartPot,	double maximumPot, double amountToAddToThePotIfNoOneIsWinning, double costPerTicket,
			int maximalAmountOfTicketWhichCanAPlayerBuy,
			int firstNumberToChooseFrom, int lastNumberToChooseFrom, int amountOfChoosenNumber,
			LinkedHashSet<DrawTime> drawTime, HashSet<WinningCategory> winningCategorys,
			String drawOnServer, boolean drawManually,
			int additionalFristNumberToChooseFrom,
			int additionalLastNumberToChooseFrom,
			int additionalAmountOfChoosenNumber) 
	{
		super(lotteryName, description, gameType,
				standartPot, maximumPot,amountToAddToThePotIfNoOneIsWinning,
				costPerTicket, maximalAmountOfTicketWhichCanAPlayerBuy,
				firstNumberToChooseFrom, lastNumberToChooseFrom, amountOfChoosenNumber,
				drawTime, winningCategorys, drawOnServer, drawManually);
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
