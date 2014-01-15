package net.xisberto.work_schedule.alarm;

import net.xisberto.work_schedule.MainActivity;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.database.Period;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.format.Time;

public class CountdownService extends Service {
	public static final String ACTION_START = "net.xisberto.work_schedule.start_countdown",
			ACTION_STOP = "net.xisberto.work_schedule.stop_countdown";

	private CountDownTimer timer;
	private NotificationCompat.Builder builder;
	private NotificationManager manager;
	private Period period;

	public CountdownService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent.hasExtra(AlarmMessageActivity.EXTRA_PERIOD_ID)) {
			int period_id = intent.getIntExtra(
					AlarmMessageActivity.EXTRA_PERIOD_ID,
					R.string.fstp_entrance);
			period = Period.getPeriod(this, period_id);
		} else {
			stopSelf();
		}

		if (ACTION_START.equals(intent.getAction())) {

			long millisInFuture = period.time.getTimeInMillis()
					- System.currentTimeMillis();
			if (millisInFuture > 0) {

				Intent mainIntent = new Intent(this, MainActivity.class);
				mainIntent.setAction(MainActivity.ACTION_SET_PERIOD);
				mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

				Intent deleteIntent = new Intent(this, CountdownService.class);
				deleteIntent.setAction(ACTION_STOP);

				builder = new Builder(this)
						.setSmallIcon(R.drawable.ic_stat_notification)
						.setContentTitle(getString(period.getLabelId()))
						.setTicker(getString(period.getLabelId()))
						.setOnlyAlertOnce(true)
						.setPriority(NotificationCompat.PRIORITY_LOW)
						.setAutoCancel(true)
						.setSound(
								RingtoneManager
										.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
						.setContentIntent(
								PendingIntent.getActivity(this, period.getId(),
										mainIntent,
										PendingIntent.FLAG_CANCEL_CURRENT))
						.setDeleteIntent(
								PendingIntent.getService(this, period.getId(),
										deleteIntent,
										PendingIntent.FLAG_CANCEL_CURRENT));
				manager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
				manager.notify(0, builder.build());

				timer = new CountDownTimer(millisInFuture, 1000) {

					@Override
					public void onTick(long millisUntilFinished) {
						Time t = new Time();
						t.set(millisUntilFinished);
						builder.setContentText(getString(
								R.string.time_until_alarm, t.format("%M:%S")));
						manager.notify(0, builder.build());
					}

					@Override
					public void onFinish() {
						manager.cancel(0);
						stopSelf();
					}
				};

				timer.start();
			}
		} else if (ACTION_STOP.equals(intent.getAction())) {
			if (timer != null)
				timer.cancel();
			((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
					.cancel(0);
			stopSelf();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
