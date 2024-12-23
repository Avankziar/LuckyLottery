package me.avankziar.lly.general.objects.lotterydraw;

public class LotteryDraw
{
	private long id;
	private String lotteryname;
	
	public LotteryDraw(long id, String lotteryname)
	{
		setId(id);
		setLotteryName(lotteryname);
	}
	
	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public String getLotteryName()
	{
		return lotteryname;
	}

	public void setLotteryName(String lotteryname)
	{
		this.lotteryname = lotteryname;
	}

	public String getMysqlTableName()
	{
		return "lly"+getLotteryName()+"Draw";
	}
}
