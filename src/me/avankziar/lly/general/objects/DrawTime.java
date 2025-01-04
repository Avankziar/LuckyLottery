package me.avankziar.lly.general.objects;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Collections;

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
	
	public static String getNow(LocalDateTime ldt) 
	{
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
	
	public static String getNow() 
	{
	    LocalDateTime ldt = LocalDateTime.now();
	    return getNow(ldt);
	}
	
	public boolean isNow(long time)
	{
		return isNow(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
	}

	public boolean isNow(LocalDateTime ldt)
	{
		LocalDateTime today = ldt == null ? LocalDateTime.now() : ldt; // aktuelles Datum
        int weekOfMonth = (today.getDayOfMonth() - 1) / 7 + 1; // Woche im Monat berechnen
        return weekOfMonth == this.getWeekOfMonth() && today.getDayOfWeek() == this.getDayOfWeek()
        		&& ldt.getHour() == this.getHour() && this.getMinute() == ldt.getMinute();
	}
	
	public static LocalDateTime getNextTime(ArrayList<DrawTime> drawTimes, LocalDateTime lDT) 
	{
	    LocalDateTime ldt = lDT == null ? LocalDateTime.now() : lDT;
	    return drawTimes.stream()
	            .map(drawTime -> 
	            {
	                try {
	                    int weekOfMonth = (ldt.getDayOfMonth() - 1) / 7 + 1; // Woche im Monat berechnen

	                    if (weekOfMonth == drawTime.getWeekOfMonth()) {
	                        if (ldt.getDayOfWeek() == drawTime.getDayOfWeek()) {
	                            // Datum wäre heute.
	                            return LocalDateTime.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth(),
	                                    drawTime.getHour(), drawTime.getMinute());
	                        }
	                        if (drawTime.getDayOfWeek().getValue() > ldt.getDayOfWeek().getValue()) {
	                            // Datum ist in ein paar Tagen in derselben Woche.
	                            return ldt.with(drawTime.getDayOfWeek())
	                                    .withHour(drawTime.getHour())
	                                    .withMinute(drawTime.getMinute());
	                        }
	                        if (drawTime.getDayOfWeek().getValue() < ldt.getDayOfWeek().getValue()) {
	                            // Datum war bereits diese Woche, also im nächsten Monat zur gleichen Wochenzahl.
	                            LocalDateTime nextMonthDate = ldt.plusMonths(1)
	                                    .withDayOfMonth(1)
	                                    .with(TemporalAdjusters.dayOfWeekInMonth(drawTime.getWeekOfMonth(), drawTime.getDayOfWeek()))
	                                    .withHour(drawTime.getHour())
	                                    .withMinute(drawTime.getMinute());
	                            return nextMonthDate;
	                        }
	                    }

	                    if (drawTime.getWeekOfMonth() > weekOfMonth) {
	                        // Datum ist später im selben Monat.
	                        return ldt.with(TemporalAdjusters.dayOfWeekInMonth(drawTime.getWeekOfMonth(), drawTime.getDayOfWeek()))
	                                .withHour(drawTime.getHour())
	                                .withMinute(drawTime.getMinute());
	                    }
	                    if (drawTime.getWeekOfMonth() < weekOfMonth) {
	                        // Datum ist im nächsten Monat.
	                        LocalDateTime nextMonthDate = ldt.plusMonths(1)
	                                .withDayOfMonth(1)
	                                .with(TemporalAdjusters.dayOfWeekInMonth(drawTime.getWeekOfMonth(), drawTime.getDayOfWeek()))
	                                .withHour(drawTime.getHour())
	                                .withMinute(drawTime.getMinute());
	                        return nextMonthDate;
	                    }
	                    // Rückgabe null, falls keine Bedingung zutrifft (sollte nicht passieren).
	                    return null;
	                } catch (DateTimeException e) {
	                    return null; // Ignoriere ungültige Zeiten
	                }
	            })
	            .filter(dateTime -> dateTime != null && dateTime.isAfter(ldt))
	            .min(LocalDateTime::compareTo)
	            .orElse(null); // Falls keine zukünftige Ziehung gefunden wird
	}
	
	public static ArrayList<LocalDateTime> getNextTimes(ArrayList<DrawTime> drawTimes, LocalDateTime lDT) 
	{
	    LocalDateTime ldt = lDT == null ? LocalDateTime.now() : lDT;
	    ArrayList<LocalDateTime> ldtA = new ArrayList<>();
	    drawTimes.stream()
	            .map(drawTime -> 
	            {
	                try {
	                    int weekOfMonth = (ldt.getDayOfMonth() - 1) / 7 + 1; // Woche im Monat berechnen

	                    if (weekOfMonth == drawTime.getWeekOfMonth()) {
	                        if (ldt.getDayOfWeek() == drawTime.getDayOfWeek()) {
	                            // Datum wäre heute.
	                            return LocalDateTime.of(ldt.getYear(), ldt.getMonth(), ldt.getDayOfMonth(),
	                                    drawTime.getHour(), drawTime.getMinute());
	                        }
	                        if (drawTime.getDayOfWeek().getValue() > ldt.getDayOfWeek().getValue()) {
	                            // Datum ist in ein paar Tagen in derselben Woche.
	                            return ldt.with(drawTime.getDayOfWeek())
	                                    .withHour(drawTime.getHour())
	                                    .withMinute(drawTime.getMinute());
	                        }
	                        if (drawTime.getDayOfWeek().getValue() < ldt.getDayOfWeek().getValue()) {
	                            // Datum war bereits diese Woche, also im nächsten Monat zur gleichen Wochenzahl.
	                            LocalDateTime nextMonthDate = ldt.plusMonths(1)
	                                    .withDayOfMonth(1)
	                                    .with(TemporalAdjusters.dayOfWeekInMonth(drawTime.getWeekOfMonth(), drawTime.getDayOfWeek()))
	                                    .withHour(drawTime.getHour())
	                                    .withMinute(drawTime.getMinute());
	                            return nextMonthDate;
	                        }
	                    }

	                    if (drawTime.getWeekOfMonth() > weekOfMonth) {
	                        // Datum ist später im selben Monat.
	                        return ldt.with(TemporalAdjusters.dayOfWeekInMonth(drawTime.getWeekOfMonth(), drawTime.getDayOfWeek()))
	                                .withHour(drawTime.getHour())
	                                .withMinute(drawTime.getMinute());
	                    }
	                    if (drawTime.getWeekOfMonth() < weekOfMonth) {
	                        // Datum ist im nächsten Monat.
	                        LocalDateTime nextMonthDate = ldt.plusMonths(1)
	                                .withDayOfMonth(1)
	                                .with(TemporalAdjusters.dayOfWeekInMonth(drawTime.getWeekOfMonth(), drawTime.getDayOfWeek()))
	                                .withHour(drawTime.getHour())
	                                .withMinute(drawTime.getMinute());
	                        return nextMonthDate;
	                    }
	                    // Rückgabe null, falls keine Bedingung zutrifft (sollte nicht passieren).
	                    return null;
	                } catch (DateTimeException e) {
	                    return null; // Ignoriere ungültige Zeiten
	                }
	            })
	            .filter(dateTime -> dateTime != null && dateTime.isAfter(ldt))
	            .forEach(x -> ldtA.add(x));
	    Collections.sort(ldtA);
	    return ldtA;
	}
	
	public String toString()
	{
		return weekOfMonth + "-" + dayOfWeek.toString() + "-" + hour + "-" + minute;
	}
}
