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
package net.xisberto.work_schedule;

import net.xisberto.work_schedule.database.Period;
import android.content.Context;
import android.database.DataSetObserver;
import android.support.v4.util.SparseArrayCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class PeriodListAdapter implements ListAdapter {
	private Context context;
	private SparseArrayCompat<Period> periods;
	private boolean show_checkboxes;

	public PeriodListAdapter(Context context, SparseArrayCompat<Period> periods) {
		this(context, periods, true);
	}

	public PeriodListAdapter(Context context,
			SparseArrayCompat<Period> periods, boolean show_checkboxes) {
		this.context = context;
		this.periods = periods;
		this.show_checkboxes = show_checkboxes;
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

		((TextView) convertView.findViewById(R.id.period_label))
				.setText(context.getString(period.getLabelId()));

		((TextView) convertView.findViewById(R.id.period_time)).setText(period
				.formatTime(DateFormat.is24HourFormat(context)));

		CompoundButton check_alarm = (CompoundButton) convertView
				.findViewById(R.id.check_alarm);
		if (show_checkboxes) {
			check_alarm.setChecked(period.enabled);
			check_alarm.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View check_box) {
					period.enabled = ((CompoundButton) check_box).isChecked();
					period.setAlarm(context, true);
					period.persist(context);
				}
			});
		} else {
			check_alarm.setVisibility(View.GONE);
			LinearLayout layout_labels = (LinearLayout) convertView.findViewById(R.id.layout_labels);
			LayoutParams params = (LayoutParams) layout_labels.getLayoutParams();
			params.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
			layout_labels.setLayoutParams(params);
		}

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
