package net.xisberto.work_schedule;

import java.util.Calendar;

import net.xisberto.work_schedule.database.Database;
import net.xisberto.work_schedule.database.Period;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;

import com.actionbarsherlock.app.SherlockListFragment;

public class ViewDayFragment extends SherlockListFragment {
	private Calendar day;

	public static ViewDayFragment newInstance(Calendar calendar) {
		ViewDayFragment fragment = new ViewDayFragment();
		fragment.day = calendar;
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Database database = Database.getInstance(getActivity());
		SparseArrayCompat<Period> periods = database.listPeriodsFromDay(day);
		setListAdapter(new PeriodListAdapter(getActivity(), periods));
	}
}
