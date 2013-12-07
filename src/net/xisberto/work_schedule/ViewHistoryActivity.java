package net.xisberto.work_schedule;

import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
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

		if ((savedInstanceState != null && savedInstanceState
				.containsKey(CURRENT_DAY))) {
			current_day = (Calendar) savedInstanceState
					.getSerializable(CURRENT_DAY);
		} else {
			current_day = Calendar.getInstance();
		}

		pager_adapter = new HistoryPageAdapter(getSupportFragmentManager());
		view_pager = (ViewPager) findViewById(R.id.pager);
		view_pager.setAdapter(pager_adapter);

		TabPageIndicator pager_indicator = (TabPageIndicator) findViewById(R.id.pager_indicator);
		pager_indicator.setViewPager(view_pager);

		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		setupTabs();

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(CURRENT_DAY, current_day);
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
