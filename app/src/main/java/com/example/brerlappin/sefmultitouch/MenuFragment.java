package com.example.brerlappin.sefmultitouch;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by BRERLAPPIN on 30/11/2016.
 */

public class MenuFragment extends Fragment {
    //LinearLayout optionsLayout;
    Button localButton, blueButton, closeButton;
    OnMenuOptionListener activityCallback;

    public interface OnMenuOptionListener{
        void onMenuButtonPressed(int button);
    }

    @Override
    public void onAttach(Activity activity){
        super.onAttach(activity);

        try {
            activityCallback = (OnMenuOptionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
            " must implement OnMenuOptionListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.options_menu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);
        initOptionsMenu();
    }

    private void initOptionsMenu(){
        //optionsLayout = (LinearLayout) getActivity().findViewById(R.id.options_layout);
        localButton = (Button) getActivity().findViewById(R.id.local_button);
        blueButton = (Button) getActivity().findViewById(R.id.blue_button);
        closeButton = (Button) getActivity().findViewById(R.id.close_button);

        localButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onMenuButtonPressed(1);
            }
        });
        blueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onMenuButtonPressed(2);
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activityCallback.onMenuButtonPressed(3);
            }
        });
    }
}
