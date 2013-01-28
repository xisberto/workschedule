/*******************************************************************************
 * Copyright (c) 2012 Humberto Fraga.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Humberto Fraga - initial API and implementation
 ******************************************************************************/
package net.xisberto.work_schedule;

import net.xisberto.work_schedule.Settings.Period;
import android.content.Context;
import android.database.DataSetObserver;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.TextView;

public class PeriodListAdapter implements ListAdapter {
	Context context;
	private SparseArray<Period> list;

	public PeriodListAdapter(Context context, SparseArray<Period> periods) {
		this.context = context;
		list = periods;
	}

	@Override
	public int getCount() {
		return list.size();
	}

	@Override
	public Object getItem(int position) {
		return list.valueAt(position);
	}

	@Override
	public long getItemId(int position) {
		return (Integer) list.keyAt(position);
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.period_list_item, null);
		}

		final Period period = (Period) getItem(position);
		final Settings settings = new Settings(context);
		final String period_pref_key = context.getString(period.pref_id);

		((TextView) view.findViewById(R.id.period_label)).setText(context
				.getString(period.label_id));

		((TextView) view.findViewById(R.id.period_time)).setText(settings
				.formatCalendar(settings.getCalendar(period_pref_key)));

		CheckBox check_alarm = (CheckBox) view.findViewById(R.id.check_alarm);
		check_alarm.setChecked(settings.isAlarmSet(period.pref_id));
		check_alarm.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				settings.setAlarm(period,
						settings.getCalendar(period_pref_key), isChecked);
			}
		});
		check_alarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View clicked_view) {
			}
		});

		return view;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
	}

	@Override
	public boolean hasStableIds() {
		return true;
	}

	@Override
	public boolean isEmpty() {
		return getCount() == 0;
	}

	@Override
	public void registerDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean areAllItemsEnabled() {
		return true;
	}

	@Override
	public boolean isEnabled(int position) {
		return true;
	}

}
