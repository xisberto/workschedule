package net.xisberto.work_schedule;

import java.text.DateFormat;
import java.util.Calendar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

public class HistoryPageAdapter extends FragmentStatePagerAdapter {

	public HistoryPageAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public CharSequence getPageTitle(int position) {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		return dateFormat.format(Calendar.getInstance().getTime());
	}

	@Override
	public Fragment getItem(int index) {
		Calendar selected_day = Calendar.getInstance();
		selected_day.add(Calendar.DAY_OF_MONTH, index);
		return ViewDayFragment.newInstance(selected_day);
	}

	@Override
	public int getCount() {
		return 5;
	}

}
