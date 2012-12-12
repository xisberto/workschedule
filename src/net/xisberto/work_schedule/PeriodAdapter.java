package net.xisberto.work_schedule;

import java.sql.Date;

import net.xisberto.work_schedule.Settings.Period;
import android.content.Context;
import android.database.DataSetObserver;
import android.text.format.DateFormat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

public class PeriodAdapter implements ListAdapter {
	Context context;
	private SparseArray<Period> list;

	public PeriodAdapter(Context context, SparseArray<Period> periods) {
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
		String format = "hh:mmaa";
		if (DateFormat.is24HourFormat(context)) {
			format = "kk:mm";
		}
		((TextView) view.findViewById(R.id.period_time)).setText(DateFormat
				.format(format, settings.getCalendar(period_pref_key)));
		
		CheckBox check_alarm = (CheckBox) view.findViewById(R.id.check_alarm);
		check_alarm.setChecked(settings.isAlarmSet(period.pref_id));
		check_alarm.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View view) {
				settings.setAlarm(period, settings.getCalendar(period_pref_key), ((CheckBox)view).isChecked());
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
