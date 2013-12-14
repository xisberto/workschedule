package net.xisberto.work_schedule;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;

import com.viewpagerindicator.TabPageIndicator;

public class HistoryPageAdapter extends FragmentPagerAdapter implements
		ViewPager.OnPageChangeListener {

	private List<Calendar> days;
	public TabPageIndicator indicator;

	public HistoryPageAdapter(FragmentManager fm, TabPageIndicator indicator) {
		super(fm);
		this.indicator = indicator;
		days = new ArrayList<Calendar>();
		for (int i = -4; i <= 0; i++) {
			Calendar cal = Calendar.getInstance();
			cal.add(Calendar.DAY_OF_MONTH, i);
			days.add(cal);
		}
	}

	@Override
	public CharSequence getPageTitle(int position) {
		DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
		return dateFormat.format(days.get(position).getTime());
	}

	@Override
	public Fragment getItem(int index) {
		ViewDayFragment fragment = ViewDayFragment.newInstance(days.get(index));
		return fragment;
	}

	@Override
	public long getItemId(int position) {
		return days.get(position).getTimeInMillis();
	}

	@Override
	public int getItemPosition(Object object) {
		return POSITION_NONE;
	}

	@Override
	public int getCount() {
		return days.size();
	}

	@Override
	public void onPageSelected(int position) {
		Calendar cal;
		Log.d("Pager", "position: " + position);
		switch (position) {
		case 0:
			cal = (Calendar) days.get(0).clone();
			cal.add(Calendar.DAY_OF_MONTH, -1);
			days.add(0, cal);
			Log.d("Pager", "size: " + days.size());
			notifyDataSetChanged();
			indicator.notifyDataSetChanged();
			indicator.setCurrentItem(position+1);
			break;
		default:
			break;
		}
	}

	@Override
	public void onPageScrollStateChanged(int state) {

	}

	@Override
	public void onPageScrolled(int position, float positionOffset,
			int positionOffsetPixels) {

	}

}
