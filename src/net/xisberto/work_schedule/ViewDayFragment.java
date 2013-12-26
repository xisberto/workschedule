package net.xisberto.work_schedule;

import java.util.Calendar;

import net.xisberto.work_schedule.database.Database;
import net.xisberto.work_schedule.database.Period;
import android.os.Bundle;
import android.support.v4.util.SparseArrayCompat;

import com.actionbarsherlock.app.SherlockListFragment;

public class ViewDayFragment extends SherlockListFragment {

	public static ViewDayFragment newInstance(Calendar calendar) {
		ViewDayFragment fragment = new ViewDayFragment();
		Bundle args = new Bundle();
		args.putSerializable("day", calendar);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Database database = Database.getInstance(getActivity());
		Calendar day = (Calendar) getArguments().getSerializable("day");
		SparseArrayCompat<Period> periods = database.listPeriodsFromDay(day);
		setListAdapter(new PeriodListAdapter(getActivity(), periods, false));
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onStart() {
		super.onStart();
		setEmptyText(getString(R.string.no_alarm));
	}

}
