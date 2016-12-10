package com.example.brerlappin.sefmultitouch;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

/**
 * Created by BRERLAPPIN on 09/12/2016.
 */

public class SelectionFragment extends Fragment {
    public Button continueButton;
    public RadioGroup devicesList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.blue_devices_window, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        devicesList = (RadioGroup) getActivity().findViewById(R.id.device_selection);
        continueButton = (Button) getActivity().findViewById(R.id.pair_button);

        continueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Retun the current Radio Button ID
            }
        });
    }

    public void addDevice(String deviceName){
        RadioButton tmpButton = new RadioButton(getActivity().getApplicationContext());
        tmpButton.setText(deviceName);
        devicesList.addView(tmpButton);
    }
}
