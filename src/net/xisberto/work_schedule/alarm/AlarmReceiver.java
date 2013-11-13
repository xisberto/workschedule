/*******************************************************************************
 * Copyright (c) 2012 Humberto Fraga.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Humberto Fraga - initial API and implementation
 ******************************************************************************/
package net.xisberto.work_schedule.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		int pref_id = extras.getInt(AlarmMessageActivity.EXTRA_PERIOD_ID);
		showAlarm(context, pref_id);
//		Settings.getInstance(context.getApplicationContext()).unsetAlarm(p);
	}

	private void showAlarm(Context context, int pref_id) {
		Intent intentAlarm = new Intent(context, AlarmMessageActivity.class);
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID,
				pref_id);
		context.startActivity(intentAlarm);
	}

}
