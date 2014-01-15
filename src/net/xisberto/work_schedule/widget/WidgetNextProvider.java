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
package net.xisberto.work_schedule.widget;

import net.xisberto.work_schedule.MainActivity;
import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.database.Database;
import net.xisberto.work_schedule.database.Period;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.text.format.DateFormat;
import android.widget.RemoteViews;

public class WidgetNextProvider extends AppWidgetProvider {
	/**
	 * Allows the app to update this widget at an arbitrary time, sending a
	 * broadcast intent with this action
	 */
	public static final String MY_ACTION_UPDATE = "net.xisberto.work_schedule.update_widgets";

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(MY_ACTION_UPDATE)) {
			int[] appWidgetIds = AppWidgetManager.getInstance(context)
					.getAppWidgetIds(new ComponentName(context, getClass()));
			onUpdate(context, AppWidgetManager.getInstance(context),
					appWidgetIds);
			return;
		}
		super.onReceive(context, intent);
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		for (int appWidgetId : appWidgetIds) {
			RemoteViews views = prepareWidget(context, appWidgetManager,
					appWidgetId, R.layout.widget_next_alarm);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	protected RemoteViews prepareWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId, int layout_id) {

		RemoteViews views = new RemoteViews(context.getPackageName(), layout_id);

		// Set an intent to open MainActivity
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
		views.setOnClickPendingIntent(R.id.image_icon, pendingIntent);

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
