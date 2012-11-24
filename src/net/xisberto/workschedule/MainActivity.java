package net.xisberto.workschedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TimePicker;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockFragmentActivity implements
		OnItemClickListener {
	public static final String PREF_TOTAL_HOURS = "total_hours",
			PREF_INTERVAL = "interval";

	public enum Period {
		FSTP_ENTRANCE(R.string.fstp_entrance, R.string.lbl_fstp_entrance), FSTP_EXIT(
				R.string.fstp_exit, R.string.lbl_fstp_exit), SNDP_ENTRANCE(
				R.string.sndp_entrance, R.string.lbl_sndp_entrance), SNDP_EXIT(
				R.string.sndp_exit, R.string.lbl_sndp_exit), FSTE_ENTRANCE(
				R.string.fste_entrance, R.string.lbl_fste_entrance), FSTE_EXIT(
				R.string.fste_exit, R.string.lbl_fste_exit), SNDE_ENTRANCE(
				R.string.snde_entrance, R.string.lbl_snde_entrance), SNDE_EXIT(
				R.string.snde_exit, R.string.lbl_snde_exit);

		private int pref_id;
		private int label_id;

		private Period(int pref_id, int label_id) {
			this.pref_id = pref_id;
			this.label_id = label_id;
		}
	}

	private static final SparseArray<Period> PeriodIds = new SparseArray<MainActivity.Period>();

	public static class TimePickerFragment extends SherlockDialogFragment
			implements OnClickListener {

		private TimePicker timePicker;

		public static TimePickerFragment newInstance(int callerId) {
			TimePickerFragment dialog_fragment = new TimePickerFragment();
			Bundle args = new Bundle();
			args.putInt("callerId", callerId);
			dialog_fragment.setArguments(args);
			return dialog_fragment;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Calendar c = Calendar.getInstance();
			int hourOfDay = c.get(Calendar.HOUR_OF_DAY);
			int minute = c.get(Calendar.MINUTE);

			LayoutInflater inflater = (LayoutInflater) getActivity()
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			View view = inflater.inflate(R.layout.time_picker_dialog, null);
			timePicker = (TimePicker) view.findViewById(R.id.timePicker);
			timePicker
					.setIs24HourView(DateFormat.is24HourFormat(getActivity()));
			timePicker.setCurrentHour(hourOfDay);
			timePicker.setCurrentMinute(minute);

			// Create a new instance of TimePickerDialog and return it
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
					.setView(view)
					.setPositiveButton(getString(android.R.string.ok), this)
					.setNegativeButton(getString(android.R.string.cancel), this);
			return builder.create();
		}

		@Override
		public void onClick(DialogInterface dialog, int which) {
			switch (which) {
			case AlertDialog.BUTTON_POSITIVE:
				int callerId = getArguments().getInt("callerId");
				((MainActivity) getActivity()).onTimeSet(
						timePicker.getCurrentHour(),
						timePicker.getCurrentMinute(), callerId);
				break;
			case AlertDialog.BUTTON_NEGATIVE:
			default:
				break;
			}
		}

	}

	public void onTimeSet(int hour, int minute, int callerId) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, hour);
		cal.set(Calendar.MINUTE, minute);
		cal.set(Calendar.SECOND, 0);

		Period period = PeriodIds.get(callerId);
		Period next = Period.SNDE_EXIT;
		Editor editor = PreferenceManager.getDefaultSharedPreferences(
				getApplicationContext()).edit();

		editor.putString(getString(period.pref_id),
				DateFormat.format("kk:mm", cal).toString());

		switch (period) {
		case FSTP_ENTRANCE:
			next = Period.FSTP_EXIT;
			cal.add(Calendar.HOUR_OF_DAY, 4);
			editor.putString(getString(next.pref_id),
					DateFormat.format("kk:mm", cal).toString());
			setAlarm(this, next.label_id, cal);
		case FSTP_EXIT:
			next = Period.SNDP_ENTRANCE;
			cal.add(Calendar.HOUR_OF_DAY, 1);
			editor.putString(getString(next.pref_id),
					DateFormat.format("kk:mm", cal).toString());
			setAlarm(this, next.label_id, cal);
		case SNDP_ENTRANCE:
			next = Period.SNDP_EXIT;
			cal.add(Calendar.HOUR_OF_DAY, 4);
			editor.putString(getString(next.pref_id),
					DateFormat.format("kk:mm", cal).toString());
			setAlarm(this, next.label_id, cal);
		case SNDP_EXIT:
			next = Period.FSTE_ENTRANCE;
			cal.add(Calendar.MINUTE, 15);
			editor.putString(getString(next.pref_id),
					DateFormat.format("kk:mm", cal).toString());
			setAlarm(this, next.label_id, cal);
		case FSTE_ENTRANCE:
			next = Period.FSTE_EXIT;
			cal.add(Calendar.HOUR_OF_DAY, 2);
			editor.putString(getString(next.pref_id),
					DateFormat.format("kk:mm", cal).toString());
			setAlarm(this, next.label_id, cal);
		case FSTE_EXIT:
		case SNDE_ENTRANCE:
		case SNDE_EXIT:
		default:
			break;
		}
		apply(editor);
		updateLayout();
	}

	/**
	 * Set a new alarm
	 * 
	 * @param context
	 *            the {@link Context} in which the alarm will start
	 * @param period_label_id
	 *            the {@link Period} related to the alarm
	 * @param cal
	 *            the time when the alarm will start
	 */
	protected void setAlarm(Context context, int period_label_id, Calendar cal) {
		// Prepare intent to AlarmManager
		String time = DateFormat.format("kk:mm", cal).toString();
		Intent intentAlarm = new Intent(context, AlarmMessageActivity.class);
		intentAlarm.putExtra("period_label_id", period_label_id);
		intentAlarm.putExtra("time", time);
		// Those flags prevent the user to come back to the alarm dialog
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
				| Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent alarmSender = PendingIntent
				.getActivity(context, period_label_id, intentAlarm,
						PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager am = (AlarmManager) context
				.getSystemService(Context.ALARM_SERVICE);
		am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), alarmSender);
		// Log.i("Alarm was set to", );
	}

	protected void setNotification(Context context, String time, int alarmId,
			Calendar cal) {
		// Prepare intent to Notification as a copy of intentAlarm and set
		// another action
		Intent intentNofity = new Intent(context, AlarmMessageActivity.class);
		intentNofity.putExtra("time", time);
		intentNofity.putExtra("alarmId", alarmId);
		PendingIntent notifySender = PendingIntent.getActivity(context,
				alarmId, intentNofity, PendingIntent.FLAG_UPDATE_CURRENT);
		NotificationManager nm = (NotificationManager) context
				.getSystemService(Context.NOTIFICATION_SERVICE);
		Notification notification = new NotificationCompat.Builder(context)
				.setSmallIcon(R.drawable.ic_choose_time).setOngoing(true)
				.setContentTitle(context.getString(R.string.alarm_set))
				.setContentText(time)
				.setTicker(context.getString(R.string.alarm_set) + " " + time)
				.setContentIntent(notifySender).getNotification();
		nm.notify(alarmId, notification);
	}

	@SuppressLint("NewApi")
	private static void apply(Editor editor) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			editor.commit();
		} else {
			editor.apply();
		}
	}

	private List<Map<String, Object>> getPeriodsTimes() {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getApplicationContext());
		for (Period period : Period.values()) {
			Map<String, Object> item = new HashMap<String, Object>();
			item.put("id", period.pref_id);
			item.put("name", getResources().getString(period.label_id));
			item.put("time", prefs.getString(
					getResources().getString(period.pref_id), "00:00"));
			result.add(item);
		}
		return result;
	}

	private void updateLayout() {
		ListView list = (ListView) findViewById(R.id.list);
		String[] from = new String[] { "name", "time" };
		int[] to = new int[] { R.id.period_label, R.id.period_time };
		SimpleAdapter adapter = new SimpleAdapter(this, getPeriodsTimes(),
				R.layout.period_list_item, from, to);
		list.setAdapter(adapter);
		list.setOnItemClickListener(this);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		for (Period period : Period.values()) {
			PeriodIds.put(period.pref_id, period);
		}

		updateLayout();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		default:
			return false;
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		TimePickerFragment dialog = TimePickerFragment.newInstance(Period
				.values()[position].pref_id);
		dialog.show(getSupportFragmentManager(), "time_picker");
	}

}
