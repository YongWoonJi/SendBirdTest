package com.example.yongwoon.sendbirdtest.group;


import android.support.v4.app.Fragment;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.example.yongwoon.sendbirdtest.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_select_distinct)
public class SelectDistinctFragment extends Fragment {

    @ViewById
    CheckBox checkBox;

    private DistinctSelectedListener mListener;
    interface DistinctSelectedListener {
        void onDistinctSelected(boolean distinct);
    }


    @AfterViews
    void init() {
        ((CreateGroupChannelActivity)getActivity()).setState(CreateGroupChannelActivity.STATE_SELECT_DISTINCT);

        mListener = (CreateGroupChannelActivity) getActivity();

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onDistinctSelected(isChecked);
            }
        });
    }

}
