package me.avankziar.lly.general.objects.lotteryticket;

public class LotteryTicket
{
	/**
	 * ID of this Object
	 */
	private long id;
	/**
	 * ID of the lottodraw
	 */
	private long drawID;
	private String lotteryname;
	
	public LotteryTicket(long id, long drawID, String lotteryname)
	{
		setID(id);
		setDrawID(drawID);
		setLotteryName(lotteryname);
	}
	
	public long getID() {
		return id;
	}

	public void setID(long id) {
		this.id = id;
	}

	public long getDrawID()
	{
		return drawID;
	}

	public void setDrawID(long drawID)
	{
		this.drawID = drawID;
	}

	public String getLotteryName() {
		return lotteryname;
	}

	public void setLotteryName(String lotteryname) {
		this.lotteryname = lotteryname;
	}
	
	public String getMysqlTableName()
	{
		return "lly"+getLotteryName()+"Ticket";
	}
}
