/*******************************************************************************
 * Copyright (c) 2012 Humberto Fraga.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Humberto Fraga - initial API and implementation
 ******************************************************************************/
package net.xisberto.work_schedule;

import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class TimePickerFragment extends SherlockDialogFragment
		implements OnClickListener {

	private TimePicker timePicker;

	public static TimePickerFragment newInstance(int callerId) {
		TimePickerFragment dialog_fragment = new TimePickerFragment();
		Bundle args = new Bundle();
		args.putInt("callerId", callerId);
		dialog_fragment.setArguments(args);
		return dialog_fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		if (!(getActivity() instanceof OnTimePickerSetListener)) {
			throw new ClassCastException("Activity must implement OnTimePickerSetListener");
		}
		
		final Calendar c = Calendar.getInstance();
		int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
		int minute = c.get(Calendar.MINUTE);

		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.time_picker_dialog, null);
		timePicker = (TimePicker) view.findViewById(R.id.timePicker);
		timePicker
				.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
		timePicker.setCurrentHour(hourOfDay);
		timePicker.setCurrentMinute(minute);

		// Create a new instance of TimePickerDialog and return it
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setView(view)
				.setPositiveButton(android.R.string.ok, this)
				.setNegativeButton(android.R.string.cancel, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		timePicker.clearFocus();
		switch (which) {
		case AlertDialog.BUTTON_POSITIVE:
			int callerId = getArguments().getInt("callerId");
			((MainActivity) getActivity()).onTimeSet(
					timePicker.getCurrentHour(),
					timePicker.getCurrentMinute(), callerId);
			break;
		case AlertDialog.BUTTON_NEGATIVE:
		default:
			break;
		}
	}

	public interface OnTimePickerSetListener {
		public void onTimeSet(int hour, int minute, int callerId);
	}
	
}
