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
import android.util.SparseArray;
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

	private SparseArrayCompat<Period> periods;
	private Settings settings;

	private boolean showDialogOnResume;

	private PeriodListAdapter adapter;

	@Override
	public void onTimeSet(int hour, int minute, int callerId) {
		Calendar now = Calendar.getInstance();

		// First we set the alarm passed by the caller, using a Period from our
		// SparseArray
		Period period = periods.get(callerId);
		period.setTime(hour, minute);
		period.enabled = period.time.after(now);
		period.persist(this);
		period.setAlarm(this);

		// We will use next_period to set all Periods remaining
		Period next_period = null;
		
		// This switch is used as a loop. We will enter it in the period.getId
		// point and go through every step after (without breaks). At every
		// step, we:
		// 1. point next_period to the correct Period in our SparseArray,
		// 2. calculate the correct hour and minute for next_period, and
		// 3. persist the period and set the alarm.
		switch (period.getId()) {
		case R.string.fstp_entrance:
			next_period = periods.get(R.string.fstp_exit);

			// fstp_exit = fstp_entrance + key_fstp_duration
			next_period.setTime(period.time);
			next_period.addTime(settings
					.getCalendar(R.string.key_fstp_duration));

			next_period.enabled = next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;

		case R.string.fstp_exit:
			next_period = periods.get(R.string.sndp_entrance);
			
			// sndp_entrance = fstp_exit + key_lunch_interval
			next_period.setTime(period.time);
			next_period.addTime(settings
					.getCalendar(R.string.key_lunch_interval));

			next_period.enabled = next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;

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
			// added to next_period
			Calendar sndp_duration = Calendar.getInstance();
			sndp_duration.setTimeInMillis(mili_sndp_duration);

			// sndp_exit = sndp_entrance + sndp_duration
			next_period.setTime(period.time);
			next_period.addTime(sndp_duration);

			next_period.enabled = next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;

		case R.string.sndp_exit:
			next_period = periods.get(R.string.fste_entrance);
			
			// fste_entrance = sndp_exis + key_extra_interval
			next_period.setTime(period.time);
			next_period.addTime(settings
					.getCalendar(R.string.key_extra_interval));

			next_period.enabled = next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;

		case R.string.fste_entrance:
			next_period = periods.get(R.string.fste_exit);
			
			// fste_exit = fste_entrance + key_fste_duration
			next_period.setTime(period.time);
			next_period.addTime(settings
					.getCalendar(R.string.key_fste_duration));

			next_period.enabled = next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;
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

	/**
	 * Generates the {@link SparseArray} with the {@link Period}s for today.
	 * Each {@link Period} will be set to this moment.
	 * 
	 */
	private void buildPeriods() {
		periods = new SparseArrayCompat<Period>(Period.ids.length);
		for (int pref_id : Period.ids) {
			periods.put(pref_id, Period.getPeriod(this, pref_id));
		}
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

		buildPeriods();

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
