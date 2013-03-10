package net.xisberto.work_schedule;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetNextProvider extends AppWidgetProvider {
	public static final String ACTION_UPDATE = "net.xisberto.work_schedule.update_widgets";

	public WidgetNextProvider() {
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(ACTION_UPDATE)) {
			int[] appWidgetIds = AppWidgetManager.getInstance(context)
					.getAppWidgetIds(new ComponentName(context, getClass()));

			Intent updateIntent = new Intent(context, getClass());
			updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					appWidgetIds);
			context.sendBroadcast(updateIntent);
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		Log.d(getClass().getCanonicalName(), "Updating widgets");
		RemoteViews views = new RemoteViews(context.getPackageName(),
				R.layout.widget_next_alarm);

		// Set an intent to open MainActivity
		Intent intent = new Intent(context, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

		Settings settings = new Settings(context.getApplicationContext());
		Bundle info = settings.getNextAlarm();
		if (info != null) {
			String period_label = info.getString(Settings.EXTRA_PERIOD_LABEL);
			String time = info.getString(Settings.EXTRA_PERIOD_TIME);
			Log.d(getClass().getCanonicalName(), "Title: " + period_label + "; Time: "
					+ time);
			views.setTextViewText(R.id.text_period_label, period_label);
			views.setTextViewText(R.id.text_time, time);
		} else {
			views.setTextViewText(R.id.text_period_label, context.getString(R.string.no_alarm));
			views.setTextViewText(R.id.text_time, "");
		}

		appWidgetManager.updateAppWidget(appWidgetIds, views);
	}

}
