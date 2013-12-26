package net.xisberto.work_schedule;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class DatePickerFragment extends SherlockDialogFragment implements
		OnDateSetListener {
	public static interface OnDateSelectedListener {
		public void onDateSelected(int year, int monthOfYear, int dayOfMonth);
	}

	OnDateSelectedListener callback;

	public static DatePickerFragment newInstance(OnDateSelectedListener callback) {
		DatePickerFragment dialog = new DatePickerFragment();
		dialog.callback = callback;
		return dialog;
	}
	
	public static DatePickerFragment newInstance(OnDateSelectedListener callback, Calendar cal) {
		DatePickerFragment dialog = newInstance(callback);
		Bundle args = new Bundle();
		args.putSerializable("calendar", cal);
		dialog.setArguments(args);
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar now;
		if (getArguments().containsKey("calendar")) {
			now = (Calendar) getArguments().getSerializable("calendar");
		} else {
			now = Calendar.getInstance();
		}
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);

		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Log.d("DatePicker", "onDateSet");
		if (callback != null) {
			callback.onDateSelected(year, monthOfYear, dayOfMonth);
		}
	}
}