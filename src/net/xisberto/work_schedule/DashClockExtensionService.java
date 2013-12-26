/*******************************************************************************
 * Copyright (c) 2013 Humberto Fraga <xisberto@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Humberto Fraga <xisberto@gmail.com> - initial API and implementation
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

		publishUpdate(new ExtensionData().visible(true)
				.icon(R.drawable.ic_dashclock).status(formated_time)
				.expandedTitle(getString(period.getLabelId()))
				.expandedBody(formated_time)
				.clickIntent(new Intent(this, MainActivity.class)));
	}

	public void updateAlarm() {
		onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
	}
}
