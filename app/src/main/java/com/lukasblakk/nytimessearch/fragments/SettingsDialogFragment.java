package com.lukasblakk.nytimessearch.fragments;

import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lukasblakk.nytimessearch.R;

import java.util.Set;

/**
 * Created by lukas on 3/18/17.
 */

public class SettingsDialogFragment extends DialogFragment {
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container);
    }

    public SettingsDialogFragment(){

    }

    public static SettingsDialogFragment newInstance() {
        SettingsDialogFragment frag = new SettingsDialogFragment();
        return frag;
    }


}
