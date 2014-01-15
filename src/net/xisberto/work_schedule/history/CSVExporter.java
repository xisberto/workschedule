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
package net.xisberto.work_schedule.history;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.xisberto.work_schedule.database.Database;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

public class CSVExporter extends AsyncTask<Calendar, Void, Uri> {
	public interface CSVExporterCallback {
		public void shareUri(Uri uri);
	}

	private ViewHistoryActivity activity;

	public CSVExporter(ViewHistoryActivity activity) {
		super();
		if (activity instanceof CSVExporterCallback) {
			this.activity = activity;
		} else {
			throw new ClassCastException(
					"Activity must implement CSVExporterCallback");
		}
	}

	/* Checks if external storage is available for read and write */
	public boolean isExternalStorageWritable() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
			return true;
		}
		return false;
	}

	@Override
	protected Uri doInBackground(Calendar... params) {
		Database database = Database.getInstance(activity);
		Calendar startDate = Calendar.getInstance(), endDate = Calendar
				.getInstance();
		if (params[0] != null) {
			startDate = params[0];
		}
		if (params.length > 1 && params[1] != null) {
			endDate = params[1];
		}

		String content = database.exportCSV(startDate, endDate);
		SimpleDateFormat dateFormat = new SimpleDateFormat(
				Database.DATE_FORMAT, Locale.getDefault());
		String filename = "export_" + dateFormat.format(startDate.getTime())
				+ "_" + dateFormat.format(endDate.getTime()) + ".csv";
		File file;
		File dir;

		if (isExternalStorageWritable()) {
			dir = new File(Environment.getExternalStorageDirectory(),
					"WorkSchedule");
			if (!dir.exists()) {
				dir.mkdirs();
			}
		} else {
			dir = activity.getCacheDir();
		}
		file = new File(dir, filename);
		Log.d("CVSExporter", "saving file on " + file.getAbsolutePath());

		FileOutputStream outputStream;
		try {
			outputStream = new FileOutputStream(file);
			outputStream.write(content.getBytes());
			outputStream.close();
		} catch (NullPointerException e) {
			e.printStackTrace();
			return null;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		return Uri.fromFile(file);

	}

	@Override
	protected void onPostExecute(Uri result) {
		activity.shareUri(result);
	}

}
