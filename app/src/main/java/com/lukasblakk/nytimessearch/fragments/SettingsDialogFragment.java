package com.lukasblakk.nytimessearch.fragments;

import android.content.Context;
import android.support.v4.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.codetroopers.betterpickers.datepicker.DatePickerBuilder;
import com.codetroopers.betterpickers.datepicker.DatePickerDialogFragment;
import com.lukasblakk.nytimessearch.R;

import java.util.ArrayList;


/**
 * Created by lukas on 3/18/17.
 */

public class SettingsDialogFragment extends DialogFragment implements DatePickerDialogFragment.DatePickerDialogHandler {


    private TextView mResultTextView;
    private Button mButton;
    private SettingsDialogListener listener;
    private Spinner mSpinner;
    private ArrayList<String> mTopics;


    public interface SettingsDialogListener {
        void onFinishSettingsDialog(String datePicked, String sortOrder, ArrayList<String> topics);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof SettingsDialogListener) {
            listener = (SettingsDialogListener) context;
        } else {
            throw new ClassCastException(context.toString()
                    + " must implement SettingsDialogFragment.SettingsDialogListener");
        }
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    // Defines a listener for every time a checkbox is checked or unchecked
    CompoundButton.OnCheckedChangeListener checkListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton view, boolean checked) {
            // compoundButton is the checkbox
            // boolean is whether or not checkbox is checked
            // Check which checkbox was clicked
            switch(view.getId()) {
                case R.id.cbArts:
                    if (checked) {
                        mTopics.add(getString(R.string.arts));
                    } else {
                        mTopics.remove(getString(R.string.arts));
                    }
                    break;
                case R.id.cbFashion:
                    if (checked) {
                        mTopics.add(getString(R.string.fashion));
                    } else {
                        mTopics.remove(getString(R.string.fashion));
                    }
                    break;
                case R.id.cbSports:
                    if (checked) {
                        mTopics.add(getString(R.string.sports));
                    } else {
                        mTopics.remove(getString(R.string.sports));
                    }
                    break;
            }
        }
    };

    public void setupCheckboxes(View view) {
        CheckBox checkArts = (CheckBox) view.findViewById(R.id.cbArts);
        CheckBox checkSports = (CheckBox) view.findViewById(R.id.cbSports);
        CheckBox checkFashion = (CheckBox) view.findViewById(R.id.cbFashion);

        // Check the current boxes stored
        ArrayList<String> newsDesks = getArguments().getStringArrayList("news_desks");
        if (newsDesks.size() > 0) {
            if (newsDesks.contains(getString(R.string.arts))){
                checkArts.setChecked(true);
            }
            if (newsDesks.contains(getString(R.string.sports))) {
                checkSports.setChecked(true);
            }
            if (newsDesks.contains(getString(R.string.fashion))){
                checkFashion.setChecked(true);
            }

        }

        checkArts.setOnCheckedChangeListener(checkListener);
        checkFashion.setOnCheckedChangeListener(checkListener);
        checkSports.setOnCheckedChangeListener(checkListener);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mResultTextView = (TextView) view.findViewById(R.id.tvDateChosen);
        mButton = (Button) view.findViewById(R.id.btnSave);
        mTopics = getArguments().getStringArrayList("news_desks");
        setupCheckboxes(view);

        mSpinner = (Spinner) view.findViewById(R.id.spinnerSortOrder);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getActivity(), R.array.sort_order_array, R.layout.support_simple_spinner_dropdown_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        mSpinner.setAdapter(adapter);
        // set the spinner selection to what's currently stored
        mSpinner.setSelection(adapter.getPosition(getArguments().getString("sort")));

        // set begin date to what we have currently stored
        mResultTextView.setText(getArguments().getString("begin_date"));

        mResultTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerBuilder dpb = new DatePickerBuilder()
                        .setFragmentManager(getChildFragmentManager())
                        .setStyleResId(R.style.BetterPickersDialogFragment)
                        .setTargetFragment(SettingsDialogFragment.this);
                dpb.show();
            }
        });

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFinishSettingsDialog((String) mResultTextView.getText(), mSpinner.getSelectedItem().toString(), mTopics);
                dismiss();
            }
        });

        return view;
    }

    public SettingsDialogFragment(){
        this.listener = null;
    }

    public static SettingsDialogFragment newInstance() {
        SettingsDialogFragment frag = new SettingsDialogFragment();

        return frag;
    }

    public void setSettingsDialogListener(SettingsDialogListener listener){
        this.listener = listener;
    }


    @Override
    public void onDialogDateSet(int reference, int year, int monthOfYear, int dayOfMonth) {
        mResultTextView.setText(getString(R.string.date_picker_result_value, String.format("%d", year), String.format("%02d", monthOfYear), String.format("%d", dayOfMonth)));
    }
}
