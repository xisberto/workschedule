package net.xisberto.work_schedule;

import java.util.Calendar;

public class Utils {


	/**
	 * Builds a {@link Calendar} with date equals as the actual day, hour and
	 * minute as specified and seconds and milliseconds set to zero. When on
	 * debug (BuildConfig.DEBUG), seconds are set to the next value.
	 * 
	 * @param hour
	 *            the hour for the {@link Calendar}
	 * @param minute
	 *            the minute for the {@link Calendar}
	 * @return the {@link Calendar} built
	 */
	public static Calendar getCalendarFromTime(int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		if (!BuildConfig.DEBUG) {
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		} else {
			cal.set(Calendar.SECOND, cal.get(Calendar.SECOND) + 1);
		}
		return cal;
	}
	
}
