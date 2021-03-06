/*******************************************************************************
 * Copyright 2014 xisberto
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.xisberto.work_schedule.database;

import java.util.Calendar;

import net.xisberto.work_schedule.BuildConfig;
import net.xisberto.work_schedule.DashClockExtensionService;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.alarm.AlarmMessageActivity;
import net.xisberto.work_schedule.alarm.AlarmReceiver;
import net.xisberto.work_schedule.alarm.CountdownService;
import net.xisberto.work_schedule.settings.Settings;
import net.xisberto.work_schedule.widget.WidgetNextProvider;
import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.Process;
import android.text.format.DateFormat;

public class Period {
	protected long id;
	private int pref_id;
	public Calendar time;
	public boolean enabled;

	public static final int[] ids = new int[] { R.string.fstp_entrance,
			R.string.fstp_exit, R.string.sndp_entrance, R.string.sndp_exit,
			R.string.fste_entrance, R.string.fste_exit, R.string.snde_entrance,
			R.string.snde_exit };

	public Period(int pref_id, Calendar time) {
		this.id = -1;
		this.pref_id = pref_id;
		if (time == null) {
			this.time = Calendar.getInstance();
		} else {
			this.time = time;
		}
		this.time.set(Calendar.SECOND, 0);
		this.time.set(Calendar.MILLISECOND, 0);
		this.enabled = false;
	}

	public static Period getPeriod(Context context, int pref_id) {
		Period p = Database.getInstance(context).getPeriodOfDay(pref_id,
				Calendar.getInstance());
		if (p == null) {
			p = new Period(pref_id, Calendar.getInstance());
			p.persist(context);
		}
		return p;
	}

	public int getId() {
		return pref_id;
	}

	public int getLabelId() {
		int labelId;
		switch (pref_id) {
		case R.string.fstp_entrance:
			labelId = R.string.lbl_fstp_entrance;
			break;
		case R.string.fstp_exit:
			labelId = R.string.lbl_fstp_exit;
			break;
		case R.string.sndp_entrance:
			labelId = R.string.lbl_sndp_entrance;
			break;
		case R.string.sndp_exit:
			labelId = R.string.lbl_sndp_exit;
			break;
		case R.string.fste_entrance:
			labelId = R.string.lbl_fste_entrance;
			break;
		case R.string.fste_exit:
			labelId = R.string.lbl_fste_exit;
			break;
		case R.string.snde_entrance:
			labelId = R.string.lbl_snde_entrance;
			break;
		case R.string.snde_exit:
			labelId = R.string.lbl_snde_exit;
			break;
		default:
			labelId = R.string.no_alarm;
			break;
		}
		return labelId;
	}

	public void setTime(int hour, int minute) {
		time.set(Calendar.HOUR_OF_DAY, hour);
		time.set(Calendar.MINUTE, minute);
	}

	/**
	 * Clones {@link reference} into this object's {@link time}
	 * 
	 * @param reference
	 */
	public void setTime(Calendar reference) {
		time = (Calendar) reference.clone();
		// time.set(Calendar.HOUR_OF_DAY, reference.get(Calendar.HOUR_OF_DAY));
		// time.set(Calendar.MINUTE, reference.get(Calendar.MINUTE));
	}

	public void addTime(Calendar reference) {
		time.add(Calendar.HOUR_OF_DAY, reference.get(Calendar.HOUR_OF_DAY));
		time.add(Calendar.MINUTE, reference.get(Calendar.MINUTE));
	}

	/**
	 * Formats {@link time} in a simple time String using {@link DateFormat}.
	 * 
	 * @param is24HourFormat
	 *            if the result should be formated as "kk:mm"
	 * @return a string formated on 24h or 12h according to
	 *         {@code is24HourFormat}
	 */
	public String formatTime(boolean is24HourFormat) {
		String inFormat = "hh:mm aa";
		if (is24HourFormat) {
			inFormat = "kk:mm";
		}
		if (BuildConfig.DEBUG) {
			inFormat = "yyyy-MM-dd " + inFormat;
		}
		return DateFormat.format(inFormat, time).toString();
	}

	/**
	 * Set a new alarm or cancel a existing one, based on this object's
	 * {@link enabled}. Also configure the countdown notification (
	 * {@link CountdownService}) if enable on Preferences. The alarm won't be
	 * set if this object's {@link time} is before now. If {@link updateWidgets}
	 * is true} , sends Broadcasts to {@link WidgetNextProvider} and to
	 * {@link DashClockExtensionService} making those widgets update their
	 * informations.
	 * 
	 * @param context
	 *            the {@link Context} to handle the need Intents
	 * @param updateWidgets
	 *            if the widgets should be updated to reflect the alarms
	 */
	@SuppressLint("NewApi")
	public void setAlarm(Context context, boolean updateWidgets) {
		AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		// Prepare the actual alarm
		Intent intentAlarm = new Intent(context.getApplicationContext(),
				AlarmReceiver.class);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID, pref_id);

		PendingIntent alarmSender = PendingIntent.getBroadcast(
				context.getApplicationContext(), pref_id, intentAlarm,
				PendingIntent.FLAG_CANCEL_CURRENT);
		// This Period is set enabled, set the alarm
		// If not, cancel it
		if (enabled) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
				am.setExact(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), alarmSender);
			} else {
				am.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis(), alarmSender);
			}
		} else {
			am.cancel(alarmSender);
		}

		// This is the countdown notification, that goes off
		// 5 minutes before the alarm, if enabled by the user
		if (Settings.getInstance(context).getNotifyCountdown()) {
			Intent countdown = new Intent(context.getApplicationContext(),
					CountdownService.class);
			countdown.setAction(CountdownService.ACTION_START);
			countdown.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID, pref_id);
			PendingIntent countSender = PendingIntent.getService(
					context.getApplicationContext(), pref_id, countdown,
					PendingIntent.FLAG_CANCEL_CURRENT);
			// If the user disables this Period, so we cancel the
			// countdown notification
			if (enabled) {
				am.set(AlarmManager.RTC_WAKEUP, time.getTimeInMillis() - 5 * 60 * 1000,
						countSender);
			} else {
				am.cancel(countSender);
				// If the service is couting for this Period, stop it
				Intent stopSpecific = new Intent(context, CountdownService.class)
						.setAction(CountdownService.ACTION_STOP_SPECIFIC)
						.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID, pref_id);
				context.startService(stopSpecific);
			}
		}

		if (updateWidgets) {
			Intent updateIntent = new Intent(context.getApplicationContext(),
					WidgetNextProvider.class);
			updateIntent.setAction(WidgetNextProvider.MY_ACTION_UPDATE);
			context.sendBroadcast(updateIntent);

			context.sendBroadcast(new Intent(
					DashClockExtensionService.ACTION_UPDATE_ALARM));
		}
	}

	/**
	 * Calls {@code setAlarm(context, false)}
	 * 
	 * @param context
	 */
	public void setAlarm(Context context) {
		setAlarm(context, false);
	}

	public void persist(Context context, PersistCallback callback) {
		new PersistThread(context, this, PersistThread.ACTION_INSERT_OR_UPDATE, callback)
				.start();
	}

	public void persist(Context context) {
		persist(context, null);
	}

	private class PersistThread extends Thread {
		public static final int ACTION_INSERT_OR_UPDATE = 1;
		private int action;
		private Context context;
		private Period period;
		private PersistCallback callback;

		public PersistThread(Context context, Period period, int action,
				PersistCallback callback) {
			this.context = context;
			this.period = period;
			this.action = action;
			this.callback = callback;
			setPriority(Process.THREAD_PRIORITY_BACKGROUND);
		}

		@Override
		public void run() {
			Database database = Database.getInstance(context);
			switch (action) {
			case ACTION_INSERT_OR_UPDATE:
				if (period.id < 0) {
					period.id = database.insertPeriod(period);
				} else {
					database.updatePeriod(period);
				}
				break;

			default:
				break;
			}

			if (callback != null) {
				Handler mainHandler = new Handler(context.getMainLooper());
				mainHandler.post(callback);
			}
		}

	}

	public interface PersistCallback extends Runnable {
	}

}
