package net.xisberto.work_schedule;

import java.text.DateFormat;
import java.util.Calendar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class HistoryPageAdapter extends FragmentPagerAdapter {

	public static final int SIZE = 120;

	public HistoryPageAdapter(FragmentManager fm) {
		super(fm);
	}

	private Calendar getSelectedDay(int position) {
		Calendar selected_day = Calendar.getInstance();
		selected_day.add(Calendar.DATE, -SIZE + position + 1);
		return selected_day;
	}

	@Override
	public CharSequence getPageTitle(int position) {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.DEFAULT);
		return dateFormat.format(getSelectedDay(position).getTime());
	}

	@Override
	public Fragment getItem(int index) {
		ViewDayFragment fragment = ViewDayFragment
				.newInstance(getSelectedDay(index));
		Log.d("Pager", "fragment at " + index + " is " + fragment.toString());
		return fragment;
	}

	@Override
	public int getCount() {
		return SIZE;
	}

}
