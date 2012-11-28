package net.xisberto.workschedule;

import java.util.Calendar;

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
		showNotification(context, extras.getInt(AlarmMessageActivity.EXTRA_PERIOD_LABEL_ID), extras.getString(AlarmMessageActivity.EXTRA_TIME));
	}
	
	protected void showNotification(Context context, int period_label_id, String time) {
		Intent intentAlarm = new Intent(context, AlarmMessageActivity.class);
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_TIME, time);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_LABEL_ID, period_label_id);
		
		PendingIntent notifySender = PendingIntent.getActivity(context, period_label_id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);
		
		Intent intent_cancel = new Intent(context, AlarmMessageActivity.class);
		intent_cancel.setAction(AlarmMessageActivity.ACTION_CANCEL);
		PendingIntent cancel_alarm = PendingIntent.getActivity(context, 0, intent_cancel, PendingIntent.FLAG_CANCEL_CURRENT);
		Intent intent_snooze = new Intent(context, AlarmMessageActivity.class);
		intent_snooze.setAction(AlarmMessageActivity.ACTION_SNOOZE);
		PendingIntent snooze_alarm = PendingIntent.getActivity(context, 1, intent_snooze, PendingIntent.FLAG_CANCEL_CURRENT);
		
		NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_stat_notification)
				.setContentTitle(context.getString(period_label_id))
				.setTicker(context.getString(period_label_id))
				//.addAction(R.drawable.ic_choose_time, context.getString(R.string.dismiss), cancel_alarm)
				//.addAction(R.drawable.ic_choose_time, context.getString(R.string.snooze), snooze_alarm)
				.setWhen(Calendar.getInstance().getTimeInMillis())
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setContentIntent(notifySender)
				.build();
		
		nm.notify(period_label_id, notification);
		context.startActivity(intentAlarm);
	}
}
