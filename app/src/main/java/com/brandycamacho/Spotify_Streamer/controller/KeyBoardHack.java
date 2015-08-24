package com.brandycamacho.Spotify_Streamer.controller;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Created by brandycamacho on 8/23/15.
 */
public class KeyBoardHack {

    Context ctx;
    View v;

    public KeyBoardHack(Context ctx, View v) {
        this.ctx = ctx;
        this.v = v;
    }

    /**
     * DialogFragments issues
     * I tried several times to prevent softKeyboard from appearing upon orentation change
     * Efforts included the following:
     * <p/>
     * //to hide keyboard when showing dialog fragment
     * getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
     * <p/>
     * if (v != null) {
     * InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
     * imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
     * }
     * <p/>
     * I created a hack that uses a timerHanfler to repeat request to dismiss softKeyboard
     * which works. However, I still prefer a regular fragment over DialogFragment due to this
     * issue. The use of full screen seems more appealing to me. Yet, during my last code review I was
     * told its a requirment which is why I ended up with this code to fullfill reviewers request.
     */
    int softKeyBaordHackAttemptsCount;
    boolean cancelHack = false;
    Handler timerHandler = new Handler();
    Runnable runSoftKeyBoardHack = new Runnable() {
        @Override
        public void run() {
            if (!cancelHack) {
                Log.v("VIEW", String.valueOf(v));
                Log.v("Context", String.valueOf(ctx));
                Log.v("HACK", "SoftKeyBoard Dismiss Attempt Count = " + softKeyBaordHackAttemptsCount);
                try {
                    if (v != null) {
                        InputMethodManager imm = (InputMethodManager) ctx.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                    if (softKeyBaordHackAttemptsCount < 5) {
                        timerHandler.postDelayed(runSoftKeyBoardHack, 100);
                        softKeyBaordHackAttemptsCount++;
                    }
                } catch (Exception e) {
                    Log.e("HACK", String.valueOf(e));
                }
            }
        }
    };

    public void killKeyboard() {
        timerHandler.postDelayed(runSoftKeyBoardHack, 1);
    }

    public void cancelHack() {
        cancelHack = true;
    }

    public boolean getCancelStatus() {
        return cancelHack;
    }

    public void setTimerLoopCount(int loopCount) {
        softKeyBaordHackAttemptsCount = loopCount;
    }

}
