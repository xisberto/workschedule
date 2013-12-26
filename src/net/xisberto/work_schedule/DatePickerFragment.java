package net.xisberto.work_schedule;

import java.util.Calendar;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

public class DatePickerFragment extends DialogFragment implements
		OnDateSetListener {
	public static interface OnDateSelectedListener {
		public void onDateSelected(int year, int monthOfYear, int dayOfMonth);
	}

	DatePickerFragment.OnDateSelectedListener callback;

	public static DatePickerFragment newInstance(DatePickerFragment.OnDateSelectedListener callback) {
		DatePickerFragment dialog = new DatePickerFragment();
		dialog.callback = callback;
		return dialog;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Calendar now = Calendar.getInstance();
		int year = now.get(Calendar.YEAR);
		int month = now.get(Calendar.MONTH);
		int day = now.get(Calendar.DAY_OF_MONTH);

		return new DatePickerDialog(getActivity(), this, year, month, day);
	}

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		if (callback != null) {
			callback.onDateSelected(year, monthOfYear, dayOfMonth);
		}
	}
}