package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.yongwoon.sendbirdtest.R;
import com.sendbird.android.User;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by YongWoon on 2017-03-23.
 */
@EViewGroup(R.layout.list_item_selectable_user)
public class SelectUserView extends RelativeLayout {

    Context context;

    @ViewById
    CheckBox checkBox;

    @ViewById
    ImageView imageProfile;

    @ViewById
    TextView textName;

    private OnAdapterCheckedChangeListener mAdapterCheckedChangeListener;


    public SelectUserView(Context context) {
        super(context);
        this.context = context;
    }

    public interface OnAdapterCheckedChangeListener {
        void OnItemCheckedAdapter(User user, boolean isChecked);
    }

    void setOnAdapterCheckedChangeListener (OnAdapterCheckedChangeListener listener) {
        mAdapterCheckedChangeListener = listener;
    }


    public void bind(final User user, boolean isSelected, final SelectUserListAdapter.OnItemCheckedChangeListener listner) {
        textName.setText(user.getNickname());
        Glide.with(context).load(user.getProfileUrl())
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, 100, 0))
                .into(imageProfile);
        checkBox.setSelected(isSelected);
        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                checkBox.setChecked(!checkBox.isChecked());
            }
        });

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                listner.OnItemChecked(user, isChecked);
                mAdapterCheckedChangeListener.OnItemCheckedAdapter(user, isChecked);
            }
        });
    }


}
