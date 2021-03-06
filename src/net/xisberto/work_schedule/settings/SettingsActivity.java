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

import java.util.List;

import net.xisberto.work_schedule.R;
import android.annotation.TargetApi;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.app.NavUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

	@Override
	protected boolean isValidFragment(String fragmentName) {
		if (fragmentName.equals(SettingsFragment.class.getName())
				|| fragmentName.equals(AboutFragment.class.getName())) {
			return true;
		}
		return false;
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
			return new AboutView(getActivity());
		}

	}
}
