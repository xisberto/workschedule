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
package net.xisberto.work_schedule.settings;

import java.util.Calendar;

import net.xisberto.work_schedule.BuildConfig;
import net.xisberto.work_schedule.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.preference.PreferenceManager;

public class Settings {
	public static final String EXTRA_PERIOD_LABEL = "period_label",
			EXTRA_PREF_ID = "pref_id", EXTRA_PERIOD_TIME = "period_time";

	private static Settings instance = null;

	private Context context;
	private SharedPreferences prefs;

	private Settings(Context context) {
		this.context = context;
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static Settings getInstance(Context context) {
		if (instance == null) {
			instance = new Settings(context);
		}
		return instance;
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
		String key_work_time = context.getString(R.string.key_work_time), key_fstp_duration = context
				.getString(R.string.key_fstp_duration), key_lunch_interval = context
				.getString(R.string.key_lunch_interval), key_extra_interval = context
				.getString(R.string.key_extra_interval), key_fste_duration = context
				.getString(R.string.key_fste_duration), key_snooze_increment = context
				.getString(R.string.key_snooze_increment), key_mark_extra = context
				.getString(R.string.key_mark_extra), key_vibrate = context
				.getString(R.string.key_vibrate);

		if (!prefs.contains(key_work_time + TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_work_time + TimePickerPreference.SUFIX_HOUR, 8);
			editor.putInt(key_work_time + TimePickerPreference.SUFIX_MINUTE, 0);
		}

		if (!prefs
				.contains(key_fstp_duration + TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_fstp_duration + TimePickerPreference.SUFIX_HOUR,
					4);
			editor.putInt(
					key_fstp_duration + TimePickerPreference.SUFIX_MINUTE, 0);
		}

		if (!prefs.contains(key_lunch_interval
				+ TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_lunch_interval + TimePickerPreference.SUFIX_HOUR,
					1);
			editor.putInt(key_lunch_interval
					+ TimePickerPreference.SUFIX_MINUTE, 0);
		}

		if (!prefs.contains(key_extra_interval
				+ TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_extra_interval + TimePickerPreference.SUFIX_HOUR,
					0);
			editor.putInt(key_extra_interval
					+ TimePickerPreference.SUFIX_MINUTE, 15);
		}

		if (!prefs
				.contains(key_fste_duration + TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_fste_duration + TimePickerPreference.SUFIX_HOUR,
					1);
			editor.putInt(
					key_fste_duration + TimePickerPreference.SUFIX_MINUTE, 0);
		}

		if (!prefs.contains(key_snooze_increment
				+ TimePickerPreference.SUFIX_HOUR)) {
			editor.putInt(key_snooze_increment
					+ TimePickerPreference.SUFIX_HOUR, 0);
			editor.putInt(key_snooze_increment
					+ TimePickerPreference.SUFIX_MINUTE, 10);
		}

		if (!prefs.contains(key_mark_extra)) {
			editor.putBoolean(key_mark_extra, false);
		}

		if (!prefs.contains(key_vibrate)) {
			editor.putBoolean(key_vibrate, true);
		}

		apply(editor);
	}

	/**
	 * Returns the Calendar with the alarm for a preference.
	 * 
	 * @param key
	 *            the key of the preference
	 * @return the Calendar for this preference. The date is the actual day, and
	 *         the hour and minute are the specified on the key (see
	 *         {@link TimePickerPreference} to Work Schedule's time
	 *         preferences).
	 */
	public Calendar getCalendar(String key) {
		int hour = prefs.getInt(key + TimePickerPreference.SUFIX_HOUR,
				TimePickerPreference.DEFAULT_HOUR);
		int minute = prefs.getInt(key + TimePickerPreference.SUFIX_MINUTE,
				TimePickerPreference.DEFAULT_MINUTE);
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

	/**
	 * Return a Calendar with the alarm for a preference
	 * 
	 * @param pref_id
	 *            the id for the preference, as defined on the project's
	 *            resources.
	 * @return the Calendar for this preference. The date is the actual day, and
	 *         the hour and minute are the specified on the key (see
	 *         {@link TimePickerPreference} to Work Schedule's time
	 *         preferences).
	 */
	public Calendar getCalendar(int pref_id) {
		return getCalendar(context.getString(pref_id));
	}

	public String getRingtone() {
		return prefs.getString(context.getString(R.string.key_ringtone), null);
	}

	public boolean getMarkExtra() {
		return prefs.getBoolean(context.getString(R.string.key_mark_extra),
				false);
	}

	public boolean getVibrate() {
		return prefs.getBoolean(context.getString(R.string.key_vibrate), true);
	}

	public boolean getShowInstructions() {
		return prefs.getBoolean(
				context.getString(R.string.key_show_instructions), true);
	}
	
	public void setShowInstructions(boolean value) {
		Editor editor = prefs.edit().putBoolean(
				context.getString(R.string.key_show_instructions), value);
		apply(editor);
	}

}
