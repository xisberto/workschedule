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
package net.xisberto.work_schedule.settings;

import net.xisberto.work_schedule.R;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.LayoutInflater;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class AboutView extends RelativeLayout {

	public AboutView(Context context) {
		super(context);
		LayoutInflater.from(context).inflate(R.layout.activity_about, this);

		String versionName = "";
		try {
			versionName = getContext().getPackageManager().getPackageInfo(
					getContext().getPackageName(), 0).versionName;
		} catch (NameNotFoundException e) {
			versionName = "";
		}
		((TextView) findViewById(R.id.text_app_version))
				.setText(versionName);

	}

}
