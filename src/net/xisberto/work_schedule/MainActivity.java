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
import android.util.Log;
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

	private static final SparseArray<Period> PeriodIds = new SparseArray<Period>();

	private Settings settings;

	private void addCalendars(Calendar cal1, Calendar cal2) {
		cal1.add(Calendar.HOUR_OF_DAY, cal2.get(Calendar.HOUR_OF_DAY));
		cal1.add(Calendar.MINUTE, cal2.get(Calendar.MINUTE));
	}

	@Override
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
