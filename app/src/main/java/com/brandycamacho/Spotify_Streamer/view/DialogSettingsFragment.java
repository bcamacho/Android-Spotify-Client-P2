package com.brandycamacho.Spotify_Streamer.view;

import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ToggleButton;

import com.brandycamacho.Spotify_Streamer.R;
import com.brandycamacho.Spotify_Streamer.controller.AudioService;
import com.brandycamacho.Spotify_Streamer.controller.MediaNotification;


public class DialogSettingsFragment extends DialogFragment {


    String TAG = this.toString();
    EditText et_country_code;
    Button btn_submit, btn_default_country_code, btn_cancel;
    ToggleButton tb_notifications;
    boolean allowNotifications, test;
    SharedPreferences sp;
    SharedPreferences.Editor editor;

    public static DialogSettingsFragment newInstance() {
        DialogSettingsFragment d = new DialogSettingsFragment();
        d.setRetainInstance(true);
        return d;
    }


    @Override
    public void onSaveInstanceState(Bundle trackPlayer) {
        super.onSaveInstanceState(trackPlayer);
        Log.v(TAG, "Saved Instance State Started");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.dialog_settings, container, false);

        sp = getActivity().getSharedPreferences("AppDataPref", Context.MODE_PRIVATE);
        editor = sp.edit();
        String countryCode = sp.getString("countryCode", getResources().getConfiguration().locale.getCountry());
        allowNotifications = sp.getBoolean("allowNotifications", true);
        Log.v(TAG, "---------->  " + allowNotifications + " <----------");
        et_country_code = (EditText) v.findViewById(R.id.et_country_code);
        et_country_code.setText(countryCode);
        tb_notifications = (ToggleButton) v.findViewById(R.id.tb_notifications);
        tb_notifications.setChecked(allowNotifications);
        tb_notifications.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    allowNotifications = true;
                    if (AudioService.albumArt != null)
                    new MediaNotification(getActivity(), AudioService.artistName, AudioService.trackTitle, AudioService.albumArt);
                } else {
                    allowNotifications = false;
                    MediaNotification.cancel(getActivity());

                }
                editor.putBoolean("allowNotifications", allowNotifications);
                editor.apply();
                Log.v(TAG, "Show notifications are " + allowNotifications);
            }
        });
        btn_cancel = (Button) v.findViewById(R.id.btn_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });
        btn_submit = (Button) v.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putString("countryCode", et_country_code.getText().toString());
                editor.apply();
                getDialog().dismiss();
            }
        });
        btn_default_country_code = (Button) v.findViewById(R.id.btn_default_code);
        btn_default_country_code.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                et_country_code.setText(getResources().getConfiguration().locale.getCountry());
            }
        });


        // Verify saveInstanceState is null otherwise we are returing to activity and must use saveInstanceState to return data to end user
        if (savedInstanceState == null) {
            Log.v(TAG, "No data within saveInstanceState");
        } else {
            Log.v(TAG, "Theres data!!! - " + savedInstanceState.getBoolean("hasStarted"));
        }
        // Verify saveInstanceState is null otherwise we are returing to activity and must use saveInstanceState to return data to end user
        Log.e(TAG, "GET INTENT EXTRAS");
        return v;
    }

    @Override
    public void onCreate(Bundle bundle) {
        this.setCancelable(true);
        setRetainInstance(true);
        super.onCreate(bundle);

    }

    @Override
    public void onDestroyView() {
        Dialog dialog = getDialog();
        // Work around bug: http://code.google.com/p/android/issues/detail?id=17423
        if ((dialog != null) && getRetainInstance())
            dialog.setDismissMessage(null);
        super.onDestroyView();
    }

    @Override
    public void onResume() {
        Log.e(TAG, "Has been resumed");
        getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Has been destroyed");
        super.onDestroy();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        return dialog;
    }
}