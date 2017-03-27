package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.yongwoon.sendbirdtest.R;
import com.sendbird.android.User;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by YongWoon on 2017-03-27 027.
 */
@EViewGroup(R.layout.list_item_user)
public class UserListView extends LinearLayout {

    Context context;

    @ViewById
    ImageView imageProfile;

    @ViewById
    TextView textName;


    public UserListView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(User user) {
        Glide.with(context).load(user.getProfileUrl())
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, 200, 0))
                .into(imageProfile);
        textName.setText(user.getNickname());
    }
}
