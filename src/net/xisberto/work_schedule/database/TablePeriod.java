package net.xisberto.work_schedule.database;

public abstract class TablePeriod {

	public static final String TABLE_NAME = "Periods",
			COLUMN_ID = "rowid as _id",
			COLUMN_PREF_ID = "pref_id",
			COLUMN_TIME = "time",
			COLUMN_ENABLED = "enabled",
			CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "( " +
					COLUMN_PREF_ID + " INTEGER NOT NULL, " +
					COLUMN_TIME + " TEXT NOT NULL, " +
					COLUMN_ENABLED + " INTEGER NOT NULL DEFAULT 0 " +
					");";
	
	public static final String[] COLUMNS = { COLUMN_ID, COLUMN_PREF_ID,
			COLUMN_TIME, COLUMN_ENABLED };

	public static void main(String[] args) {
		System.out.println(CREATE_TABLE);
	}
}
