package net.xisberto.work_schedule.model;

import java.util.ArrayList;

import android.text.format.Time;

public class WorkDay {
	public Time day;
	private ArrayList<Period> periods;
	
	public WorkDay() {
		day = new Time();
		day.setToNow();
		
		periods = new ArrayList<Period>();
		for (int period_id : Period.ids) {
			periods.add(new Period(period_id));
		}
	}
}
