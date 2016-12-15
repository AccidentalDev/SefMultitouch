package com.example.brerlappin.sefmultitouch;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/**
 * Created by BRERLAPPIN on 29/11/2016.
 */

public class BlueFragment extends Fragment {
    OnBlueOptionListener activityCallback;

    public interface OnBlueOptionListener{
        void onBlueButtonPressed(int button);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        try {
            activityCallback = (OnBlueOptionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnBlueOptionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.blue_menu_overlay, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        initBlueOptionsMenu();
    }

    private void initBlueOptionsMenu(){
        //blueToothLayout = (LinearLayout) findViewById(R.id.bluetooth_options);
        Button blueDiscButton = (Button) getActivity().findViewById(R.id.discover_button);
        Button bluePairButton = (Button) getActivity().findViewById(R.id.pair_button);
        Button blueReciveButton = (Button) getActivity().findViewById(R.id.recive_button);
        Button blueSendButton = (Button) getActivity().findViewById(R.id.send_button);
        Button blueCloseButton = (Button) getActivity().findViewById(R.id.close_button_blue);
        //blueNotificationText = (TextView) findViewById(R.id.blue_notification);
        //blueNotificationText.setAlpha(0);

        blueDiscButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onBlueButtonPressed(1);
            }
        });
        bluePairButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onBlueButtonPressed(2);
            }
        });
        blueReciveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onBlueButtonPressed(3);
            }
        });
        blueSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onBlueButtonPressed(4);
            }
        });
        blueCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onBlueButtonPressed(5);
            }
        });
    }
}
