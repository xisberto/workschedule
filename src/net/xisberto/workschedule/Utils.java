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
package net.xisberto.workschedule;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;

public class Utils {

	@SuppressLint("NewApi")
	public static void apply(Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			editor.commit();
		} else {
			editor.apply();
		}
	}
	
	public static void setDefaultPreferenceValues(Context context, SharedPreferences prefs) {
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
	
}
