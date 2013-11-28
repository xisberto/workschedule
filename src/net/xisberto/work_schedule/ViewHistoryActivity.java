package net.xisberto.work_schedule;

import java.text.DateFormat;
import java.util.Calendar;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.MenuItem;

public class ViewHistoryActivity extends SherlockFragmentActivity {
	private static final String CURRENT_DAY = "current_day";
	private Calendar day;
	private DateFormat dateFormat;
	private ViewDayFragment fragment;
	private Button btn_prev, btn_next;
	private TextView text_date;

	private void setup(Calendar date) {
		Log.d("setup", "setup for " + dateFormat.format(date.getTime()));
		text_date.setText(dateFormat.format(date.getTime()));
		date.add(Calendar.DAY_OF_MONTH, -1);
		Log.d("setup", "prev is " + dateFormat.format(date.getTime()));
		btn_prev.setText(dateFormat.format(date.getTime()));
		date.add(Calendar.DAY_OF_MONTH, +2);
		Log.d("setup", "next is " + dateFormat.format(date.getTime()));
		btn_next.setText(dateFormat.format(date.getTime()));

		date.add(Calendar.DAY_OF_MONTH, -1);

		OnClickListener clickListener = new OnClickListener() {
			@Override
			public void onClick(View v) {
				int anim_enter, anim_exit;
				switch (v.getId()) {
				case R.id.btn_prev:
					day.add(Calendar.DAY_OF_MONTH, -1);
					setup(day);
					anim_enter = R.anim.enter_right;
					anim_exit = R.anim.exit_right;
					break;
				case R.id.btn_next:
					day.add(Calendar.DAY_OF_MONTH, 1);
					setup(day);
					anim_enter = R.anim.enter_left;
					anim_exit = R.anim.exit_left;
					break;
				default:
					return;
				}

				ViewDayFragment next_frag = ViewDayFragment.newInstance(day);
				getSupportFragmentManager().beginTransaction()
						.setCustomAnimations(anim_enter, anim_exit)
						.replace(R.id.view_day, next_frag).commit();
				fragment = next_frag;

			}
		};

		btn_prev.setOnClickListener(clickListener);
		btn_next.setOnClickListener(clickListener);
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_history);

		if ((savedInstanceState != null && savedInstanceState
				.containsKey(CURRENT_DAY))) {
			day = (Calendar) savedInstanceState.getSerializable(CURRENT_DAY);
		} else {
			day = Calendar.getInstance();
		}

		dateFormat = DateFormat.getDateInstance(DateFormat.SHORT);

		btn_prev = (Button) findViewById(R.id.btn_prev);
		btn_next = (Button) findViewById(R.id.btn_next);
		text_date = (TextView) findViewById(R.id.text_date);

		setup(day);

		fragment = ViewDayFragment.newInstance(day);

		getSupportFragmentManager().beginTransaction()
				.replace(R.id.view_day, fragment).commit();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
