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

import java.text.DateFormat;
import java.util.Calendar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.Log;

public class HistoryPagerAdapter extends FragmentPagerAdapter {

	public static final int SIZE = 120;

	public HistoryPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	public Calendar getSelectedDay(int position) {
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
