package net.xisberto.work_schedule.history;

import java.util.Calendar;

import net.xisberto.work_schedule.BuildConfig;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.database.Period;
import net.xisberto.work_schedule.history.CSVExporter.CSVExporterCallback;
import net.xisberto.work_schedule.settings.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.actionbarsherlock.app.SherlockDialogFragment;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog;
import com.doomonafireball.betterpickers.calendardatepicker.CalendarDatePickerDialog.OnDateSetListener;
import com.doomonafireball.betterpickers.datepicker.DatePickerBuilder;
import com.doomonafireball.betterpickers.datepicker.DatePickerDialogFragment.DatePickerDialogHandler;
import com.viewpagerindicator.TabPageIndicator;

public class ViewHistoryActivity extends SherlockFragmentActivity implements
		OnDateSetListener, DatePickerDialogHandler, CSVExporterCallback {

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

		switch (item.getItemId()) {
		case R.id.menu_go_today:
			view_pager = (ViewPager) findViewById(R.id.pager);
			view_pager.setCurrentItem(HistoryPagerAdapter.SIZE);
			break;
		case R.id.menu_share:
			if (Settings.getInstance(this).getShowInstructions()) {
				InstrucionDialog.newInstance(this, selected_day).show(
						getSupportFragmentManager(), "instruction");
			} else {
				showDatePicker(selected_day);
			}
			break;
		case R.id.menu_fake_data:
			CalendarDatePickerDialog dialog = CalendarDatePickerDialog
					.newInstance(new OnDateSetListener() {
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

	@Override
	public void shareUri(Uri uri) {
		Log.d("History", "shareURI");
		if (uri == null) {
			new AlertDialog.Builder(this).setTitle(R.string.app_name)
					.setMessage(R.string.txt_no_data)
					.setPositiveButton(android.R.string.ok, null).show();
			return;
		}
		Intent intentShare = new Intent(Intent.ACTION_SEND);
		intentShare.setType("text/csv");
		intentShare.putExtra(Intent.EXTRA_STREAM, uri);
		startActivity(Intent.createChooser(intentShare, getResources()
				.getString(R.string.menu_share)));
	}

	private void showDatePicker(Calendar selected_day) {
		CalendarDatePickerDialog dialog;
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

	public static class InstrucionDialog extends SherlockDialogFragment {
		private ViewHistoryActivity activity;
		private Calendar selected_day;
		private View view;

		public static InstrucionDialog newInstance(
				ViewHistoryActivity activity, Calendar selected_day) {
			InstrucionDialog dialog = new InstrucionDialog();
			dialog.activity = activity;
			dialog.selected_day = selected_day;
			return dialog;
		}

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			OnClickListener callback = new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which) {
					case DialogInterface.BUTTON_POSITIVE:
						CheckBox checkBox = (CheckBox) view
								.findViewById(R.id.check_show_instructions);
						if (checkBox.isChecked()) {
							Settings.getInstance(activity).setShowInstructions(
									false);
						}
						activity.showDatePicker(selected_day);
						break;
					default:
						break;
					}
				}
			};

			view = LayoutInflater.from(getActivity()).inflate(
					R.layout.dialog_instructions, null);

			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle(R.string.app_name).setView(view)
					.setPositiveButton(android.R.string.ok, callback)
					.setNegativeButton(android.R.string.cancel, callback);
			return builder.create();
		}
	}
}
