package me.avankziar.lly.general.objects;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.WeekFields;

public class DrawTime 
{
	private int weekOfMonth;
	private DayOfWeek dayOfWeek;
	private int hour;
	private int minute;
	
	public DrawTime(int weekOfMonth, DayOfWeek dayOfWeek, int hour, int minute)
	{
		setWeekOfMonth(weekOfMonth);
		setDayOfWeek(dayOfWeek);
		setHour(hour);
		setMinute(minute);
	}

	public int getWeekOfMonth() {
		return weekOfMonth;
	}

	public void setWeekOfMonth(int weekOfMonth) {
		this.weekOfMonth = weekOfMonth;
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
		int weekNumber = dateTime.get(WeekFields.ISO.weekOfWeekBasedYear());
		return getWeekOfMonth() == weekNumber
				&& getDayOfWeek() == dateTime.getDayOfWeek()
				&& getHour() == dateTime.getHour()
				&& getMinute() == dateTime.getMinute();
	}
}
