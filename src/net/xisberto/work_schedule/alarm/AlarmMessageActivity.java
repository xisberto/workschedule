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
package net.xisberto.work_schedule.alarm;

import java.io.IOException;
import java.util.Calendar;

import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.settings.Settings;
import net.xisberto.work_schedule.settings.Settings.Period;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.MotionEventCompat;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
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
	private Settings settings;
	private float initialPoint;
	private float currentPoint;
	private boolean moving;
	private HinterThread hinter;

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
	
	private void startHinter() {
        View hinter_top = findViewById(R.id.hinter_top);
        View hinter_bottom = findViewById(R.id.hinter_bottom);

        hinter = new HinterThread(hinter_top, hinter_bottom);
        hinter.start();
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

		initialPoint = 0f;
        currentPoint = 0f;
        moving = false;

        findViewById(R.id.frame_top).setOnTouchListener(this);
        findViewById(R.id.frame_bottom).setOnTouchListener(this);

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
        startHinter();
	}

	@Override
	protected void onPause() {
		super.onPause();
        hinter.interrupt();
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

	@Override
	public boolean onTouch(View view, MotionEvent event) {
		if (event.getPointerCount() > 1) {
            return super.onTouchEvent(event);
        }

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();

        int action = MotionEventCompat.getActionMasked(event);
        if (view.getId() == R.id.frame_bottom || view.getId() == R.id.frame_top) {
            switch (action) {
                case MotionEvent.ACTION_DOWN:
                    initialPoint = event.getRawY();
                    moving = true;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (moving) {
                        hinter.interrupt();
                        currentPoint = event.getRawY();
                        
                        DisplayMetrics displayMetrics = new DisplayMetrics();
                        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                        int screenHeight = displayMetrics.heightPixels;

                        if (view.getId() == R.id.frame_top) {
                            int new_margin = (int) (currentPoint - initialPoint);
                            params.topMargin = (new_margin > 0) ? new_margin : 0;
                            if ( (new_margin > (screenHeight / 3)) 
                            		&& (!isFinishing()) ) {
                            	snoozeAlarm();
                            	break;
                            }
                        } else {
                            int new_margin = (int) (initialPoint - currentPoint);
                            params.bottomMargin = (new_margin > 0) ? new_margin : 0;
                            if ( (new_margin > (screenHeight / 3)) 
                            		&& (!isFinishing()) ) {
                            	cancelAlarm();
                            	break;
                            }
                        }
                        view.setLayoutParams(params);
                        view.invalidate();
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    initialPoint = 0;
                    TranslateAnimation ta;
                    if (view.getId() == R.id.frame_top) {
                        ta = new TranslateAnimation(0, 0, params.topMargin, 0);
                        params.topMargin = 0;
                    } else {
                        ta = new TranslateAnimation(0, 0, -params.bottomMargin, 0);
                        params.bottomMargin = 0;
                    }
                    ta.setDuration(100);
                    view.setLayoutParams(params);
                    view.startAnimation(ta);
                    moving = false;
                    startHinter();
                    break;
                default:
                    return super.onTouchEvent(event);
            }
        }
        return true;
	}

}
