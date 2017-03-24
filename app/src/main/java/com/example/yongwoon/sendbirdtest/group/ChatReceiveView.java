package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.UserMessage;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by YongWoon on 2017-03-20.
 */
@EViewGroup(R.layout.chat_item_receive)
public class ChatReceiveView extends RelativeLayout {

    Context context;

    @ViewById
    ImageView imageProfile;

    @ViewById
    TextView textName, textMessage, textTime;

    public ChatReceiveView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(UserMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous) {

        Glide.with(context).load(message.getSender().getProfileUrl())
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, 200, 0))
                .into(imageProfile);
        textName.setText(message.getSender().getNickname());
        textMessage.setText(message.getMessage());

        if (channel != null) {
            int readReceipt = channel.getReadReceipt(message);
            if (readReceipt > 0) {
                textTime.setText(readReceipt + "\n" + Utils.convertTimeToString(message.getCreatedAt()));
            } else {
                textTime.setText(Utils.convertTimeToString(message.getCreatedAt()));
            }
        }


    }
}
