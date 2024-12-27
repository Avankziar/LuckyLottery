package me.avankziar.lly.general.objects;

public class WinningClassSuper extends WinningClass
{
	private int addtionalNumberMatchToWin;
	
	public WinningClassSuper(int winningClassLevel, PayoutType payoutType, double amount, int numberMatchToWin,
			int addtionalNumberMatchToWin)
	{
		super(winningClassLevel, payoutType, amount, numberMatchToWin);
		setAddtionalNumberMatchToWin(addtionalNumberMatchToWin);
	}

	public int getAddtionalNumberMatchToWin() {
		return addtionalNumberMatchToWin;
	}

	public void setAddtionalNumberMatchToWin(int addtionalNumberMatchToWin) {
		this.addtionalNumberMatchToWin = addtionalNumberMatchToWin;
	}
}