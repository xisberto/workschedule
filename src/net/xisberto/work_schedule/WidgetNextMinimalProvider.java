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
import android.util.Log;
import android.widget.RemoteViews;

public class WidgetNextMinimalProvider extends AppWidgetProvider {
	/**
	 * Allows the app to update this widget at an arbitrary time, sending a
	 * broadcast intent with this action
	 */
	public static final String MY_ACTION_UPDATE = "net.xisberto.work_schedule.update_widgets";

	public WidgetNextMinimalProvider() {
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
			RemoteViews views = prepareWidget(context, appWidgetManager,
					appWidgetId, R.layout.widget_next_alarm_minimal);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	protected RemoteViews prepareWidget(Context context,
			AppWidgetManager appWidgetManager, int appWidgetId, int layout_id) {

		if (BuildConfig.DEBUG) {
			Log.d(getClass().getCanonicalName(), "Using layout " + layout_id);
		}

		RemoteViews views = new RemoteViews(context.getPackageName(), layout_id);

		// Set an intent to open MainActivity
		Intent intent = new Intent(context, MainActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
				intent, 0);
		views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
		views.setOnClickPendingIntent(R.id.image_icon, pendingIntent);

		return views;
	}

}
