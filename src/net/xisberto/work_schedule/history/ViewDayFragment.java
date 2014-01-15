/*******************************************************************************
 * Copyright 2014 xisberto
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package net.xisberto.work_schedule.history;

import java.util.Calendar;

import net.xisberto.work_schedule.PeriodListAdapter;
import net.xisberto.work_schedule.R;
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
