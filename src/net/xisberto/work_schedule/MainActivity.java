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

import net.xisberto.work_schedule.Settings.Period;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements
		OnItemClickListener {

	private static final SparseArray<Period> PeriodIds = new SparseArray<Period>();

	private Settings settings;

	public static class TimePickerFragment extends SherlockDialogFragment
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
					.setPositiveButton(getString(android.R.string.ok), this)
					.setNegativeButton(getString(android.R.string.cancel), this);
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

	}

	private void addCalendars(Calendar cal1, Calendar cal2) {
		cal1.add(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
		cal1.add(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
	}

	public void onTimeSet(int hour, int minute, int callerId) {
		// This object will be incremented ad each step of the switch bellow
		Calendar cal = settings.getCalendarFromTime(hour, minute);

		Period period = PeriodIds.get(callerId);
		Period next_period = Period.SNDE_EXIT;

		settings.setAlarm(period, cal, false);

		switch (period) {
		case FSTP_ENTRANCE:
			next_period = Period.FSTP_EXIT;
			addCalendars(
					cal,
					settings.getCalendar(getString(R.string.key_fstp_duration)));
			settings.setAlarm(next_period, cal, true);
		case FSTP_EXIT:
			next_period = Period.SNDP_ENTRANCE;
			addCalendars(
					cal,
					settings.getCalendar(getString(R.string.key_lunch_interval)));
			settings.setAlarm(next_period, cal, true);
		case SNDP_ENTRANCE:
			next_period = Period.SNDP_EXIT;

			Calendar work_time = settings.getCalendar(getString(R.string.key_work_time));
			Calendar fstp_entrance = settings.getCalendar(getString(R.string.fstp_entrance));
			Calendar fstp_exit = settings.getCalendar(getString(R.string.fstp_exit));

			long mili_sndp_duration =
					work_time.getTimeInMillis()
					- (fstp_exit.getTimeInMillis() - fstp_entrance.getTimeInMillis());
			
			Calendar sndp_duration = Calendar.getInstance();
			sndp_duration.setTimeInMillis(mili_sndp_duration);

			addCalendars(cal, sndp_duration);
			settings.setAlarm(next_period, cal, true);
		case SNDP_EXIT:
			next_period = Period.FSTE_ENTRANCE;
			addCalendars(
					cal,
					settings.getCalendar(getString(R.string.key_extra_interval)));
			settings.setAlarm(next_period, cal, true);
		case FSTE_ENTRANCE:
			next_period = Period.FSTE_EXIT;
			addCalendars(
					cal,
					settings.getCalendar(getString(R.string.key_fste_duration)));
			settings.setAlarm(next_period, cal, false);
			settings.setAlarm(Period.SNDE_ENTRANCE, cal, false);
			settings.setAlarm(Period.SNDE_EXIT, cal, false);
		case FSTE_EXIT:
		case SNDE_ENTRANCE:
		case SNDE_EXIT:
		default:
			break;
		}
		updateLayout();
	}

	private void updateLayout() {
		ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(new PeriodAdapter(this, PeriodIds));
		list.setOnItemClickListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		settings = new Settings(getApplicationContext());
		settings.setDefaultPreferenceValues();

		for (Period period : Period.values()) {
			PeriodIds.put(period.pref_id, period);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLayout();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_settings:
			Intent settings = new Intent(this, SettingsActivity.class);
			startActivity(settings);
		default:
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		Log.i("", "OnItemClick view: "+v.getClass().getCanonicalName());
		TimePickerFragment dialog = TimePickerFragment.newInstance(Period
				.values()[position].pref_id);
		dialog.show(getSupportFragmentManager(), "time_picker");
	}

}
