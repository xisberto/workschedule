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

import net.xisberto.work_schedule.database.Period;
import net.xisberto.work_schedule.settings.Settings;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.util.SparseArrayCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

public class PeriodListAdapter implements ListAdapter {
	private Context context;
	private SparseArrayCompat<Period> periods;

	public PeriodListAdapter(Context context, SparseArrayCompat<Period> periods) {
		this.context = context;
		this.periods = periods;
	}

	@Override
	public int getCount() {
		return periods.size();
	}

	@Override
	public Object getItem(int position) {
		return periods.valueAt(position);
	}

	@Override
	public long getItemId(int position) {
		return periods.keyAt(position);
	}

	@Override
	public int getItemViewType(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.period_list_item, null);
		}

		final Period period = (Period) getItem(position);
		final Settings settings = Settings.getInstance(context);

		((TextView) convertView.findViewById(R.id.period_label)).setText(context
				.getString(period.getLabel()));


		String inFormat = "hh:mm aa";
		if (DateFormat.is24HourFormat(context)) {
			inFormat = "kk:mm";
		}
		((TextView) convertView.findViewById(R.id.period_time)).setText(
				DateFormat.format(inFormat, period.time));

		CompoundButton check_alarm = (CompoundButton) convertView.findViewById(R.id.check_alarm);
		check_alarm.setChecked(settings.isAlarmSet(period.getId()));
		check_alarm.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View check_box) {
				settings.setAlarm(period);
			}
		});

		return convertView;
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
