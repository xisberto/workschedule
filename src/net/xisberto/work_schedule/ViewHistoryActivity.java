package net.xisberto.work_schedule;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.NavUtils;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ViewHistoryActivity extends SherlockFragmentActivity {
	private static final String CURRENT_DAY = "current_day";
	private Calendar day;
	private ViewDayFragment fragment;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_history);

		if ((savedInstanceState != null && savedInstanceState.containsKey(CURRENT_DAY))) {
			day = (Calendar) savedInstanceState.getSerializable(CURRENT_DAY);
		} else {
			day = Calendar.getInstance();
		}

		fragment = ViewDayFragment.newInstance(day);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.view_day, fragment).commit();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
