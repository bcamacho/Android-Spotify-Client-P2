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
import android.widget.EditText;

import com.brandycamacho.Spotify_Streamer.R;


public class DialogSettingsFragment extends DialogFragment {


    String TAG = this.toString();
    EditText et_country_code;
    Button btn_submit, btn_default_country_code;

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

        final SharedPreferences sp = getActivity().getSharedPreferences("AppDataPref", Context.MODE_PRIVATE);
        String countryCode = sp.getString("countryCode", getResources().getConfiguration().locale.getCountry());
        et_country_code = (EditText) v.findViewById(R.id.et_country_code);
        et_country_code.setText(countryCode);
        btn_submit = (Button) v.findViewById(R.id.btn_submit);
        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("countryCode", et_country_code.getText().toString());
                editor.commit();
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
            Log.e(TAG, "No data within saveInstanceState");
        } else {
            Log.e(TAG, "Theres data!!! - " + savedInstanceState.getBoolean("hasStarted"));
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
        Log.e(TAG, "Has been destroyed");
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