package net.xisberto.work_schedule;

import java.text.DateFormat;
import java.util.Calendar;

import net.xisberto.work_schedule.database.Period;
import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.DatePicker;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.viewpagerindicator.TabPageIndicator;

public class ViewHistoryActivity extends SherlockFragmentActivity {
	private static final String CURRENT_DAY = "current_day";
	private Calendar current_day;

	private HistoryPageAdapter pager_adapter;
	ViewPager view_pager;

	private void setupTabs() {

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_history);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		if ((savedInstanceState != null && savedInstanceState
				.containsKey(CURRENT_DAY))) {
			current_day = (Calendar) savedInstanceState
					.getSerializable(CURRENT_DAY);
		} else {
			current_day = Calendar.getInstance();
		}

		view_pager = (ViewPager) findViewById(R.id.pager);
		TabPageIndicator pager_indicator = (TabPageIndicator) findViewById(R.id.pager_indicator);

		pager_adapter = new HistoryPageAdapter(getSupportFragmentManager(),
				pager_indicator);
		view_pager.setAdapter(pager_adapter);

		pager_indicator.setViewPager(view_pager);
		pager_indicator.setCurrentItem(4);
		pager_indicator.setOnPageChangeListener(pager_adapter);

		setupTabs();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(CURRENT_DAY, current_day);
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
		switch (item.getItemId()) {
		case R.id.menu_fake_data:
			DialogFragment dialog = new SelectDate();
			dialog.show(getSupportFragmentManager(), "select_date");
			break;

		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	public static class SelectDate extends DialogFragment implements
			OnDateSetListener {

		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			Calendar now = Calendar.getInstance();
			int year = now.get(Calendar.YEAR);
			int month = now.get(Calendar.MONTH);
			int day = now.get(Calendar.DAY_OF_MONTH);

			return new DatePickerDialog(getActivity(), this, year, month, day);
		}

		@Override
		public void onDateSet(DatePicker view, int year, int monthOfYear,
				int dayOfMonth) {
			Calendar date = Calendar.getInstance();
			date.set(Calendar.YEAR, year);
			date.set(Calendar.MONTH, monthOfYear);
			date.set(Calendar.DAY_OF_MONTH, dayOfMonth);

			while (date.before(Calendar.getInstance())) {
				Log.d("SelectDate",
						"Adding date "
								+ DateFormat.getDateInstance(DateFormat.SHORT)
										.format(date.getTime()));
				if ((date.get(Calendar.DAY_OF_WEEK) == Calendar.SATURDAY)
						|| (date.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY)) {
					Log.d("SelectDate", "Skipping weekends");
				} else {
					for (int id : Period.ids) {
						Period period = new Period(id, date);
						period.persist(getActivity());
					}
				}
				date.add(Calendar.DAY_OF_MONTH, 1);
			}
		}
	}
}
