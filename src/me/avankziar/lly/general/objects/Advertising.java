package me.avankziar.lly.general.objects;

import java.util.ArrayList;

public class Advertising
{
	private boolean active;
	private boolean canIgnored;
	private ArrayList<String> message = new ArrayList<>();
	private ArrayList<DrawTime> drawTime = new ArrayList<>();
	
	public Advertising(boolean active, boolean canIgnored, ArrayList<String> message, ArrayList<DrawTime> drawTime)
	{
		setActive(active);
		setCanIgnored(canIgnored);
		setMessage(message);
		setDrawTime(drawTime);
	}

	public boolean isActive()
	{
		return active;
	}

	public void setActive(boolean active)
	{
		this.active = active;
	}

	public boolean canIgnored()
	{
		return canIgnored;
	}

	public void setCanIgnored(boolean canIgnored)
	{
		this.canIgnored = canIgnored;
	}

	public ArrayList<String> getMessage()
	{
		return message;
	}

	public void setMessage(ArrayList<String> message)
	{
		this.message = message;
	}

	public ArrayList<DrawTime> getDrawTime()
	{
		return drawTime;
	}

	public void setDrawTime(ArrayList<DrawTime> drawTime)
	{
		this.drawTime = drawTime;
	}
}
