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

import net.xisberto.work_schedule.TimePickerFragment.OnTimePickerSetListener;
import net.xisberto.work_schedule.database.Period;
import net.xisberto.work_schedule.settings.Settings;
import net.xisberto.work_schedule.settings.SettingsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements
		OnItemClickListener, OnTimePickerSetListener {

	public static final String ACTION_SET_PERIOD = "net.xisberto.work_schedule.set_period";

	// private static final SparseArray<Period> PeriodIds = new
	// SparseArray<Period>();

	private SparseArrayCompat<Period> periods;
	private Settings settings;

	private boolean showDialogOnResume;

	private PeriodListAdapter adapter;

	@Override
	public void onTimeSet(int hour, int minute, int callerId) {
		// First we set the alarm passed by the caller, using a Period from our
		// WorkDay
		Period period = periods.get(callerId);
		period.setTime(hour, minute);
		period.enabled = true;
		settings.setAlarm(period);

		// We will use next_period to set all Periods remaining in he WorkDay
		Period next_period = null;
		// We will use this Calendar to calculate the Periods' times
		Calendar cal = (Calendar) period.time.clone();

		// This switch is used as a loop. We will enter it in the period.getId
		// point and go through every step (without breaks). At every step, we:
		// 1. point next_period to the correct Period in our WorkDay,
		// 2. calculate the correct hour and minute for the alarm, and
		// 3. set the alarm.
		switch (period.getId()) {
		case R.string.fstp_entrance:
			next_period = periods.get(R.string.fstp_exit);

			// In this point, cal = period.time
			// fstp_exit = fstp_entrance + key_fstp_duration
			settings.addCalendars(cal,
					settings.getCalendar(R.string.key_fstp_duration));
			next_period.time = (Calendar) cal.clone();

			settings.setAlarm(next_period);

		case R.string.fstp_exit:
			next_period = periods.get(R.string.sndp_entrance);

			// From this point and in the remaining, cal is a clone of
			// period.time or a result of previous calculations
			// sndp_entrance = fstp_exit + key_lunch_interval
			settings.addCalendars(cal,
					settings.getCalendar(R.string.key_lunch_interval));
			next_period.time = (Calendar) cal.clone();

			settings.setAlarm(next_period);

		case R.string.sndp_entrance:
			next_period = periods.get(R.string.sndp_exit);

			// Here we need more calculations. The duration of the second period
			// is based on the total work time (key_work_time) minus the today's
			// first period (fstp_exit - fstp_entrance). It's best calculated
			// using milliseconds math
			Calendar work_time = settings
					.getCalendar(getString(R.string.key_work_time));
			Calendar fstp_entrance = periods.get(R.string.fstp_entrance).time;
			Calendar fstp_exit = periods.get(R.string.fstp_exit).time;

			long mili_sndp_duration = work_time.getTimeInMillis()
					- (fstp_exit.getTimeInMillis() - fstp_entrance
							.getTimeInMillis());

			// We set this Calendar to the duration calculated above. It will be
			// added to cal
			Calendar sndp_duration = Calendar.getInstance();
			sndp_duration.setTimeInMillis(mili_sndp_duration);

			// sndp_exit = sndp_entrance + sndp_duration
			settings.addCalendars(cal, sndp_duration);
			next_period.time = (Calendar) cal.clone();
			
			settings.setAlarm(next_period);
			
		case R.string.sndp_exit:
			next_period = periods.get(R.string.fste_entrance);
			
			// fste_entrance = sndp_exis + key_extra_interval
			settings.addCalendars(cal, settings
					.getCalendar(getString(R.string.key_extra_interval)));
			next_period.time = (Calendar) cal.clone();
			
			settings.setAlarm(next_period);
			
		case R.string.fste_entrance:
			next_period = periods.get(R.string.fste_exit);
			
			// fste_exit = fste_entrance + key_fste_duration
			settings.addCalendars(cal,
					settings.getCalendar(getString(R.string.key_fste_duration)));
			next_period.time = (Calendar) cal.clone();
			
			settings.setAlarm(next_period);
		default:
			break;
		}
		
		updateLayout();
	}

	private void updateLayout() {
		ListView list = (ListView) findViewById(R.id.list);
		adapter = new PeriodListAdapter(this, periods);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	private void showTimePickerDialog(Period period) {
		TimePickerFragment dialog = TimePickerFragment.newInstance(period);
		dialog.show(getSupportFragmentManager(), "time_picker");
	}
	
	private void insertPeriod(Period p) {
		if (periods == null) {
			periods = new SparseArrayCompat<Period>(8);
		}
		periods.put(p.getId(), p);
	}
	
	private void getPeriods() {
		insertPeriod(new Period(R.string.fstp_entrance, Calendar.getInstance()));
		insertPeriod(new Period(R.string.fstp_exit, Calendar.getInstance()));
		insertPeriod(new Period(R.string.sndp_entrance, Calendar.getInstance()));
		insertPeriod(new Period(R.string.sndp_exit, Calendar.getInstance()));
		insertPeriod(new Period(R.string.fste_entrance, Calendar.getInstance()));
		insertPeriod(new Period(R.string.fste_exit, Calendar.getInstance()));
		insertPeriod(new Period(R.string.snde_entrance, Calendar.getInstance()));
		insertPeriod(new Period(R.string.snde_exit, Calendar.getInstance()));
	}
	
	public Period getNextAlarm() {
		Calendar cal = Calendar.getInstance();
		for (int i = 0; i < periods.size(); i++) {
			Period period = periods.valueAt(i);
			if (period.enabled && period.time.after(cal)) {
				return period;
			}
		}
		return null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		settings = Settings.getInstance(getApplicationContext());
		settings.setDefaultPreferenceValues();

		getPeriods();

		if (getIntent().getAction() != null
				&& getIntent().getAction().equals(ACTION_SET_PERIOD)) {
			showDialogOnResume = true;
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLayout();
		if (showDialogOnResume) {
			showDialogOnResume = false;
			Period next = getNextAlarm();
			if (next != null) {
				showTimePickerDialog(next);
			}
		}
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
		showTimePickerDialog(periods.valueAt(position));
	}

}
