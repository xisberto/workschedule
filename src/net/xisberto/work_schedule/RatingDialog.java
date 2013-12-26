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

import net.xisberto.work_schedule.settings.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class RatingDialog extends SherlockDialogFragment implements
		OnClickListener {

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
				.setTitle(R.string.app_name).setMessage(R.string.msg_rating)
				.setPositiveButton(R.string.yes, this)
				.setNegativeButton(R.string.no, this)
				.setNeutralButton(R.string.later, this);
		return builder.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Settings settings = Settings.getInstance(getActivity()
				.getApplicationContext());
		switch (which) {
		case Dialog.BUTTON_POSITIVE:
			String appName = getActivity().getPackageName();
			Toast.makeText(getActivity(), appName, Toast.LENGTH_SHORT).show();
			try {
				startActivity(new Intent(Intent.ACTION_VIEW,
						Uri.parse("market://details?id=" + appName)));
			} catch (android.content.ActivityNotFoundException anfe) {
				startActivity(new Intent(
						Intent.ACTION_VIEW,
						Uri.parse("http://play.google.com/store/apps/details?id="
								+ appName)));
			}
		case Dialog.BUTTON_NEGATIVE:
			settings.setAskForRating(false);
			break;
		case Dialog.BUTTON_NEUTRAL:
			settings.setAskForRating(true);
		default:
			break;
		}
	}

}
