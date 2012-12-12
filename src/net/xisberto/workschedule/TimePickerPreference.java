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

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class TimePickerPreference extends DialogPreference {
	private TimePicker timepicker;
	private static final int DEFAULT_HOUR = 1, DEFAULT_MINUTE = 0;

	public TimePickerPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		setPersistent(false);
	}

	@Override
	protected View onCreateDialogView() {
		timepicker = new TimePicker(getContext());
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getContext());
		timepicker.setIs24HourView(DateFormat.is24HourFormat(getContext()));
		timepicker.setCurrentHour(prefs.getInt(getKey()+".hour", DEFAULT_HOUR));
		timepicker.setCurrentMinute(prefs.getInt(getKey()+".minute", DEFAULT_MINUTE));
		return timepicker;
	}

	@Override
	protected void onDialogClosed(boolean positiveResult) {
		super.onDialogClosed(positiveResult);
		if (positiveResult) {
			timepicker.clearFocus();
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
			editor.putInt(getKey()+".hour", timepicker.getCurrentHour());
			editor.putInt(getKey()+".minute", timepicker.getCurrentMinute());
			Utils.apply(editor);
		}
	}

}
