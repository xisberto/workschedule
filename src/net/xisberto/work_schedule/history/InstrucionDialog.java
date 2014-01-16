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
package net.xisberto.work_schedule.history;

import net.xisberto.work_schedule.R;
import net.xisberto.work_schedule.settings.Settings;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import com.actionbarsherlock.app.SherlockDialogFragment;

public class InstrucionDialog extends SherlockDialogFragment {
	public interface InstructionCallback {
		public void onInstructionsAccepted();
	}

	private InstructionCallback callback;
	private View view;

	public static InstrucionDialog newInstance(InstructionCallback callback) {
		InstrucionDialog dialog = new InstrucionDialog();
		dialog.callback = callback;
		return dialog;
	}
	
	public void setInstructionCallback(InstructionCallback call) {
		this.callback = call;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		OnClickListener clickCallback = new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				switch (which) {
				case DialogInterface.BUTTON_POSITIVE:
					CheckBox checkBox = (CheckBox) view
							.findViewById(R.id.check_show_instructions);
					if (checkBox.isChecked()) {
						Settings.getInstance(getActivity()).setShowInstructions(false);
					}
					callback.onInstructionsAccepted();
					break;
				default:
					break;
				}
			}
		};

		view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_instructions,
				null);

		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.app_name).setView(view)
				.setPositiveButton(android.R.string.ok, clickCallback)
				.setNegativeButton(android.R.string.cancel, clickCallback);
		return builder.create();
	}
}
