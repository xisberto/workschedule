/*******************************************************************************
 * Copyright (c) 2013 Humberto Fraga <xisberto@gmail.com>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Humberto Fraga <xisberto@gmail.com> - initial API and implementation
 ******************************************************************************/
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
	/**
	 * Allows the app to update this widget at an arbitrary time,
	 * sending a broadcast intent with this action
	 */
	public static final String MY_ACTION_UPDATE = "net.xisberto.work_schedule.update_widgets";
	
	public WidgetNextProvider() {
	}
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(MY_ACTION_UPDATE)) {
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
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.image_icon, pendingIntent);

		Settings settings = new Settings(context.getApplicationContext());
		Bundle info = settings.getNextAlarm();
		
		// Set an intent to open MainActivity setting a period
		Intent intentAction = new Intent(context, MainActivity.class);
		intentAction.setAction(MainActivity.ACTION_SET_PERIOD);
		intentAction.putExtra(MainActivity.EXTRA_PREF_ID, info.getInt(Settings.EXTRA_PREF_ID));
		PendingIntent pendingIntentAction = PendingIntent.getActivity(context, 0, intentAction, 0);
		views.setOnClickPendingIntent(R.id.text_label, pendingIntentAction);

		String period_label = info.getString(Settings.EXTRA_PERIOD_LABEL);
		String time = info.getString(Settings.EXTRA_PERIOD_TIME);
		if (! time.equals("")) {
				period_label += "\n" + time;
		}
		Log.d(getClass().getCanonicalName(), "Title: " + period_label
				+ "; Time: " + time);
		views.setTextViewText(R.id.text_label, period_label);

		appWidgetManager.updateAppWidget(appWidgetIds, views);
	}

}
