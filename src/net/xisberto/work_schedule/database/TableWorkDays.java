package net.xisberto.work_schedule.database;


public class TableWorkDays {

	public static final String TABLE_NAME = "Workdays",
			COLUMN_ID = "_id",
			COLUMN_DAY = "day";
	private static TableWorkDays instance;
	
	private TableWorkDays() {
		
	}
	
	public static TableWorkDays getInstance(Database database) {
		if (instance == null) {
			instance = new TableWorkDays();
			if (! database.tableExists(TABLE_NAME)) {
				
			}
		}
		return instance;
	}
	
}
