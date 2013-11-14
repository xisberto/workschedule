package net.xisberto.work_schedule.database;

import java.util.Calendar;

import net.xisberto.work_schedule.R;
import android.content.Context;
import android.os.Handler;
import android.os.Process;

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
		this.enabled = false;
	}
	
	public static Period getPeriod(int pref_id) {
		//TODO check on database if we have such pref_id + calendar combination
		return new Period(pref_id, Calendar.getInstance());
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
			labelId = R.string.lbl_fste_exit;
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

	/**Sets the the hour and minute fields of this object's time to the hour
	 * and minute fields of the parameter
	 * @param time
	 */
	public void setTime(Calendar time) {
		time.set(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
		time.set(Calendar.MINUTE, time.get(Calendar.MINUTE));
	}

	public void addTime(Calendar time) {
		time.add(Calendar.HOUR_OF_DAY, time.get(Calendar.HOUR_OF_DAY));
		time.add(Calendar.MINUTE, time.get(Calendar.MINUTE));
	}

	public void persist(Context context, PersistCallback callback) {
		new PersistThread(context, this, PersistThread.ACTION_INSERT_OR_UPDATE,
				callback).start();
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
