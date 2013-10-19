package net.xisberto.work_schedule.database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "work_schedule";
	private static final int DATABASE_VERSION = 1;

	public Database(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {

	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

	}

	public boolean tableExists(String tableName) {
		SQLiteDatabase sqlite = getReadableDatabase();
		Cursor c = sqlite
				.rawQuery(
						"select DISTINCT tbl_name from sqlite_master where tbl_name = '?'",
						new String[] { tableName });
		if (c != null) {
			if (c.getCount() > 0) {
				c.close();
				return true;
			}
			c.close();
		}
		return false;
	}

}
