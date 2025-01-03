package me.avankziar.lly.general.objects;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
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
	
	private static LocalDateTime convertToDateTime(DrawTime drawTime, int year, int month) {
	    try {
	        YearMonth yearMonth = YearMonth.of(year, month);
	        LocalDate firstDayOfMonth = yearMonth.atDay(1);
	        int offset = (drawTime.getDayOfWeek().getValue() - firstDayOfMonth.getDayOfWeek().getValue() + 7) % 7 
	                + (drawTime.getWeekOfMonth() - 1) * 7;

	        LocalDate drawDate = firstDayOfMonth.plusDays(offset);
	        if (drawDate.getMonthValue() != month) {
	            throw new DateTimeException("Ungültige Kombination aus Woche und Wochentag: " + drawTime);
	        }

	        return LocalDateTime.of(drawDate, LocalTime.of(drawTime.getHour(), drawTime.getMinute()));
	    } catch (DateTimeException e) {
	        System.err.println("Fehler bei der Konvertierung von DrawTime: " + drawTime);
	        throw e;
	    }
	}
	
	public static String getNow() 
	{
	    LocalDateTime ldt = LocalDateTime.now();
	    LocalDate firstDayOfMonth = ldt.withDayOfMonth(1).toLocalDate();

	    // Wochentag des ersten Tages des Monats
	    DayOfWeek firstDayOfWeek = firstDayOfMonth.getDayOfWeek();
	    int firstWeekdayValue = firstDayOfWeek.getValue(); // 1 = Montag, 7 = Sonntag

	    // Tag des Monats
	    int dayOfMonth = ldt.getDayOfMonth();

	    // Wochennummer des Monats berechnen
	    int weekNumber = ((dayOfMonth + firstWeekdayValue - 2) / 7) + 1;

	    return weekNumber + "-" + ldt.getDayOfWeek().toString() + "-" + ldt.getHour() + "-" + ldt.getMinute();
	}
	
	public boolean isNow(long time)
	{
		return isNow(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
	}

	public boolean isNow(LocalDateTime ldt)
	{
		LocalDateTime nldt = LocalDateTime.now();
		LocalDateTime now = convertToDateTime(this, nldt.getYear(), nldt.getMonthValue());
		return ldt.isEqual(now);
	}
	
	public String toString()
	{
		return weekOfMonth + "-" + dayOfWeek.toString() + "-" + hour + "-" + minute;
	}
}
