package com.example.brerlappin.sefmultitouch;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Created by BRERLAPPIN on 30/11/2016.
 */

public class DevicesFragment extends Fragment {
    public TextView blueDevicesStatus, blueDevicesTextList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.blue_devices_window, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        blueDevicesTextList = (TextView) getActivity().findViewById(R.id.blue_devices);
        blueDevicesStatus = (TextView) getActivity().findViewById(R.id.statusView);

        Button closeButton = (Button) getActivity().findViewById(R.id.close_button_disc);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getActivity().getFragmentManager().popBackStack();
            }
        });
    }

    public void setStatusText(String txt){
        blueDevicesStatus.setText(txt);
    }
    public void addDeviceToList(String deviceName){
        String tmpTxt = blueDevicesTextList.getText().toString();
        if(tmpTxt.equals(" ")){
            tmpTxt = deviceName;
        }else {
            tmpTxt = tmpTxt + "\n" + deviceName;
        }
        blueDevicesTextList.setText(tmpTxt);
    }
}
