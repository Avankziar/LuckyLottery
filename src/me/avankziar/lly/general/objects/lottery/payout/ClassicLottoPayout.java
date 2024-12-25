package me.avankziar.lly.general.objects.lottery.payout;

import java.util.HashSet;
import java.util.UUID;

public class ClassicLottoPayout 
{
	private int winningCategoryLevel;
	private double payout;
	private HashSet<UUID> uuids = new HashSet<>();
	
	public ClassicLottoPayout(int winningCategoryLevel, double payout, HashSet<UUID> uuids)
	{
		setWinningCategoryLevel(winningCategoryLevel);
		setPayout(payout);
		setUUIDs(uuids);
	}

	public int getWinningCategoryLevel() {
		return winningCategoryLevel;
	}

	public void setWinningCategoryLevel(int winningCategoryLevel) {
		this.winningCategoryLevel = winningCategoryLevel;
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
	
	public double getFinalPayoutPerUUID()
	{
		return payout / uuids.size();
	}
}