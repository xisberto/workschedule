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

import net.xisberto.work_schedule.database.Database;
import net.xisberto.work_schedule.database.Period;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

public class DashClockExtensionService extends DashClockExtension {
	public static final String ACTION_UPDATE_ALARM = "net.xisberto.work_schedule.UPDATE_ALARM";

	private BroadcastReceiver receiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			updateAlarm();
		}
	};

	public DashClockExtensionService() {
	}

	@Override
	public void onCreate() {
		super.onCreate();
		IntentFilter filter = new IntentFilter(ACTION_UPDATE_ALARM);
		registerReceiver(receiver, filter);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiver);
	}

	@Override
	protected void onUpdateData(int arg0) {
		Period period = Database.getInstance(getApplicationContext())
				.getNextAlarm();
		Log.d(getPackageName(), "Updating Dash Clock Extension");

		if (period == null) {
			publishUpdate(new ExtensionData().visible(false));
			return;
		}

		String formated_time = period.formatTime(DateFormat
				.is24HourFormat(getApplicationContext()));
		Intent clickIntent = new Intent(this, MainActivity.class)
				.setAction(MainActivity.ACTION_SET_PERIOD);

		publishUpdate(new ExtensionData().visible(true)
				.icon(R.drawable.ic_dashclock).status(formated_time)
				.expandedTitle(getString(period.getLabelId()))
				.expandedBody(formated_time).clickIntent(clickIntent));
	}

	public void updateAlarm() {
		onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
	}
}
