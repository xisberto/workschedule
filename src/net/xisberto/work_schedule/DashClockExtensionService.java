package net.xisberto.work_schedule;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
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
		Bundle next_alarm = new Settings(getApplicationContext()).getNextAlarm();
		Log.d(getPackageName(), "Updating Dash Clock Extension");
		
		if (next_alarm.getString(Settings.EXTRA_PERIOD_TIME).equals("")) {
			publishUpdate(null);
		}
		
		publishUpdate(new ExtensionData()
				.visible(true)
				.icon(R.drawable.ic_dashclock)
				.status(next_alarm.getString(Settings.EXTRA_PERIOD_TIME))
				.expandedTitle(next_alarm.getString(Settings.EXTRA_PERIOD_LABEL))
				.expandedBody(next_alarm.getString(Settings.EXTRA_PERIOD_TIME))
				.clickIntent(
						new Intent(this, MainActivity.class)));
	}
	
	public void updateAlarm() {
		onUpdateData(UPDATE_REASON_CONTENT_CHANGED);
	}
}
