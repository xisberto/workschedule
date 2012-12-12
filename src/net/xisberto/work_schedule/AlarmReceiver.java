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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;

public class AlarmReceiver extends BroadcastReceiver {
	public AlarmReceiver() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		int pref_id = extras.getInt(AlarmMessageActivity.EXTRA_PERIOD_ID);
		Period p = Period.getFromPrefId(pref_id);
		showNotification(context, p, extras.getString(AlarmMessageActivity.EXTRA_TIME));
		new Settings(context).unsetAlarm(p);
	}
	
	protected void showNotification(Context context, Period period, String time) {
		Intent intentAlarm = new Intent(context, AlarmMessageActivity.class);
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_TIME, time);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID, period.pref_id);
		
		PendingIntent notifySender = PendingIntent.getActivity(context, period.pref_id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent intent_cancel = new Intent(context, AlarmMessageActivity.class);
		intent_cancel.setAction(AlarmMessageActivity.ACTION_CANCEL);
		PendingIntent cancel_alarm = PendingIntent.getActivity(context, 0, intent_cancel, PendingIntent.FLAG_CANCEL_CURRENT);
		Intent intent_snooze = new Intent(context, AlarmMessageActivity.class);
		intent_snooze.setAction(AlarmMessageActivity.ACTION_SNOOZE);
		PendingIntent snooze_alarm = PendingIntent.getActivity(context, 1, intent_snooze, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_stat_notification)
				.setContentTitle(context.getString(period.label_id))
				.setTicker(context.getString(period.label_id))
				//.addAction(R.drawable.ic_choose_time, context.getString(R.string.dismiss), cancel_alarm)
				//.addAction(R.drawable.ic_choose_time, context.getString(R.string.snooze), snooze_alarm)
				.setWhen(Calendar.getInstance().getTimeInMillis())
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setContentIntent(notifySender)
				.build();
		
		nm.notify(period.pref_id, notification);
		context.startActivity(intentAlarm);
	}
}
