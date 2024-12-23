package me.avankziar.lly.general.objects;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class DrawTime 
{
	private DayOfWeek dayOfWeek;
	private int hour;
	private int minute;
	
	public DrawTime(DayOfWeek dayOfWeek, int hour, int minute)
	{
		setDayOfWeek(dayOfWeek);
		setHour(hour);
		setMinute(minute);
	}

	public DayOfWeek getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(DayOfWeek dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getHour() {
		return hour;
	}

	public void setHour(int hour) {
		this.hour = hour;
	}

	public int getMinute() {
		return minute;
	}

	public void setMinute(int minute) {
		this.minute = minute;
	}
	
	public boolean isNow(long time)
	{
		return isNow(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
	}

	public boolean isNow(LocalDateTime dateTime)
	{
		return getDayOfWeek() == dateTime.getDayOfWeek()
				&& getHour() == dateTime.getHour()
				&& getMinute() == dateTime.getMinute();
	}
}
