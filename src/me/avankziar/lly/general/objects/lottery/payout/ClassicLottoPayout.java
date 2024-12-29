package me.avankziar.lly.general.objects.lottery.payout;

import java.util.HashSet;
import java.util.UUID;

import me.avankziar.lly.general.objects.WinningClass.PayoutType;

public class ClassicLottoPayout 
{
	private int winningClassLevel;
	private PayoutType payoutType;
	private double payout;
	private HashSet<UUID> uuids = new HashSet<>();
	private int winnersAmount;
	private int numberMatchToWin;
	
	public ClassicLottoPayout(int winningClassLevel, PayoutType payoutType, double payout, 
			HashSet<UUID> uuids, int winnersAmount, int numberMatchToWin)
	{
		setWinningClassLevel(winningClassLevel);
		setPayoutType(payoutType);
		setPayout(payout);
		setUUIDs(uuids);
		setWinnersAmount(winnersAmount);
		setNumberMatchToWin(numberMatchToWin);
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

	public double getPayout() {
		return payout;
	}

	public void setPayout(double payout) {
		this.payout = payout;
	}

	public HashSet<UUID> getUUIDs() {
		return uuids;
	}

	public void setUUIDs(HashSet<UUID> uuids) {
		this.uuids = uuids;
	}
	
	public HashSet<UUID> getUuids() {
		return uuids;
	}

	public void setUuids(HashSet<UUID> uuids) {
		this.uuids = uuids;
	}

	public int getWinnersAmount()
	{
		return winnersAmount;
	}

	public void setWinnersAmount(int winnersAmount)
	{
		this.winnersAmount = winnersAmount;
	}

	public int getNumberMatchToWin() {
		return numberMatchToWin;
	}

	public void setNumberMatchToWin(int numberMatchToWin) {
		this.numberMatchToWin = numberMatchToWin;
	}

	public double getFinalPayoutPerUUID()
	{
		switch(getPayoutType())
		{
		default:
		case LUMP_SUM: return payout;
		case PERCENTAGE: return payout / uuids.size();
		}
	}
}