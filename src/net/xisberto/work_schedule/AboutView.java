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
