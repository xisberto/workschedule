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
package net.xisberto.work_schedule;

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
