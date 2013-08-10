package net.xisberto.work_schedule;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class WidgetConfigActivity extends Activity {

	private int mAppWidgetId;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_widget_config);

		findViewById(R.id.btn_widget_ok).setOnClickListener(
				new OnClickListener() {

					@Override
					public void onClick(View v) {
						prepareWidget();
						Intent resultValue = new Intent();
						resultValue.putExtra(
								AppWidgetManager.EXTRA_APPWIDGET_ID,
								mAppWidgetId);
						setResult(RESULT_OK, resultValue);
						finish();
					}
				});
	}

	protected void prepareWidget() {
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			mAppWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
			//TODO: Configure here theme
			Intent updateIntent = new Intent(this, WidgetNextProvider.class);
			updateIntent.setAction(WidgetNextProvider.MY_ACTION_UPDATE);
			sendBroadcast(updateIntent);
		}
	}

}
