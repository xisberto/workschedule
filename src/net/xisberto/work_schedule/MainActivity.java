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
import net.xisberto.work_schedule.TimePickerFragment.OnTimePickerSetListener;
import android.content.Intent;
import android.os.Bundle;
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
	
	public static final String 
			ACTION_SET_PERIOD = "net.xisberto.work_schedule.set_period",
			EXTRA_PREF_ID = "pref_id";

	private static final SparseArray<Period> PeriodIds = new SparseArray<Period>();

	private Settings settings;

	@Override
	public void onTimeSet(int hour, int minute, int callerId) {
		// This object will be incremented ad each step of the switch bellow
		Calendar cal = settings.getCalendarFromTime(hour, minute);

		Period period = PeriodIds.get(callerId);
		Period next_period = Period.SNDE_EXIT;

		settings.setAlarm(period, cal, true);

		switch (period) {
		case FSTP_ENTRANCE:
			next_period = Period.FSTP_EXIT;
			settings.addCalendars(
					cal,
					settings.getCalendar(getString(R.string.key_fstp_duration)));
			settings.setAlarm(next_period, cal, true);
		case FSTP_EXIT:
			next_period = Period.SNDP_ENTRANCE;
			settings.addCalendars(
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

			settings.addCalendars(cal, sndp_duration);
			settings.setAlarm(next_period, cal, true);
		case SNDP_EXIT:
			next_period = Period.FSTE_ENTRANCE;
			settings.addCalendars(
					cal,
					settings.getCalendar(getString(R.string.key_extra_interval)));
			settings.setAlarm(next_period, cal, settings.getMarkExtra());
		case FSTE_ENTRANCE:
			next_period = Period.FSTE_EXIT;
			settings.addCalendars(
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
		if (settings.canAskForRating()) {
			RatingDialog dialog = new RatingDialog();
			dialog.show(getSupportFragmentManager(), "rating");
		}
	}

	private void updateLayout() {
		ListView list = (ListView) findViewById(R.id.list);
		list.setAdapter(new PeriodListAdapter(this, PeriodIds));
		list.setOnItemClickListener(this);
	}

	private void showTimePickerDialog(int pref_id) {
		TimePickerFragment dialog = TimePickerFragment.newInstance(pref_id);
		dialog.show(getSupportFragmentManager(), "time_picker");
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
		if (getIntent().getAction() != null && getIntent().getAction().equals(ACTION_SET_PERIOD)) {
			int pref_id = getIntent().getIntExtra(EXTRA_PREF_ID, 0);
			if (PeriodIds.get(pref_id) != null) {
				showTimePickerDialog(pref_id);
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
		showTimePickerDialog(Period.values()[position].pref_id);
	}

}
