package me.avankziar.lly.general.objects.lottery.payout;

import me.avankziar.lly.general.objects.ScratchCardField;

public class ScratchCardPayout 
{
	private ScratchCardField scratchCardField;
	/**
	 * Amount of how many times this field was drawn
	 */
	private int amountDrawn;
	
	public ScratchCardPayout(ScratchCardField scratchCardField, int amountDrawn)
	{
		setScratchCardField(scratchCardField);
		setAmountDrawn(amountDrawn);
	}

	public ScratchCardField getScratchCardField() {
		return scratchCardField;
	}

	public void setScratchCardField(ScratchCardField scratchCardField) {
		this.scratchCardField = scratchCardField;
	}

	public int getAmountDrawn() {
		return amountDrawn;
	}

	public void setAmountDrawn(int amountDrawn) {
		this.amountDrawn = amountDrawn;
	}
}