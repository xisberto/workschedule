package net.xisberto.work_schedule.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.xisberto.work_schedule.BuildConfig;
import net.xisberto.work_schedule.R;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;

public class Database extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "work_schedule.db";
	private static final int DATABASE_VERSION = 1;

	public static final String DATE_FORMAT = "yyyy-MM-dd",
			TIME_FORMAT = "HH:mm", DATETIME_FORMAT = "yyyy-MM-dd HH:mmZZZZZ";

	private static Database instance;

	private SQLiteDatabase db;
	private Context ctx;
	private SimpleDateFormat dateTimeFormat;

	private Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		ctx = context;
		db = getWritableDatabase();
		dateTimeFormat = new SimpleDateFormat(DATETIME_FORMAT,
				Locale.getDefault());
	}

	public static synchronized Database getInstance(Context context) {
		if (instance == null) {
			instance = new Database(context.getApplicationContext());
		}
		return instance;
	}

	private static void log(String message) {
		if (BuildConfig.DEBUG) {
			Log.d("Database", message);
		}
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		// db.execSQL(TableWorkDays.CREATE_TABLE);
		db.execSQL(TablePeriod.CREATE_TABLE);
		// db.execSQL(TablePeriod.CREATE_VIEW);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	@Override
	public synchronized void close() {
		super.close();
		log("Database closed");
	}

	private Calendar parseCalendar(String string) {
		Calendar cal = Calendar.getInstance();
		try {
			cal.setTime(dateTimeFormat.parse(string));
		} catch (ParseException e) {
			log(e.getLocalizedMessage());
			cal.set(Calendar.HOUR_OF_DAY, 0);
			cal.set(Calendar.MINUTE, 0);
			cal.set(Calendar.SECOND, 0);
			cal.set(Calendar.MILLISECOND, 0);
		}
		return cal;
	}

	// private WorkDay workdayFromCursor(Cursor c) {
	// WorkDay day = new WorkDay();
	// try {
	// // try to get the id from the column used on TablePeriod.CREATE_VIEW
	// day.id = c.getLong(c.getColumnIndexOrThrow(TablePeriod.COLUMN_DAY_ID));
	// } catch (IllegalArgumentException e) {
	// // if above column doesn't exists, the cursor refers to TableWorkDays
	// day.id = c.getLong(c.getColumnIndex(TableWorkDays.COLUMN_ID));
	// }
	// day.day =
	// parseCalendar(c.getString(c.getColumnIndex(TableWorkDays.COLUMN_DAY)));
	// return day;
	// }

	private Period periodFromCursor(Cursor c) {
		Period period = new Period(c.getInt(1), parseCalendar(c.getString(2)));
		period.id = c.getLong(0);
		period.enabled = (c.getInt(3) == 1);
		return period;
	}

	// public long insertDay(WorkDay day) {
	// ContentValues cv = new ContentValues();
	// cv.put(TableWorkDays.COLUMN_DAY, DateFormat
	// .format(DATE_FORMAT, day.day).toString());
	// return db.insert(TableWorkDays.TABLE_NAME, null, cv);
	// }

	// public WorkDay getDay(Calendar calendar) {
	// WorkDay result = null;
	//
	// Cursor cursor = db.query(TablePeriod.VIEW_NAME,
	// null, TableWorkDays.COLUMN_DAY + " = ?",
	// new String[] { DateFormat.format(DATE_FORMAT, calendar)
	// .toString() }, null, null, null);
	//
	// if (cursor == null || cursor.getCount() < 0) {
	// return null;
	// }
	//
	// cursor.moveToFirst();
	// result = workdayFromCursor(cursor);
	// do {
	// Period period = result.getPeriod(cursor.getInt(
	// cursor.getColumnIndex(TablePeriod.COLUMN_PREF_ID)));
	// period.enabled = (cursor.getInt(
	// cursor.getColumnIndex(TablePeriod.COLUMN_ENABLED)) == 1);
	// period.time = parseCalendar(cursor.getString(
	// cursor.getColumnIndex(TablePeriod.COLUMN_HOUR)));
	// } while (cursor.moveToNext());
	//
	// return result;
	// }

	private ContentValues contentValuesFromPeriod(Period period) {
		ContentValues cv = new ContentValues();
		cv.put(TablePeriod.COLUMN_PREF_ID, period.getId());
		log("saving time " + period.formatTime(true));
		String formatted = dateTimeFormat.format(period.time.getTime());
		// String formatted = DateFormat.format(DATETIME_WRITE_FORMAT,
		// period.time)
		// .toString();
		log("formatted time " + formatted);
		cv.put(TablePeriod.COLUMN_TIME, formatted);
		cv.put(TablePeriod.COLUMN_ENABLED, period.enabled ? 1 : 0);
		return cv;
	}

	public long insertPeriod(Period period) {
		return db.insert(TablePeriod.TABLE_NAME, null,
				contentValuesFromPeriod(period));
	}

	public void updatePeriod(Period period) {
		db.update(TablePeriod.TABLE_NAME, contentValuesFromPeriod(period),
				TablePeriod.COLUMN_ID + " = ?",
				new String[] { Long.toString(period.id) });
	}

	public Period getPeriodOfDay(int pref_id, Calendar day) {
		SimpleDateFormat dayFormat = new SimpleDateFormat(DATE_FORMAT,
				Locale.getDefault());
		Cursor cursor = db.query(
				TablePeriod.TABLE_NAME,
				TablePeriod.COLUMNS,
				TablePeriod.COLUMN_PREF_ID + " = ? AND "
						+ TablePeriod.COLUMN_TIME + " LIKE ?",
				new String[] { Integer.toString(pref_id),
						dayFormat.format(day.getTime()) + "%" }, null, null,
				null);

		if (cursor == null || cursor.getCount() <= 0) {
			return null;
		}

		cursor.moveToFirst();
		return periodFromCursor(cursor);
	}

	public Period getNextAlarm() {
		Cursor cursor = db.query(TablePeriod.TABLE_NAME, TablePeriod.COLUMNS,
				TablePeriod.COLUMN_ENABLED + " = 1 AND "
						+ TablePeriod.COLUMN_TIME
						+ " > datetime('now', 'localtime')", null, null, null,
				TablePeriod.COLUMN_TIME, "1");

		if (cursor == null || cursor.getCount() <= 0) {
			return null;
		}

		cursor.moveToFirst();
		Period result = periodFromCursor(cursor);
		return result;

	}

	public SparseArrayCompat<Period> listPeriodsFromDay(Calendar day) {
		SimpleDateFormat dayFormat = new SimpleDateFormat(DATE_FORMAT,
				Locale.getDefault());
		log("listing periods for " + dayFormat.format(day.getTime()));
		Cursor cursor = db.query(
				TablePeriod.TABLE_NAME,
				TablePeriod.COLUMNS,
				TablePeriod.COLUMN_TIME + " LIKE '"
						+ dayFormat.format(day.getTime()) + "%'", null, null,
				null, TablePeriod.COLUMN_PREF_ID);

		SparseArrayCompat<Period> result = new SparseArrayCompat<Period>(8);

		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.moveToNext()) {
				Period p = periodFromCursor(cursor);
				result.put(p.getId(), p);
			}
		}

		return result;
	}

	public String exportCSV(Calendar dateStart, Calendar dateEnd) {
		SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT,
				Locale.getDefault());
		String start = dateFormat.format(dateStart.getTime());
		String end = dateFormat.format(dateEnd.getTime());

		Cursor cursor = db.query(TablePeriod.TABLE_NAME, new String[] {
				TablePeriod.COLUMN_PREF_ID, TablePeriod.COLUMN_TIME },
				TablePeriod.COLUMN_TIME + " BETWEEN '" + start + "' AND '"
						+ end + "'", null, null, null, TablePeriod.COLUMN_TIME);

		if (cursor.getCount() > 0) {
			String export = "";
			String header = ctx.getString(R.string.header_date) + "\t"
					+ ctx.getString(R.string.header_period) + "\t"
					+ ctx.getString(R.string.header_time) + "\n";
			export += header;
			Log.d("Export CSV", header);
			while (cursor.moveToNext()) {
				String dateTime = cursor.getString(1);
				String date = dateTime.substring(0, 10);
				String time = dateTime.substring(11);
				String line = "\"" + date + "\"\t\"" + cursor.getString(0)
						+ "\"\t\"" + time + "\"\n";
				export += line;
				Log.d("Export CSV", line);
			}
			return export;
		}
		return null;
	}
	// public SparseArrayCompat<Period> listPeriodsFromDay(WorkDay day) {
	// if (day.id > 0) {
	// return listPeriodsFromDay(day.id);
	// } else {
	// return null;
	// }
	// }

}
