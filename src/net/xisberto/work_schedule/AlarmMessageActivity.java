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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;

public class AlarmMessageActivity extends SherlockFragmentActivity implements
		DialogInterface.OnClickListener {
	public static final String EXTRA_TIME = "time",
			EXTRA_PERIOD_ID = "period_id",
			ACTION_SHOW_ALARM = "net.xisberto.workschedule.showalarm",
			ACTION_CANCEL = "net.xisberto.workschedule.cancel",
			ACTION_SNOOZE = "net.xisberto.workschedule.snooze";
	private MediaPlayer mMediaPlayer;
	private String time;
	private int period_pref_id;

	public static class AlarmDialog extends SherlockDialogFragment {

		public static AlarmDialog newInstance(int period_id, String time) {
			AlarmDialog alarmDialog = new AlarmDialog();
			Bundle args = new Bundle();
			args.putInt(AlarmMessageActivity.EXTRA_PERIOD_ID, period_id);
			args.putString(AlarmMessageActivity.EXTRA_TIME, time);
			alarmDialog.setArguments(args);
			return alarmDialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			LayoutInflater inflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.activity_alarm_message, null);
			((TextView) view.findViewById(R.id.alarm_time))
					.setText(getArguments().getString(
							AlarmMessageActivity.EXTRA_TIME));

			int period_id = getArguments().getInt(
					AlarmMessageActivity.EXTRA_PERIOD_ID);
			Period period = Period.getFromPrefId(period_id);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
					.setView(view)
					.setTitle(period.label_id)
					.setPositiveButton(R.string.dismiss,
							(OnClickListener) getActivity())
					.setNegativeButton(R.string.snooze,
							(OnClickListener) getActivity());

			return builder.create();
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			super.onDismiss(dialog);
			((AlarmMessageActivity)getActivity()).cancelAlarm();
		}

	}

	// Get an alarm sound. Try for an alarm. If none set, try notification,
	// Otherwise, ringtone.
	private Uri getAlarmUri() {
		Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
		if (alert == null) {
			alert = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			if (alert == null) {
				alert = RingtoneManager
						.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
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
				mMediaPlayer.prepare();
			}
		} catch (IOException e) {
			System.out.println("OOPS");
		}
	}

	private void dismisDialog() {
		AlarmDialog dialog = (AlarmDialog) getSupportFragmentManager()
				.findFragmentByTag("alarm");
		if (dialog != null) {
			dialog.dismiss();
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
		dismissNotification();
		dismisDialog();
		stopSound();
		finish();
	}

	private void snoozeAlarm() {
		Settings settings = new Settings(getApplicationContext());
		Calendar alarm_time = settings.getCalendar(period_pref_id);
		alarm_time.add(Calendar.MINUTE, 10);
		Period period = Period.getFromPrefId(period_pref_id);
		settings.setAlarm(period, alarm_time, true);
		Toast.makeText(
				this,
				getResources().getString(R.string.snooze_set_to)
						+ settings.formatCalendar(alarm_time),
				Toast.LENGTH_SHORT).show();
		cancelAlarm();
	}

	private void showNotification() {
		Period period = Period.getFromPrefId(period_pref_id);
		
		Intent intentAlarm = new Intent(this, AlarmMessageActivity.class);
		intentAlarm.setAction(AlarmMessageActivity.ACTION_SHOW_ALARM);
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_TIME, time);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID,
				period.pref_id);

		PendingIntent notifySender = PendingIntent.getActivity(this,
				period.pref_id, intentAlarm, PendingIntent.FLAG_UPDATE_CURRENT);

		Intent intent_cancel = new Intent(this, AlarmMessageActivity.class);
		intent_cancel.setAction(AlarmMessageActivity.ACTION_CANCEL);
		PendingIntent cancel_alarm = PendingIntent.getActivity(this, 0,
				intent_cancel, Intent.FLAG_ACTIVITY_NEW_TASK);
		Intent intent_snooze = new Intent(this, AlarmMessageActivity.class);
		intent_snooze.setAction(AlarmMessageActivity.ACTION_SNOOZE);
		PendingIntent snooze_alarm = PendingIntent.getActivity(this, 1,
				intent_snooze, Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationManager nm = (NotificationManager) this
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new NotificationCompat.Builder(this)
				.setSmallIcon(R.drawable.ic_stat_notification)
				.setContentTitle(this.getString(period.label_id))
				.setTicker(this.getString(period.label_id))
				.setWhen(Calendar.getInstance().getTimeInMillis())
				.setOngoing(true)
				.setOnlyAlertOnce(true)
				.setContentIntent(notifySender)
				.addAction(R.drawable.ic_snooze,
						this.getString(R.string.snooze), snooze_alarm)
				.addAction(R.drawable.ic_dismiss,
						this.getString(R.string.dismiss), cancel_alarm)
				.build();

		nm.notify(period.pref_id, notification);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		time = getIntent().getStringExtra(EXTRA_TIME);
		period_pref_id = getIntent().getIntExtra(EXTRA_PERIOD_ID,
				R.string.sndp_entrance);

		// Dismiss any previous dialogs
		dismisDialog();

		AlarmDialog dialog = AlarmDialog.newInstance(period_pref_id, time);
		dialog.show(getSupportFragmentManager(), "alarm");

		prepareSound(getApplicationContext(), getAlarmUri());

		Log.i("alarm", "onCreate");
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i("alarm", "onStart");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i("alarm", "onResume");
		dismissNotification();
		mMediaPlayer.start();
		Log.i(getApplication().getPackageName(), "action: " + getIntent().getAction());
	}

	@Override
	protected void onStop() {
		super.onStop();
		getIntent().setAction("none");
		if (!isFinishing()) {
			showNotification();
		} else {
			cancelAlarm();
		}
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		switch (which) {
		case AlertDialog.BUTTON_NEGATIVE:
			snoozeAlarm();
		case AlertDialog.BUTTON_POSITIVE:
			cancelAlarm();
		}
	}

}
