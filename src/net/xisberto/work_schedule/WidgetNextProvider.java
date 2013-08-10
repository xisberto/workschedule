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

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
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
		for (int appWidgetId : appWidgetIds) {
			prepareWidget(context, appWidgetManager, appWidgetId);
		}
	}

	@Override
	public void onAppWidgetOptionsChanged(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId,
			Bundle newOptions) {
		prepareWidget(context, appWidgetManager, appWidgetId);
	}

	@SuppressLint("NewApi")
	private void prepareWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
		int layout_id = R.layout.widget_next_alarm;
		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
			int max_width = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH);
			if (max_width < 120) {
				layout_id = R.layout.widget_next_alarm_minimal;
			}
		}
				
		if (BuildConfig.DEBUG) {
			Log.d(getClass().getCanonicalName(), "Using layout "+layout_id);
		}
		RemoteViews views = new RemoteViews(context.getPackageName(),
				layout_id);

		// Set an intent to open MainActivity
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
		views.setOnClickPendingIntent(R.id.image_icon, pendingIntent);

		if (layout_id == R.layout.widget_next_alarm) {
			// Set an intent to open MainActivity setting a period
			Intent intentAction = new Intent(MainActivity.ACTION_SET_PERIOD);
			intentAction.setComponent(new ComponentName(context, MainActivity.class));
			intentAction.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			PendingIntent pendingIntentAction = PendingIntent.getActivity(context, 1, intentAction, 0);
			views.setOnClickPendingIntent(R.id.text_label, pendingIntentAction);

			Settings settings = new Settings(context.getApplicationContext());
			Bundle info = settings.getNextAlarm();
			String period_label = info.getString(Settings.EXTRA_PERIOD_LABEL);
			String time = info.getString(Settings.EXTRA_PERIOD_TIME);
			if (! time.equals("")) {
					period_label += "\n" + time;
			}
			views.setTextViewText(R.id.text_label, period_label);
		}

		appWidgetManager.updateAppWidget(appWidgetId, views);
	}

}
