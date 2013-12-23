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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_history);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		ViewPager view_pager = (ViewPager) findViewById(R.id.pager);
		TabPageIndicator pager_indicator = (TabPageIndicator) findViewById(R.id.pager_indicator);

		HistoryPageAdapter pager_adapter = new HistoryPageAdapter(getSupportFragmentManager());
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
		switch (item.getItemId()) {
		case R.id.menu_go_today:
			ViewPager view_pager = (ViewPager) findViewById(R.id.pager);
			view_pager.setCurrentItem(HistoryPageAdapter.SIZE);
			break;
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
