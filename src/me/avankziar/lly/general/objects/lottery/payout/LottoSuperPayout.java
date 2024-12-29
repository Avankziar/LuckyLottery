package me.avankziar.lly.general.objects.lottery.payout;

import java.util.HashSet;
import java.util.UUID;

import me.avankziar.lly.general.objects.WinningClass.PayoutType;

public class LottoSuperPayout extends ClassicLottoPayout
{
	private int addtionalNumberMatchToWin;
	
	public LottoSuperPayout(int winningCategoryLevel, PayoutType payoutType, double payout, 
			HashSet<UUID> uuids, int numberMatchToWin, int addtionalNumberMatchToWin)
	{
		super(winningCategoryLevel, payoutType, payout, uuids, 0, numberMatchToWin);
		setAddtionalNumberMatchToWin(addtionalNumberMatchToWin);
	}

	public int getAddtionalNumberMatchToWin() {
		return addtionalNumberMatchToWin;
	}

	public void setAddtionalNumberMatchToWin(int addtionalNumberMatchToWin) {
		this.addtionalNumberMatchToWin = addtionalNumberMatchToWin;
	}
}