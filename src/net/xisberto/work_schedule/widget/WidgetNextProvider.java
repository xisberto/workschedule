package net.xisberto.work_schedule.widget;

import net.xisberto.work_schedule.MainActivity;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.database.Database;
import net.xisberto.work_schedule.database.Period;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
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

		Period period = Database.getInstance(context).getNextAlarm();
		String info = null;
		if (period != null) {
			info = context.getString(period.getLabelId()) + "\n"
					+ period.formatTime(DateFormat.is24HourFormat(context));
		} else {
			info = context.getString(R.string.no_alarm);
		}
		views.setTextViewText(R.id.text_label, info);

		return views;
	}

}
