package com.example.brerlappin.sefmultitouch;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by BRERLAPPIN on 29/11/2016.
 */

public class BlueFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        return inflater.inflate(R.layout.blue_menu_overlay, container, false);
    }
}
