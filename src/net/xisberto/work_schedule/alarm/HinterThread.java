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

import android.util.Log;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

public class HinterThread extends Thread {

    private static String TAG = "HinterThread";
    private View hinter_top, hinter_bottom;

    public HinterThread(View hinter_top, View hinter_bottom) {
        this.hinter_top = hinter_top;
        this.hinter_bottom = hinter_bottom;
    }

    @Override
    public void run() {
        while (!this.isInterrupted()) {
            try {
                Runnable run_hinter_top = new Runnable() {
                    @Override
                    public void run() {
                        hinter_top.setVisibility(View.VISIBLE);
                        hinter_top.startAnimation(getAnimation(hinter_top, 150));
                    }
                };
                Runnable run_hinter_bottom = new Runnable() {
                    @Override
                    public void run() {
                        hinter_bottom.setVisibility(View.VISIBLE);
                        hinter_bottom.startAnimation(getAnimation(hinter_bottom, -150));
                    }
                };
                Thread.sleep(1100);
                hinter_top.post(run_hinter_top);
                Thread.sleep(200);
                hinter_bottom.post(run_hinter_bottom);
            } catch (InterruptedException e) {
                Log.i(TAG, "stopping thread");
                hinter_top.post(new Runnable() {
                    @Override
                    public void run() {
                        hinter_top.setVisibility(View.GONE);
                        hinter_bottom.setVisibility(View.GONE);
                    }
                });
                Thread.currentThread().interrupt();
            }
        }
        Log.i(TAG, "exited loop");
    }

    private AnimationSet getAnimation(View view, int margin) {
        //RelativeLayout.LayoutParams paramsTop = new RelativeLayout.LayoutParams(view.getLayoutParams());
        //paramsTop.topMargin = margin;
        TranslateAnimation ta_top = new TranslateAnimation(0, 0, 0, margin);
        AlphaAnimation aa_top = new AlphaAnimation(1f, 0f);
        AnimationSet animation = new AnimationSet(true);
        animation.setDuration(400);
        animation.addAnimation(ta_top);
        animation.addAnimation(aa_top);
        return animation;
    }
}
