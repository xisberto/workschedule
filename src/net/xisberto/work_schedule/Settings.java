/*******************************************************************************
 * Copyright (c) 2012 Humberto Fraga <xisberto@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Humberto Fraga <xisberto@gmail.com> - initial API and implementation
 ******************************************************************************/
package net.xisberto.work_schedule;

import java.util.Calendar;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;

public class Settings {
	private Context context;
	private SharedPreferences prefs;

	public enum Period {
		FSTP_ENTRANCE(R.string.fstp_entrance, R.string.lbl_fstp_entrance), FSTP_EXIT(
				R.string.fstp_exit, R.string.lbl_fstp_exit), SNDP_ENTRANCE(
				R.string.sndp_entrance, R.string.lbl_sndp_entrance), SNDP_EXIT(
				R.string.sndp_exit, R.string.lbl_sndp_exit), FSTE_ENTRANCE(
				R.string.fste_entrance, R.string.lbl_fste_entrance), FSTE_EXIT(
				R.string.fste_exit, R.string.lbl_fste_exit), SNDE_ENTRANCE(
				R.string.snde_entrance, R.string.lbl_snde_entrance), SNDE_EXIT(
				R.string.snde_exit, R.string.lbl_snde_exit);

		public int pref_id;
		public int label_id;

		private Period(int pref_id, int label_id) {
			this.pref_id = pref_id;
			this.label_id = label_id;
		}
		
		public static Period getFromPrefId(int pref_id) {
			for (Period period : Period.values()) {
				if (period.pref_id == pref_id) {
					return period;
				}
			}
			return null;
		}

	}
	
	public Settings(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	@SuppressLint("NewApi")
	public static void apply(Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			editor.commit();
		} else {
			editor.apply();
		}
	}
	
	public void setDefaultPreferenceValues() {
		Editor editor = prefs.edit();
		String key_work_time = context.getString(R.string.key_work_time),
				key_fstp_duration = context.getString(R.string.key_fstp_duration),
				key_lunch_interval = context.getString(R.string.key_lunch_interval),
				key_extra_interval = context.getString(R.string.key_extra_interval),
				key_fste_duration = context.getString(R.string.key_fste_duration);
		
		if (! prefs.contains(key_work_time+TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_work_time+TimePickerPreference.SUFIX_HOUR, 8);
			editor.putInt(key_work_time+TimePickerPreference.SUFIX_MINUTE, 0);
		}
		
		if (! prefs.contains(key_fstp_duration+TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_fstp_duration+TimePickerPreference.SUFIX_HOUR, 4);
			editor.putInt(key_fstp_duration+TimePickerPreference.SUFIX_MINUTE, 0);
		}
		
		if (! prefs.contains(key_lunch_interval+TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_lunch_interval+TimePickerPreference.SUFIX_HOUR, 1);
			editor.putInt(key_lunch_interval+TimePickerPreference.SUFIX_MINUTE, 0);
		}
		
		if (! prefs.contains(key_extra_interval+TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_extra_interval+TimePickerPreference.SUFIX_HOUR, 0);
			editor.putInt(key_extra_interval+TimePickerPreference.SUFIX_MINUTE, 15);
		}
		
		if (! prefs.contains(key_fste_duration+TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_fste_duration+TimePickerPreference.SUFIX_HOUR, 1);
			editor.putInt(key_fste_duration+TimePickerPreference.SUFIX_MINUTE, 0);
		}
		
		apply(editor);
	}
	
	public Calendar getCalendarFromTime(int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	public Calendar getCalendar(String key) {
		int hour = prefs.getInt(key + TimePickerPreference.SUFIX_HOUR,
				TimePickerPreference.DEFAULT_HOUR);
		int minute = prefs.getInt(key + TimePickerPreference.SUFIX_MINUTE,
				TimePickerPreference.DEFAULT_MINUTE);
		return getCalendarFromTime(hour, minute);
	}
	
	public Calendar getCalendar(int pref_id) {
		return getCalendar(context.getString(pref_id));
	}
	
	public String formatCalendar(Calendar cal) {
		String inFormat = "hh:mm aa";
		if (DateFormat.is24HourFormat(context)) {
			inFormat = "kk:mm";
		}
		return DateFormat.format(inFormat, cal).toString();
	}

	public void saveCalendar(Calendar cal, String key) {
		Editor editor = prefs
				.edit()
				.putInt(key + TimePickerPreference.SUFIX_HOUR,
						cal.get(Calendar.HOUR_OF_DAY))
				.putInt(key + TimePickerPreference.SUFIX_MINUTE,
						cal.get(Calendar.MINUTE));
		apply(editor);
	}
	
	public boolean isAlarmSet(int period_pref_id) {
		return prefs.getBoolean(context.getString(period_pref_id)+".isset", false);
	}
	
	/**
	 * Set a new alarm
	 * 
	 * @param context
	 *            the {@link Context} in which the alarm will start
	 * @param period
	 *            the {@link Period} related to the alarm
	 * @param cal
	 *            the time when the alarm will start
	 */
	public void setAlarm(Period period, Calendar cal, boolean enabled) {
		//Save the time and the status to the SharedPreferences
		Editor editor = prefs.edit();
		saveCalendar(cal, context.getString(period.pref_id));
		editor.putBoolean(context.getString(period.pref_id) + ".isset", enabled);
		apply(editor);
		
		//Set or cancel the alarm
		String time = formatCalendar(cal);
		Intent intentAlarm = new Intent(context, AlarmReceiver.class);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID,
				period.pref_id);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_TIME, time);
		PendingIntent alarmSender = PendingIntent
				.getBroadcast(context, period.pref_id, intentAlarm,
						PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		if (enabled) {
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmSender);	
		} else {
			am.cancel(alarmSender);
		}
		
	}
	
	public void unsetAlarm(Period period) {
		setAlarm(period, getCalendar(period.pref_id), false);
	}

}
