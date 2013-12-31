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
	private ViewHistoryActivity activity;

	public CSVExporter(ViewHistoryActivity context) {
		super();
		this.activity = context;
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
		String filename = "export_"
				+ dateFormat.format(startDate.getTime()) + "_"
				+ dateFormat.format(endDate.getTime()) + ".csv";
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