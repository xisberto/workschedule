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
package net.xisberto.workschedule;

import java.io.IOException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
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
			EXTRA_PERIOD_LABEL_ID = "period_label_id",
			ACTION_CANCEL = "net.xisberto.workschedule.cancel", ACTION_SNOOZE = "net.xisberto.workschedule.snooze";
	private MediaPlayer mMediaPlayer;
	private String time;
	private int period_label_id;

	public static class AlarmDialog extends SherlockDialogFragment {

		public static AlarmDialog newInstance(int period_label_id, String time) {
			AlarmDialog alarmDialog = new AlarmDialog();
			Bundle args = new Bundle();
			args.putInt(AlarmMessageActivity.EXTRA_PERIOD_LABEL_ID,
					period_label_id);
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

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
					.setView(view)
					.setTitle(
							getArguments().getInt(
									AlarmMessageActivity.EXTRA_PERIOD_LABEL_ID))
					.setPositiveButton(R.string.dismiss,
							(OnClickListener) getActivity())
					.setNegativeButton(R.string.snooze,
							(OnClickListener) getActivity());

			return builder.create();
		}

		@Override
		public void onStop() {
			super.onStop();
			Log.i("dialog", "onStop");
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			super.onCancel(dialog);
			getActivity().finish();
			Log.i("dialog", "onCancel");
		}

		@Override
		public void onDismiss(DialogInterface dialog) {
			super.onDismiss(dialog);
			Log.i("dialog", "onDismiss");
		}

		@Override
		public void onDetach() {
			super.onDetach();
			Log.i("dialog", "onDetach");
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
		nm.cancel(period_label_id);
	}

	private void cancelAlarm() {
		dismissNotification();
		dismisDialog();
		finish();
	}

	private void snoozeAlarm() {
		dismissNotification();
		Toast.makeText(this, R.string.snooze_not_implemented,
				Toast.LENGTH_SHORT).show();
		finish();
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		time = getIntent().getStringExtra(EXTRA_TIME);
		period_label_id = getIntent().getIntExtra(EXTRA_PERIOD_LABEL_ID,
				R.string.sndp_entrance);

		// Dismiss any previous dialogs and notifications
		dismisDialog();

		AlarmDialog dialog = AlarmDialog.newInstance(period_label_id, time);
		dialog.show(getSupportFragmentManager(), "alarm");

		prepareSound(getApplicationContext(), getAlarmUri());

		Log.i("alarm", "onCreate");
	}

	@Override
	protected void onResume() {
		super.onResume();
		mMediaPlayer.start();
		Log.i("alarm", "onResume");
	}

	@Override
	protected void onStop() {
		super.onStop();
		if (isFinishing()) {
			mMediaPlayer.stop();
			mMediaPlayer.release();
			mMediaPlayer = null;
			dismissNotification();
		}
		Log.i("alarm", "onStop");
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
