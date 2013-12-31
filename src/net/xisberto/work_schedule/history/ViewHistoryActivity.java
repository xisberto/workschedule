package net.xisberto.work_schedule.history;

import java.util.Calendar;

import net.xisberto.work_schedule.BuildConfig;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.database.Period;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog.OnDateSetListener;
import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment.DatePickerDialogHandler;
import com.viewpagerindicator.TabPageIndicator;

public class ViewHistoryActivity extends SherlockFragmentActivity implements
		OnDateSetListener, DatePickerDialogHandler {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_history);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ViewPager view_pager = (ViewPager) findViewById(R.id.pager);
		TabPageIndicator pager_indicator = (TabPageIndicator) findViewById(R.id.pager_indicator);

		HistoryPagerAdapter pager_adapter = new HistoryPagerAdapter(
				getSupportFragmentManager());
		view_pager.setAdapter(pager_adapter);

		pager_indicator.setViewPager(view_pager);
		view_pager.setCurrentItem(HistoryPagerAdapter.SIZE);

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
		ViewPager view_pager = (ViewPager) findViewById(R.id.pager);
		HistoryPagerAdapter adapter = (HistoryPagerAdapter) view_pager
				.getAdapter();
		Calendar selected_day = adapter.getSelectedDay(view_pager
				.getCurrentItem());
		CalendarDatePickerDialog dialog;
		switch (item.getItemId()) {
		case R.id.menu_go_today:
			view_pager = (ViewPager) findViewById(R.id.pager);
			view_pager.setCurrentItem(HistoryPagerAdapter.SIZE);
			break;
		case R.id.menu_share:
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
				dialog = CalendarDatePickerDialog.newInstance(this,
						selected_day.get(Calendar.YEAR),
						selected_day.get(Calendar.MONTH),
						selected_day.get(Calendar.DAY_OF_MONTH));
				dialog.show(getSupportFragmentManager(), "date_picker");
			} else {
				DatePickerBuilder builder = new DatePickerBuilder()
						.setFragmentManager(getSupportFragmentManager())
						.setStyleResId(R.style.BetterPickersDialogFragment_Light)
						.setDayOfMonth(selected_day.get(Calendar.DAY_OF_MONTH))
						.setMonthOfYear(selected_day.get(Calendar.MONTH))
						.setYear(selected_day.get(Calendar.YEAR));
				builder.show();
			}
			break;
		case R.id.menu_fake_data:
			dialog = CalendarDatePickerDialog.newInstance(
					new OnDateSetListener() {
						@Override
						public void onDateSet(CalendarDatePickerDialog dialog,
								int year, int monthOfYear, int dayOfMonth) {
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
					}, selected_day.get(Calendar.YEAR), selected_day
							.get(Calendar.MONTH), selected_day
							.get(Calendar.DAY_OF_MONTH));
			dialog.show(getSupportFragmentManager(), "date_picker");
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onDateSet(CalendarDatePickerDialog dialog, int year,
			int monthOfYear, int dayOfMonth) {
		startExporter(year, monthOfYear, dayOfMonth);
	}

	@Override
	public void onDialogDateSet(int reference, int year, int monthOfYear,
			int dayOfMonth) {
		startExporter(year, monthOfYear, dayOfMonth);
	}

	private void startExporter(int year, int monthOfYear, int dayOfMonth) {
		Log.d("History", "onDateSelected");
		Calendar startDate = Calendar.getInstance();
		startDate.set(Calendar.YEAR, year);
		startDate.set(Calendar.MONTH, monthOfYear);
		startDate.set(Calendar.DAY_OF_MONTH, dayOfMonth);
		CSVExporter exporter = new CSVExporter(ViewHistoryActivity.this);
		exporter.execute(startDate);
	}

	public void shareUri(Uri uri) {
		Log.d("History", "shareURI");
		if (uri == null) {
			AlertDialog dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.app_name)
					.setMessage(R.string.txt_no_data)
					.setPositiveButton(android.R.string.ok, null).create();
			dialog.show();
			return;
		}
		Intent intentShare = new Intent(Intent.ACTION_SEND);
		intentShare.setType("text/csv");
		intentShare.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intentShare, getResources()
				.getString(R.string.menu_share)));
	}

}
