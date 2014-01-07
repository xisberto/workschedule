package net.xisberto.work_schedule.alarm;

import net.xisberto.work_schedule.MainActivity;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.database.Period;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationCompat.Builder;
import android.text.format.Time;

public class CountdownService extends Service {
	public static final String ACTION_START = "net.xisberto.work_schedule.start_countdown",
			ACTION_STOP = "net.xisberto.work_schedule.stop_countdown";

	private CountDownTimer timer = new CountDownTimer(5 * 60 * 1000, 1000) {

		@Override
		public void onTick(long millisUntilFinished) {
			Time t = new Time();
			t.set(millisUntilFinished);
			builder.setContentText(t.format("%M:%S"));
			manager.notify(period_id, builder.build());
		}

		@Override
		public void onFinish() {
			manager.cancel(period_id);
			stopSelf();
		}
	};

	private NotificationCompat.Builder builder;
	NotificationManager manager;
	private int period_id;

	public CountdownService() {
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if (intent.hasExtra(AlarmMessageActivity.EXTRA_PERIOD_ID)) {
			period_id = intent.getIntExtra(
					AlarmMessageActivity.EXTRA_PERIOD_ID,
					R.string.fstp_entrance);
		} else {
			stopSelf();
		}
		
		if (ACTION_START.equals(intent.getAction())) {
			Period period = Period.getPeriod(this, period_id);
			
			Intent deleteIntent = new Intent(this, CountdownService.class);
			deleteIntent.setAction(ACTION_STOP);

			Intent mainIntent = new Intent(this, MainActivity.class);
			mainIntent.setAction(MainActivity.ACTION_SET_PERIOD);
			mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

			builder = new Builder(this)
					.setSmallIcon(R.drawable.ic_stat_notification)
					.setContentTitle(this.getString(period.getLabelId()))
					.setTicker(this.getString(period.getLabelId()))
					.setContentIntent(
							PendingIntent.getActivity(this, period_id,
									mainIntent,
									PendingIntent.FLAG_CANCEL_CURRENT))
					.setDeleteIntent(
							PendingIntent.getService(this, period_id,
									deleteIntent,
									PendingIntent.FLAG_CANCEL_CURRENT));
			manager = ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE));
			manager.notify(period_id, builder.build());
			timer.start();
		} else if (ACTION_STOP.equals(intent.getAction())) {
			timer.cancel();
			manager.cancel(period_id);
			stopSelf();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
}
