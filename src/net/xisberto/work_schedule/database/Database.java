package net.xisberto.work_schedule.database;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.v4.util.SparseArrayCompat;
import android.text.format.DateFormat;

public class Database extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "work_schedule";
	private static final int DATABASE_VERSION = 1;

	public static final String DATE_FORMAT = "yyyy-MM-dd",
			TIME_FORMAT = "HH:mm:ss", DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

	private static Database instance;

	private SQLiteDatabase db;

	private Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		db = getWritableDatabase();
	}

	public static synchronized Database getInstance(Context context) {
		if (instance == null) {
			instance = new Database(context.getApplicationContext());
		}
		return instance;
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

	private Calendar parseCalendar(String string) {
		Calendar cal = Calendar.getInstance();
		try {
			SimpleDateFormat dateFormat = new SimpleDateFormat(DATETIME_FORMAT,
					Locale.getDefault());
			cal.setTime(dateFormat.parse(string));
		} catch (ParseException e) {
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
		cv.put(TablePeriod.COLUMN_TIME,
				DateFormat.format(DATETIME_FORMAT, period.time).toString());
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
		day.set(Calendar.SECOND, 0);
		Cursor cursor = db.query(
				TablePeriod.TABLE_NAME,
				TablePeriod.COLUMNS,
				TablePeriod.COLUMN_PREF_ID + " = ? AND "
						+ TablePeriod.COLUMN_TIME + " LIKE ?",
				new String[] { Integer.toString(pref_id),
						DateFormat.format(DATE_FORMAT, day).toString() + "%" },
				null, null, null);

		if (cursor == null || cursor.getCount() <= 0) {
			return null;
		}

		cursor.moveToFirst();
		return periodFromCursor(cursor);
	}

	public Period getNextAlarm() {
		Cursor cursor = db.query(
				TablePeriod.TABLE_NAME,
				TablePeriod.COLUMNS,
				TablePeriod.COLUMN_ENABLED + " = 1 AND "
						+ TablePeriod.COLUMN_TIME + " > DATETIME('NOW')",
				null, null, null,
				TablePeriod.COLUMN_TIME, "1");

		if (cursor == null || cursor.getCount() <= 0) {
			return null;
		}

		cursor.moveToFirst();
		return periodFromCursor(cursor);

	}

	public SparseArrayCompat<Period> listPeriodsFromDay(Calendar day) {
		Cursor cursor = db.query(TablePeriod.TABLE_NAME, TablePeriod.COLUMNS,
				TablePeriod.COLUMN_TIME + " LIKE ?%", new String[] { DateFormat
						.format(DATE_FORMAT, day).toString() }, null, null,
				TablePeriod.COLUMN_PREF_ID);

		if (cursor == null || cursor.getCount() <= 0) {
			return null;
		}

		SparseArrayCompat<Period> result = new SparseArrayCompat<Period>(8);
		while (cursor.moveToNext()) {
			Period p = periodFromCursor(cursor);
			result.put(p.getId(), p);
		}

		return result;
	}

	// public SparseArrayCompat<Period> listPeriodsFromDay(WorkDay day) {
	// if (day.id > 0) {
	// return listPeriodsFromDay(day.id);
	// } else {
	// return null;
	// }
	// }

}
