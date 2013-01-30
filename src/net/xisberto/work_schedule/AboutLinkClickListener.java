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

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

public class AboutLinkClickListener implements OnClickListener {
	Context context;

	public AboutLinkClickListener(Context context) {
		this.context = context;
	}

	@Override
	public void onClick(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.btn_open_link:
			intent = new Intent(Intent.ACTION_VIEW);
			intent.setData(Uri.parse(context.getString(R.string.github_address)));
			context.startActivity(intent);
			break;
		case R.id.btn_share_link:
			intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT,
					context.getString(R.string.github_address));
			context.startActivity(Intent.createChooser(intent,
					context.getString(R.string.btn_share_link)));
			break;
		default:
			break;
		}
	}

}
