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

import java.io.IOException;
import java.util.Calendar;

import net.xisberto.work_schedule.Settings.Period;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AlarmMessageActivity extends SherlockFragmentActivity implements
		OnTouchListener {
	public static final String EXTRA_TIME = "time",
			EXTRA_PERIOD_ID = "period_id",
			ACTION_SHOW_ALARM = "net.xisberto.workschedule.showalarm";
	private MediaPlayer mMediaPlayer;
	private int period_pref_id;
	private TextView text_snooze;
	private TextView text_dismiss;
	private int deltaY;
	private Settings settings;

	// Get an alarm sound. Try for saved user option. If none set, try default
	// alarm, notification, or ringtone.
	private Uri getAlarmUri() {
		String ringtone = settings.getRingtone();
		Uri alert = null;
		if (ringtone != null) {
			alert = Uri.parse(ringtone);
		} else {
			alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
				if (alert == null) {
					alert = RingtoneManager
							.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
				}
			}
		}
		return alert;
	}

	private void prepareSound(Context context, Uri alert) {
		mMediaPlayer = new MediaPlayer();
		mMediaPlayer.setWakeMode(context, PowerManager.PARTIAL_WAKE_LOCK);
		try {
			mMediaPlayer.setDataSource(context, alert);
			final AudioManager audioManager = (AudioManager) context
					.getSystemService(Context.AUDIO_SERVICE);
			if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
				mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mMediaPlayer.setLooping(true);
				mMediaPlayer.prepare();
			}
		} catch (IOException e) {
			System.out.println("OOPS");
		}
	}

	private void dismissNotification() {
		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		nm.cancel(period_pref_id);
	}

	private void stopSound() {
		if (mMediaPlayer != null) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
		}
	}

	private void cancelAlarm() {
		stopSound();
		((Vibrator) getSystemService(VIBRATOR_SERVICE)).cancel();
		if (!isFinishing()) {
			finish();
		}
	}

	private void snoozeAlarm() {
		settings = new Settings(getApplicationContext());

		Calendar alarm_time = settings.getCalendar(period_pref_id);
		Calendar snooze_increment = settings
				.getCalendar(R.string.key_snooze_increment);
		settings.addCalendars(alarm_time, snooze_increment);

		Period period = Period.getFromPrefId(period_pref_id);
		settings.setAlarm(period, alarm_time, true);
		Toast.makeText(
				this,
				getResources().getString(R.string.snooze_set_to) + " "
						+ settings.formatCalendar(alarm_time),
				Toast.LENGTH_SHORT).show();
		cancelAlarm();
	}

	private void showNotification() {
		Period period = Period.getFromPrefId(period_pref_id);

		Intent intentAlarm = new Intent(this, AlarmMessageActivity.class);
		intentAlarm.setAction(AlarmMessageActivity.ACTION_SHOW_ALARM);
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
				| Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

		PendingIntent notifySender = PendingIntent.getActivity(this,
				period.pref_id, intentAlarm, PendingIntent.FLAG_CANCEL_CURRENT);

		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);

		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_stat_notification)
				.setContentTitle(this.getString(period.label_id))
				.setTicker(this.getString(period.label_id))
				.setWhen(settings.getCalendar(period_pref_id).getTimeInMillis())
				.setOngoing(true).setOnlyAlertOnce(true)
				.setContentIntent(notifySender).build();

		nm.notify(period.pref_id, notification);
	}

	private boolean isLargeScreen() {
		DisplayMetrics metrics = new DisplayMetrics();
		getWindowManager().getDefaultDisplay().getMetrics(metrics);
		Log.d(getPackageName(), "X size: " + metrics.widthPixels
				/ metrics.density);
		Log.d(getPackageName(), "Y size: " + metrics.heightPixels
				/ metrics.density);
		return (metrics.widthPixels / metrics.density > 600f);
	}

	private void setOrientation() {
		if (isLargeScreen()) {
			setRequestedOrientation(Configuration.ORIENTATION_LANDSCAPE);
		} else {
			setRequestedOrientation(Configuration.ORIENTATION_PORTRAIT);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_close_exit);
		
		getWindow().addFlags(
				WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
						| WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
						| WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
		
		setContentView(R.layout.activity_alarm_message);

		period_pref_id = getIntent().getIntExtra(EXTRA_PERIOD_ID,
				R.string.fstp_entrance);
		settings = new Settings(getApplicationContext());
		String time = settings.formatCalendar(settings
				.getCalendar(period_pref_id));

		((TextView) findViewById(R.id.txt_alarm_label)).setText(Period
				.getFromPrefId(period_pref_id).label_id);
		((TextView) findViewById(R.id.txt_alarm_time)).setText(time);

		text_snooze = (TextView) findViewById(R.id.txt_snooze);
		text_dismiss = (TextView) findViewById(R.id.txt_dismiss);
		text_snooze.setOnTouchListener(this);
		text_dismiss.setOnTouchListener(this);

		setOrientation();

		prepareSound(getApplicationContext(), getAlarmUri());

		if (settings.getVibrate()) {
			((Vibrator) getSystemService(VIBRATOR_SERVICE)).vibrate(new long[] {
					500, 500 }, 0);
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
		dismissNotification();
		if (!mMediaPlayer.isPlaying()) {
			mMediaPlayer.start();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (isFinishing()) {
			overridePendingTransition(R.anim.activity_open_enter, R.anim.activity_close_exit);
		}
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isFinishing()) {
			cancelAlarm();
		} else {
			showNotification();
		}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		setOrientation();
	}

	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		switch (event.getKeyCode()) {
		case KeyEvent.KEYCODE_VOLUME_DOWN:
		case KeyEvent.KEYCODE_VOLUME_MUTE:
			mMediaPlayer.setVolume(0f, 0f);
			((Vibrator)getSystemService(VIBRATOR_SERVICE)).cancel();
			return true;

		default:
			return super.dispatchKeyEvent(event);
		}
	}

	@SuppressLint("NewApi")
	@SuppressWarnings("deprecation")
	@Override
	public boolean onTouch(View view, MotionEvent event) {
		boolean is_snooze;

		if ((view.getId() != R.id.txt_snooze)
				&& (view.getId() != R.id.txt_dismiss)) {
			return false;
		}

		is_snooze = view.getId() == R.id.txt_snooze;

		View other_view;
		if (is_snooze) {
			other_view = text_dismiss;
		} else {
			other_view = text_snooze;
		}
		RelativeLayout.LayoutParams params = (LayoutParams) view
				.getLayoutParams();
		RelativeLayout.LayoutParams other_params = (LayoutParams) other_view
				.getLayoutParams();

		final int rawY = (int) event.getRawY();

		int action = MotionEventCompat.getActionMasked(event);
		switch (action) {
		case (MotionEvent.ACTION_DOWN):
			deltaY = rawY;
			return true;
		case (MotionEvent.ACTION_MOVE):
			int screenHeight = 0;
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
				Point size = new Point();
				getWindowManager().getDefaultDisplay().getSize(size);
				screenHeight = size.y;
			} else {
				Display d = getWindowManager().getDefaultDisplay();
				screenHeight = d.getHeight();
			}

			if (is_snooze) {
				if (rawY - deltaY < 0) {
					params.topMargin = 0;
					other_params.bottomMargin = 0;
					return false;
				}
				params.topMargin = rawY - deltaY;
				other_params.bottomMargin = deltaY - rawY;

				if (params.topMargin > (screenHeight / 3)) {
					if (!isFinishing()) {
						snoozeAlarm();
					}
				}
			} else {
				if (deltaY - rawY < 0) {
					params.bottomMargin = 0;
					other_params.topMargin = 0;
					return false;
				}
				params.bottomMargin = deltaY - rawY;
				other_params.topMargin = rawY - deltaY;

				if (params.bottomMargin > (screenHeight / 3)) {
					if (!isFinishing()) {
						cancelAlarm();
					}
				}
			}

			view.setLayoutParams(params);
			findViewById(R.id.alarm_layout_root).invalidate();
			return true;
		case (MotionEvent.ACTION_UP):
		case (MotionEvent.ACTION_CANCEL):
			if (is_snooze) {
				params.topMargin = 0;
				other_params.bottomMargin = 0;
			} else {
				params.bottomMargin = 0;
				other_params.topMargin = 0;
			}
			view.setLayoutParams(params);
			return true;
		case (MotionEvent.ACTION_OUTSIDE):
			return true;
		default:
			return super.onTouchEvent(event);
		}
	}

}
