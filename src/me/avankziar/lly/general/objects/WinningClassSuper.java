package me.avankziar.lly.general.objects;

import java.util.ArrayList;

public class WinningClassSuper extends WinningClass
{
	private int addtionalNumberMatchToWin;
	
	public WinningClassSuper(int winningClassLevel, PayoutType payoutType, double amount, int numberMatchToWin,
			ArrayList<ExecutableCommand> executableCommands, KeepsakeItem keepsakeItem,
			int addtionalNumberMatchToWin)
	{
		super(winningClassLevel, payoutType, amount, numberMatchToWin, executableCommands, keepsakeItem);
		setAddtionalNumberMatchToWin(addtionalNumberMatchToWin);
	}

	public int getAddtionalNumberMatchToWin() {
		return addtionalNumberMatchToWin;
	}

	public void setAddtionalNumberMatchToWin(int addtionalNumberMatchToWin) {
		this.addtionalNumberMatchToWin = addtionalNumberMatchToWin;
	}
}