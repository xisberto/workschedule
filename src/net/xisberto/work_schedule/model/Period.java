package net.xisberto.work_schedule.model;

import java.util.Arrays;

import net.xisberto.work_schedule.R;
import android.text.format.Time;

public class Period {
	private int id;
	private int labelId;
	public Time time;
	public boolean enabled;
	
	public static final int[] ids = new int[] {
		R.string.fstp_entrance,
		R.string.fstp_exit,
		R.string.sndp_entrance,
		R.string.sndp_exit,
		R.string.fste_entrance,
		R.string.fste_exit,
		R.string.snde_entrance,
		R.string.snde_exit
	};

	public Period(int id) {
		if (Arrays.asList(ids).contains(id)) {
			this.id = id;
		} else {
			this.id = -1;
		}
		switch (id) {
		case R.string.fstp_entrance:
			labelId = R.string.lbl_fstp_entrance;
			break;
		case R.string.fstp_exit:
			labelId = R.string.lbl_fste_exit;
			break;
		case R.string.sndp_entrance:
			labelId = R.string.lbl_sndp_entrance;
			break;
		case R.string.sndp_exit:
			labelId = R.string.lbl_sndp_exit;
			break;
		case R.string.fste_entrance:
			labelId = R.string.lbl_fste_entrance;
			break;
		case R.string.fste_exit:
			labelId = R.string.lbl_fste_exit;
			break;
		case R.string.snde_entrance:
			labelId = R.string.lbl_snde_entrance;
			break;
		case R.string.snde_exit:
			labelId = R.string.lbl_snde_exit;
			break;
		default:
			labelId = R.string.no_alarm;
			break;
		}
		time = new Time();
		time.setToNow();
		enabled = true;
	}

	public int getId() {
		return id;
	}
	
	public int getLabel() {
		return labelId;
	}
}
