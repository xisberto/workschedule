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

import java.util.List;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;

public class SettingsActivity extends SherlockPreferenceActivity {

	@SuppressWarnings("deprecation")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		String action = getIntent().getAction();
		if (action != null) {
			if (action.equals(getString(R.string.action_settings_intervals))) {
				addPreferencesFromResource(R.xml.settings_intervals);
			} else if (action.equals(getString(R.string.action_settings_alarm))) {
				addPreferencesFromResource(R.xml.settings_alarm);
			}
		} else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			addPreferencesFromResource(R.xml.settings_legacy);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	@Override
	public void onBuildHeaders(List<Header> target) {
		loadHeadersFromResource(R.xml.settings_headers, target);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class SettingsFragment extends PreferenceFragment {

		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);

			String category = getArguments().getString("category");
			if (category.equals(getResources().getString(
					R.string.pref_category_intervals))) {
				addPreferencesFromResource(R.xml.settings_intervals);
			} else if (category.equals(getResources().getString(
					R.string.pref_category_alarm))) {
				addPreferencesFromResource(R.xml.settings_alarm);
			}
		}

	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	public static class AboutFragment extends Fragment {

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {
			View view = inflater.inflate(R.layout.activity_about, null);

			String versionName = "";
			try {
				versionName = getActivity().getPackageManager().getPackageInfo(
						getActivity().getPackageName(), 0).versionName;
			} catch (NameNotFoundException e) {
				versionName = "";
			}
			((TextView) view.findViewById(R.id.text_app_version))
					.setText(versionName);

			AboutLinkClickListener click_listener = new AboutLinkClickListener(getActivity());
			view.findViewById(R.id.btn_open_link).setOnClickListener(click_listener);
			view.findViewById(R.id.btn_share_link).setOnClickListener(click_listener);
			
			return view;
		}

	}
}
