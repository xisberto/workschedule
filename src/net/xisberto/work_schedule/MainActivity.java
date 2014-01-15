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
package net.xisberto.work_schedule;

import java.util.Calendar;

import net.xisberto.work_schedule.alarm.CountdownService;
import net.xisberto.work_schedule.database.Database;
import net.xisberto.work_schedule.database.Period;
import net.xisberto.work_schedule.history.ViewHistoryActivity;
import net.xisberto.work_schedule.settings.Settings;
import net.xisberto.work_schedule.settings.SettingsActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.doomonafireball.betterpickers.radialtimepicker.RadialPickerLayout;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog;
import com.doomonafireball.betterpickers.radialtimepicker.RadialTimePickerDialog.OnTimeSetListener;

public class MainActivity extends SherlockFragmentActivity implements
		OnItemClickListener, OnTimeSetListener {

	public static final String ACTION_SET_PERIOD = "net.xisberto.work_schedule.set_period";

	private SparseArrayCompat<Period> periods;
	private Settings settings;

	private boolean showDialogOnResume;

	private PeriodListAdapter adapter;

	private int waiting_for = -1;

	private RadialTimePickerDialog timePickerDialog;

	@Override
	public void onTimeSet(RadialPickerLayout view, int hourOfDay, int minute) {
		Calendar now = Calendar.getInstance();

		// First we set the alarm passed by the caller, using a Period from our
		// SparseArray
		Period period = periods.get(waiting_for);
		period.setTime(hourOfDay, minute);
		period.enabled = period.time.after(now);
		period.persist(this);
		// From every setAlarm we will call during this operation, only this
		// need to update the widgets, because this will become the next alarm
		period.setAlarm(this, true);

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
			next_period.addTime(settings.getCalendar(R.string.key_fstp_duration));

			next_period.enabled = next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;

		case R.string.fstp_exit:
			next_period = periods.get(R.string.sndp_entrance);

			// sndp_entrance = fstp_exit + key_lunch_interval
			next_period.setTime(period.time);
			next_period.addTime(settings.getCalendar(R.string.key_lunch_interval));

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
			Calendar work_time = settings.getCalendar(getString(R.string.key_work_time));
			Calendar fstp_entrance = periods.get(R.string.fstp_entrance).time;
			Calendar fstp_exit = periods.get(R.string.fstp_exit).time;

			long mili_sndp_duration = work_time.getTimeInMillis()
					- (fstp_exit.getTimeInMillis() - fstp_entrance.getTimeInMillis());

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
			next_period.addTime(settings.getCalendar(R.string.key_extra_interval));

			next_period.enabled = settings.getMarkExtra() && next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;

		case R.string.fste_entrance:
			next_period = periods.get(R.string.fste_exit);

			// fste_exit = fste_entrance + key_fste_duration
			next_period.setTime(period.time);
			next_period.addTime(settings.getCalendar(R.string.key_fste_duration));

			next_period.enabled = settings.getMarkExtra() && next_period.time.after(now);
			next_period.persist(this);
			next_period.setAlarm(this);
			period = next_period;
		default:
			break;
		}

		waiting_for = -1;
		updateLayout();
	}

	private void updateLayout() {
		ListView list = (ListView) findViewById(R.id.list);
		adapter = new PeriodListAdapter(this, periods);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	private void showTimePickerDialog(Period period) {
		waiting_for = period.getId();
		Calendar time = Calendar.getInstance();
		timePickerDialog = RadialTimePickerDialog.newInstance(this,
				time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),
				DateFormat.is24HourFormat(this));
		// Originally, RadialTimePickerDialog has no title and use no arguments
		// When using the original from Better Pickers, there's no difference
		// But I'm using a lightly different version that adds a title to the
		// dialog and uses the "title" key from arguments to set the dialog's
		// title
		Bundle args = new Bundle();
		args.putString("title", getString(period.getLabelId()));
		timePickerDialog.setArguments(args);
		timePickerDialog.show(getSupportFragmentManager(), "time_picker");
	}

	/**
	 * Generates the {@link SparseArray} with the {@link Period}s for today.
	 * Each {@link Period} not saved on the database will be set to this moment.
	 * 
	 */
	private void buildPeriods() {
		periods = new SparseArrayCompat<Period>(Period.ids.length);
		for (int id : Period.ids) {
			periods.put(id, Period.getPeriod(this, id));
		}
	}

	private boolean shouldSetPeriod(Intent intent) {
		return (intent.getAction() != null && intent.getAction()
				.equals(ACTION_SET_PERIOD))
				|| (intent.getScheme() != null && intent.getScheme().equals(
						"work_schedule"));
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		settings = Settings.getInstance(getApplicationContext());
		settings.setDefaultPreferenceValues();

		buildPeriods();

		if (shouldSetPeriod(getIntent())) {
			if (savedInstanceState != null) {
				showDialogOnResume = savedInstanceState.getBoolean("showDialogOnResume",
						true);
			} else {
				showDialogOnResume = true;
			}
			startService(new Intent(this, CountdownService.class)
					.setAction(CountdownService.ACTION_STOP));
		}

	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		showDialogOnResume = shouldSetPeriod(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		updateLayout();
		// If we should show the dialog
		if (showDialogOnResume) {
			// We get the period to set
			Period next = Database.getInstance(this).getNextAlarm();
			if (next == null) {
				next = periods.get(R.string.fstp_entrance);
			}
			// If the dialog is already present
			if (timePickerDialog != null && timePickerDialog.isVisible()) {
				// We compare the period to set with the current waiting_for
				if (next.getId() != waiting_for) {
					// If it's different, we must change it
					timePickerDialog.dismiss();
					showTimePickerDialog(next);
				}
			} else {
				showTimePickerDialog(next);
			}
		}
		// And we don't show the dialog again until onNewIntent says so
		showDialogOnResume = false;
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putBoolean("showDialogOnResume", showDialogOnResume);
		super.onSaveInstanceState(outState);
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
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_history:
			startActivity(new Intent(this, ViewHistoryActivity.class));
			return true;
		default:
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		showTimePickerDialog(periods.valueAt(position));
	}

}
