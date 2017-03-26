package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by JYW on 2017-03-27 027.
 */
@EViewGroup(R.layout.chat_item_image_receive)
public class ChatImageReceiveView extends RelativeLayout {

    Context context;

    @ViewById
    ImageView imageProfile, imageView;

    @ViewById
    TextView textName, textTime;

    public ChatImageReceiveView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous) {

        Glide.with(context).load(message.getSender().getProfileUrl())
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, 200, 0))
                .into(imageProfile);
        textName.setText(message.getSender().getNickname());

        ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

        if (thumbnails.size() > 0) {
            Glide.with(context).load(thumbnails.get(0).getUrl()).into(imageView);
        } else {
            Glide.with(context).load(message.getUrl()).into(imageView);
        }

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
