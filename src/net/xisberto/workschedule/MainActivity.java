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
package net.xisberto.workschedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements
		OnItemClickListener {

	public enum Period {
		FSTP_ENTRANCE(R.string.fstp_entrance, R.string.lbl_fstp_entrance), FSTP_EXIT(
				R.string.fstp_exit, R.string.lbl_fstp_exit), SNDP_ENTRANCE(
				R.string.sndp_entrance, R.string.lbl_sndp_entrance), SNDP_EXIT(
				R.string.sndp_exit, R.string.lbl_sndp_exit), FSTE_ENTRANCE(
				R.string.fste_entrance, R.string.lbl_fste_entrance), FSTE_EXIT(
				R.string.fste_exit, R.string.lbl_fste_exit), SNDE_ENTRANCE(
				R.string.snde_entrance, R.string.lbl_snde_entrance), SNDE_EXIT(
				R.string.snde_exit, R.string.lbl_snde_exit);

		private int pref_id;
		private int label_id;

		private Period(int pref_id, int label_id) {
			this.pref_id = pref_id;
			this.label_id = label_id;
		}
	}

	private static final SparseArray<Period> PeriodIds = new SparseArray<MainActivity.Period>();

	private SharedPreferences prefs;

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

	private Calendar getCalendarFromTime(int hour, int minute) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}

	private Calendar getCalendarFromPreference(String key) {
		int hour = prefs.getInt(key + TimePickerPreference.SUFIX_HOUR,
				TimePickerPreference.DEFAULT_HOUR);
		int minute = prefs.getInt(key + TimePickerPreference.SUFIX_MINUTE,
				TimePickerPreference.DEFAULT_MINUTE);
		return getCalendarFromTime(hour, minute);
	}

	private void saveCalendarToPreference(Calendar cal, String key) {
		Editor editor = prefs
				.edit()
				.putInt(key + TimePickerPreference.SUFIX_HOUR,
						cal.get(Calendar.HOUR_OF_DAY))
				.putInt(key + TimePickerPreference.SUFIX_MINUTE,
						cal.get(Calendar.MINUTE));
		Utils.apply(editor);
	}
	
	private void addCalendars(Calendar cal1, Calendar cal2) {
		cal1.add(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
		cal1.add(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
	}

	public void onTimeSet(int hour, int minute, int callerId) {
		// This object will be incremented ad each step of the switch bellow
		Calendar cal = getCalendarFromTime(hour, minute);

		Period period = PeriodIds.get(callerId);
		Period next_period = Period.SNDE_EXIT;

		Editor editor = prefs.edit();
		saveCalendarToPreference(cal, getString(period.pref_id));
		editor.putBoolean(getString(period.pref_id) + ".isset", false);
		Utils.apply(editor);

		switch (period) {
		case FSTP_ENTRANCE:
			next_period = Period.FSTP_EXIT;
			addCalendars(
					cal,
					getCalendarFromPreference(getString(R.string.key_fstp_duration)));
			saveAlarm(next_period, cal);
		case FSTP_EXIT:
			next_period = Period.SNDP_ENTRANCE;
			addCalendars(
					cal,
					getCalendarFromPreference(getString(R.string.key_lunch_interval)));
			saveAlarm(next_period, cal);
		case SNDP_ENTRANCE:
			next_period = Period.SNDP_EXIT;

			Calendar work_time = getCalendarFromPreference(getString(R.string.key_work_time));
			Calendar fstp_entrance = getCalendarFromPreference(getString(R.string.fstp_entrance));
			Calendar fstp_exit = getCalendarFromPreference(getString(R.string.fstp_exit));

			long mili_sndp_duration =
					work_time.getTimeInMillis()
					- (fstp_exit.getTimeInMillis() - fstp_entrance.getTimeInMillis());
			
			Calendar sndp_duration = Calendar.getInstance();
			sndp_duration.setTimeInMillis(mili_sndp_duration);

			addCalendars(cal, sndp_duration);
			saveAlarm(next_period, cal);
		case SNDP_EXIT:
			next_period = Period.FSTE_ENTRANCE;
			addCalendars(
					cal,
					getCalendarFromPreference(getString(R.string.key_extra_interval)));
			saveAlarm(next_period, cal);
		case FSTE_ENTRANCE:
			next_period = Period.FSTE_EXIT;
			addCalendars(
					cal,
					getCalendarFromPreference(getString(R.string.key_fste_duration)));
			saveAlarm(next_period, cal);
		case FSTE_EXIT:
		case SNDE_ENTRANCE:
		case SNDE_EXIT:
		default:
			break;
		}
		updateLayout();
	}

	private void saveAlarm(Period period, Calendar cal) {
		Editor editor = prefs.edit();
		saveCalendarToPreference(cal, getString(period.pref_id));
		editor.putBoolean(getString(period.pref_id) + ".isset", true);
		Utils.apply(editor);
		setAlarm(this, period.label_id, cal);
	}

	/**
	 * Set a new alarm
	 * 
	 * @param context
	 *            the {@link Context} in which the alarm will start
	 * @param period_label_id
	 *            the {@link Period} related to the alarm
	 * @param cal
	 *            the time when the alarm will start
	 */
	protected void setAlarm(Context context, int period_label_id, Calendar cal) {
		String time = DateFormat.format("kk:mm", cal).toString();
		Intent intentAlarm = new Intent(context, AlarmReceiver.class);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_LABEL_ID,
				period_label_id);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_TIME, time);
		PendingIntent alarmSender = PendingIntent
				.getBroadcast(context, period_label_id, intentAlarm,
						PendingIntent.FLAG_CANCEL_CURRENT);

		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmSender);
	}

	private List<Map<String, Object>> getPeriodsInfo() {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (Period period : Period.values()) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("id", period.pref_id);
			item.put("name", getResources().getString(period.label_id));
			
			Calendar cal = getCalendarFromPreference(getString(period.pref_id));
			String time = DateFormat.format("kk:mm", cal).toString();
			item.put("time", time);
			
			item.put("isset", prefs.getBoolean(getString(period.pref_id)
					+ ".isset", false));
			result.add(item);
		}
		return result;
	}

	private void updateLayout() {
		ListView list = (ListView) findViewById(R.id.list);
		String[] from = new String[] { "name", "time", "isset" };
		int[] to = new int[] { R.id.period_label, R.id.period_time,
				R.id.check_alarm };
		SimpleAdapter adapter = new SimpleAdapter(this, getPeriodsInfo(),
				R.layout.period_list_item, from, to);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		Utils.setDefaultPreferenceValues(getApplicationContext(), prefs);

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
		TimePickerFragment dialog = TimePickerFragment.newInstance(Period
				.values()[position].pref_id);
		dialog.show(getSupportFragmentManager(), "time_picker");
	}

}
