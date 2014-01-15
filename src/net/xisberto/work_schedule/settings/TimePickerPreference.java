/*******************************************************************************
 * Copyright 2014 xisberto
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.xisberto.work_schedule.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerPreference extends DialogPreference {
	private TimePicker timepicker;
	public static final int DEFAULT_HOUR = 1, DEFAULT_MINUTE = 0;
	public static final String SUFIX_HOUR = ".hour", SUFIX_MINUTE = ".minute";

	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
	}

	@Override
	protected View onCreateDialogView() {
		timepicker = new TimePicker(getContext());
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getContext());
		timepicker.setIs24HourView(true);
		timepicker.setCurrentHour(prefs.getInt(getKey() + SUFIX_HOUR,
				DEFAULT_HOUR));
		timepicker.setCurrentMinute(prefs.getInt(getKey() + SUFIX_MINUTE,
				DEFAULT_MINUTE));
		return timepicker;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			timepicker.clearFocus();
			SharedPreferences.Editor editor = PreferenceManager
					.getDefaultSharedPreferences(getContext()).edit();
			editor.putInt(getKey() + SUFIX_HOUR, timepicker.getCurrentHour());
			editor.putInt(getKey() + SUFIX_MINUTE,
					timepicker.getCurrentMinute());
			Settings.apply(editor);
		}
	}

}
