package net.xisberto.work_schedule;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import net.xisberto.work_schedule.DatePickerFragment.OnDateSelectedListener;
import net.xisberto.work_schedule.database.Database;
import net.xisberto.work_schedule.database.Period;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;

public class ViewHistoryActivity extends SherlockFragmentActivity implements
		OnDateSelectedListener {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_history);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ViewPager view_pager = (ViewPager) findViewById(R.id.pager);
		TabPageIndicator pager_indicator = (TabPageIndicator) findViewById(R.id.pager_indicator);

		HistoryPageAdapter pager_adapter = new HistoryPageAdapter(
				getSupportFragmentManager());
		view_pager.setAdapter(pager_adapter);

		pager_indicator.setViewPager(view_pager);
		view_pager.setCurrentItem(HistoryPageAdapter.SIZE);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_history, menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		if (!BuildConfig.DEBUG) {
			menu.findItem(R.id.menu_fake_data).setVisible(false);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		DatePickerFragment dialog;
		switch (item.getItemId()) {
		case R.id.menu_go_today:
			ViewPager view_pager = (ViewPager) findViewById(R.id.pager);
			view_pager.setCurrentItem(HistoryPageAdapter.SIZE);
			break;
		case R.id.menu_share:
			dialog = DatePickerFragment.newInstance(this);
			dialog.show(getSupportFragmentManager(), "select_date");
			break;
		case R.id.menu_fake_data:
			dialog = DatePickerFragment
					.newInstance(new DatePickerFragment.OnDateSelectedListener() {
						@Override
						public void onDateSelected(int year, int monthOfYear,
								int dayOfMonth) {
							Calendar date = Calendar.getInstance();
							date.set(Calendar.YEAR, year);
							date.set(Calendar.MONTH, monthOfYear);
							date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

							while (date.before(Calendar.getInstance())) {
								if ((date.get(Calendar.DAY_OF_WEEK) != Calendar.SATURDAY)
										&& (date.get(Calendar.DAY_OF_WEEK) != Calendar.SUNDAY)) {
									for (int id : Period.ids) {
										Period period = new Period(id, date);
										period.persist(ViewHistoryActivity.this);
									}
								}
								date.add(Calendar.DAY_OF_MONTH, 1);
							}
						}
					});
			dialog.show(getSupportFragmentManager(), "select_date");
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onDateSelected(int year, int monthOfYear, int dayOfMonth) {
		Calendar startDate = Calendar.getInstance();
		startDate.set(Calendar.YEAR, year);
		startDate.set(Calendar.MONTH, monthOfYear);
		startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		CSVExporter exporter = new CSVExporter(ViewHistoryActivity.this);
		exporter.execute(startDate);
	}

	public void shareUri(Uri uri) {
		Intent intentShare = new Intent(Intent.ACTION_SEND);
		intentShare.setType("text/csv");
		intentShare.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intentShare, getResources()
				.getString(R.string.menu_share)));
	}

	public static class CSVExporter extends AsyncTask<Calendar, Void, Uri> {
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

		@SuppressLint("NewApi")
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

			if (isExternalStorageWritable()) {
				SimpleDateFormat dateFormat = new SimpleDateFormat(
						Database.DATE_FORMAT, Locale.getDefault());
				String filename = "export_"
						+ dateFormat.format(startDate.getTime()) + "_"
						+ dateFormat.format(endDate.getTime()) + ".csv";
				File file;
				File dir;
				if (Build.VERSION.SDK_INT > Build.VERSION_CODES.ECLAIR_MR1) {
					dir = activity.getExternalFilesDir(null);
				} else {
					dir = activity.getFilesDir();
				}
				if (!dir.exists()) {
					dir.mkdirs();
				}
				file = new File(dir, filename);

				Log.d("CVSExporter", "saving file on " + file.getAbsolutePath());
				FileOutputStream outputStream;
				try {
					outputStream = new FileOutputStream(file);
					outputStream.write(content.getBytes());
					outputStream.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
					return null;
				} catch (IOException e) {
					e.printStackTrace();
					return null;
				}

				return Uri.fromFile(file);
			}

			return null;

		}

		@Override
		protected void onPostExecute(Uri result) {
			activity.shareUri(result);
		}

	}

}
