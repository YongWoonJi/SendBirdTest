package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.view.View;
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
 * Created by YongWoon on 2017-03-27 027.
 */
@EViewGroup(R.layout.chat_item_video_receive)
public class ChatVideoReceiveView extends RelativeLayout {

    Context context;

    @ViewById
    ImageView imageProfile, imageThumb;

    @ViewById
    TextView textName, textTime;


    public ChatVideoReceiveView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous, final GroupChatAdapter.OnItemClickListener listener) {

        Glide.with(context).load(message.getSender().getProfileUrl())
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, 200, 0))
                .into(imageProfile);
        textName.setText(message.getSender().getNickname());

        ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();
        if (thumbnails.size() > 0) {
            Glide.with(context).load(thumbnails.get(0).getUrl()).dontAnimate().into(imageThumb);
        }


        if (channel != null) {
            int readReceipt = channel.getReadReceipt(message);
            if (readReceipt > 0) {
                textTime.setText(readReceipt + "\n" + Utils.convertTimeToString(message.getCreatedAt()));
            } else {
                textTime.setText(Utils.convertTimeToString(message.getCreatedAt()));
            }
        }

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onFileMessageItemClick(message);
            }
        });

    }



}
