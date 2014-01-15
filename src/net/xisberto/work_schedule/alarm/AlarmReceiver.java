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
	}

	private void showAlarm(Context context, int pref_id) {
		Intent intentAlarm = new Intent(context, AlarmMessageActivity.class);
		intentAlarm.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intentAlarm.putExtra(AlarmMessageActivity.EXTRA_PERIOD_ID,
				pref_id);
		context.startActivity(intentAlarm);
	}

}
