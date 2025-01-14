package me.avankziar.lly.general.objects.lottery;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;

import me.avankziar.lly.general.objects.Advertising;
import me.avankziar.lly.general.objects.ScratchCardField;
import me.avankziar.lly.general.objects.lottery.ticket.ScratchCardTicket;

public class ScratchCard extends Lottery
{
	/**
	 * The amount of money to buy on lottery ticket.
	 */
	private double costPerTicket;
	/**
	 * The amount of field to scratch to draw
	 */
	private int amountOfFields;
	/**
	 * The amount of the same field on the display to win said field amount.
	 */
	private int amountOfSameFieldToWin;
	/**
	 * The pool of fields with the winningamount and chance
	 */
	private ArrayList<ScratchCardField> scratchCardFields = new ArrayList<>();
	/**
	 * The jackpot amount. Will be drawn of @scratchCardField.
	 */
	private ScratchCardField jackpot;
	/**
	 * The value witch will act as joker.<br>
	 * The Joker will be count once as twin of the highest drawn field.<br>
	 * Joker only will be drawn, if you have @amountOfSameFieldToWin >= 1!
	 */
	private ScratchCardField joker = new ScratchCardField(-1.0, 0.0, "", null);
	
	private ArrayList<Advertising> advertising = new ArrayList<>();
	
	private int fieldPerLine;
	
	private String displayFieldUnscratched;
	
	private String displayFieldScratched;
	
	private String displayHeadLine;
	private String displayBetweenLine;
	private String displayBottomLine;
	
	public ScratchCard(String lotteryName, String description, GameType gameType,
			double costPerTicket,
			int amountOfFields, int amountOfSameFieldToWin, ArrayList<ScratchCardField> scratchCardFields,
			ArrayList<Advertising> advertising,
			int fieldPerLine, String displayFieldUnscratched, String displayFieldScratched,
			String displayHeadLine, String displayBetweenLine, String displayBottomLine)
	{
		super(lotteryName, description, gameType);
		setCostPerTicket(costPerTicket);
		setAmountOfFields(amountOfFields);
		setAmountOfSameFieldToWin(amountOfSameFieldToWin);
		setScratchCardFields(scratchCardFields);
		ScratchCardField jackpot = null;
		for(ScratchCardField x : scratchCardFields)
		{
			jackpot = jackpot == null ? x : (jackpot.getWinningAmount() < x.getWinningAmount() ? x : jackpot);
			if(x.getWinningAmount() == -1.0)
			{
				joker = x;
			}
		}
		setJackpot(jackpot);
		setAdvertising(advertising);
		setFieldPerLine(fieldPerLine);
		setDisplayFieldUnscratched(displayFieldUnscratched);
		setDisplayFieldScratched(displayFieldScratched);
		setDisplayHeadLine(displayHeadLine);
		setDisplayBetweenLine(displayBetweenLine);
		setDisplayBottomLine(displayBottomLine);
	}

	public double getCostPerTicket()
	{
		return costPerTicket;
	}

	public void setCostPerTicket(double costPerTicket)
	{
		this.costPerTicket = costPerTicket;
	}

	public int getAmountOfFields()
	{
		return amountOfFields;
	}

	public void setAmountOfFields(int amountOfFields)
	{
		this.amountOfFields = amountOfFields;
	}

	public int getAmountOfSameFieldToWin()
	{
		return amountOfSameFieldToWin;
	}

	public void setAmountOfSameFieldToWin(int amountOfSameFieldToWin)
	{
		this.amountOfSameFieldToWin = amountOfSameFieldToWin;
	}

	public ArrayList<ScratchCardField> getScratchCardFields()
	{
		return scratchCardFields;
	}

	public void setScratchCardFields(ArrayList<ScratchCardField> scratchCardFields)
	{
		this.scratchCardFields = scratchCardFields;
	}

	public ScratchCardField getJackpot()
	{
		return jackpot;
	}

	public void setJackpot(ScratchCardField jackpot)
	{
		this.jackpot = jackpot;
	}

	public ScratchCardField getJoker()
	{
		return joker;
	}

	public ArrayList<Advertising> getAdvertising()
	{
		return advertising;
	}

	public int getFieldPerLine()
	{
		return fieldPerLine;
	}

	public void setFieldPerLine(int fieldPerLine)
	{
		this.fieldPerLine = fieldPerLine;
	}

	public String getDisplayFieldUnscratched()
	{
		return displayFieldUnscratched;
	}

	public void setDisplayFieldUnscratched(String displayFieldUnscratched)
	{
		this.displayFieldUnscratched = displayFieldUnscratched;
	}

	public String getDisplayFieldScratched()
	{
		return displayFieldScratched;
	}

	public void setDisplayFieldScratched(String displayFieldScratched)
	{
		this.displayFieldScratched = displayFieldScratched;
	}

	public String getDisplayHeadLine()
	{
		return displayHeadLine;
	}

	public void setDisplayHeadLine(String displayHeadLine)
	{
		this.displayHeadLine = displayHeadLine;
	}

	public String getDisplayBetweenLine()
	{
		return displayBetweenLine;
	}

	public void setDisplayBetweenLine(String displayBetweenLine)
	{
		this.displayBetweenLine = displayBetweenLine;
	}

	public String getDisplayBottomLine()
	{
		return displayBottomLine;
	}

	public void setDisplayBottomLine(String displayBottomLine)
	{
		this.displayBottomLine = displayBottomLine;
	}

	public void setAdvertising(ArrayList<Advertising> advertising)
	{
		this.advertising = advertising;
	}
	
	public String getWinningChance()
	{
		BigDecimal scf = BigDecimal.valueOf(getScratchCardFields().size());
		BigDecimal jp = BigDecimal.valueOf(jackpot.getChance());
		BigDecimal aosf = BigDecimal.valueOf(getAmountOfSameFieldToWin());
		DecimalFormat df = new DecimalFormat("#");
		return df.format(aosf.multiply(scf.multiply(BigDecimal.valueOf(100).divide(jp))).doubleValue());
	}

	public ScratchCardTicket getTicketMysql()
	{
		return new ScratchCardTicket(getLotteryName());
	}
}