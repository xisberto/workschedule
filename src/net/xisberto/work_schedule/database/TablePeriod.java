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

public abstract class TablePeriod {

	public static final String TABLE_NAME = "Periods",
			COLUMN_ID = "_id",
			COLUMN_PREF_ID = "pref_id",
			COLUMN_TIME = "time",
			COLUMN_ENABLED = "enabled",
			CREATE_TABLE = "CREATE TABLE IF NOT EXISTS " + TABLE_NAME + "( " +
					COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
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
