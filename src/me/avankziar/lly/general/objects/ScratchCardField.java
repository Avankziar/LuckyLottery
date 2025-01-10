package me.avankziar.lly.general.objects;

import java.util.ArrayList;
import java.util.Random;

public class ScratchCardField
{
	/**
	 * The amount of winning.
	 */
	private double winningAmount;
	/**
	 * The chance to pull this winningamount.
	 */
	private double chance;
	/**
	 * The text will display the number.
	 */
	private String display;
	
	private KeepsakeItem keepsakeItem;
	
	public ScratchCardField(double winningAmount, double chance, String display, KeepsakeItem keepsakeItem)
	{
		setWinningAmount(winningAmount);
		setChance(chance);
		setDisplay(display);
		setKeepsakeItem(keepsakeItem);
	}

	public double getWinningAmount()
	{
		return winningAmount;
	}

	public void setWinningAmount(double winningAmount)
	{
		this.winningAmount = winningAmount;
	}

	public double getChance()
	{
		return chance;
	}

	public void setChance(double chance)
	{
		this.chance = chance;
	}
	
	public String getDisplay()
	{
		return display;
	}

	public void setDisplay(String display)
	{
		this.display = display;
	}

	public KeepsakeItem getKeepsakeItem()
	{
		return keepsakeItem;
	}

	public void setKeepsakeItem(KeepsakeItem keepsakeItem)
	{
		this.keepsakeItem = keepsakeItem;
	}

	public static ScratchCardField roll(ArrayList<ScratchCardField> scfA, int exit)
	{
		Random r = new Random();
		ScratchCardField scf = scfA.get(r.nextInt(scfA.size()));
		if(exit >= 50)
		{
			return scf;
		}
		return (r.nextDouble() * 100.0) < scf.getChance() ? scf : roll(scfA, exit + 1);
	}
}