package net.xisberto.work_schedule.widget;

import net.xisberto.work_schedule.MainActivity;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.settings.Settings;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.RemoteViews;

public class WidgetNextProvider extends WidgetNextMinimalProvider {

	@Override
	protected RemoteViews prepareWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId, int layout_id) {
		RemoteViews views = super.prepareWidget(context, appWidgetManager,
				appWidgetId, R.layout.widget_next_alarm_horizontal);

		// Set an intent to open MainActivity setting a period
		Intent intentAction = new Intent(MainActivity.ACTION_SET_PERIOD);
		intentAction
				.setComponent(new ComponentName(context, MainActivity.class));
		intentAction.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntentAction = PendingIntent.getActivity(context,
				1, intentAction, 0);
		views.setOnClickPendingIntent(R.id.text_label, pendingIntentAction);

		Settings settings = Settings.getInstance(context.getApplicationContext());
		Bundle info = settings.getNextAlarm();
		String period_label = info.getString(Settings.EXTRA_PERIOD_LABEL);
		String time = info.getString(Settings.EXTRA_PERIOD_TIME);
		if (!time.equals("")) {
			period_label += "\n" + time;
		}
		views.setTextViewText(R.id.text_label, period_label);

		return views;
	}

}
