package net.xisberto.work_schedule;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.RemoteViews;

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
			AppWidgetManager widgetManager = AppWidgetManager.getInstance(this);
			RemoteViews views = new RemoteViews(getPackageName(), R.layout.widget_next_alarm);
			widgetManager.updateAppWidget(mAppWidgetId, views);
		}
	}

}
