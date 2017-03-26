package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * Created by JYW on 2017-03-26 026.
 */
@EViewGroup(R.layout.chat_item_image_send)
public class ChatImageSendView extends RelativeLayout {

    Context context;

    @ViewById
    TextView textTime;

    @ViewById
    ImageView imageView;

    public ChatImageSendView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(FileMessage message, GroupChannel channel, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, Uri tempFileMessageUri) {
        if (isTempMessage) {
            Glide.with(context).load(tempFileMessageUri).into(imageView);
        } else {
            ArrayList<FileMessage.Thumbnail> thumbnails = (ArrayList<FileMessage.Thumbnail>) message.getThumbnails();

            if (thumbnails.size() > 0) {
                Glide.with(context).load(thumbnails.get(0).getUrl()).into(imageView);
            } else {
                Glide.with(context).load(message.getUrl()).into(imageView);
            }
        }

        if (isFailedMessage) {
            textTime.setText("전송 실패\n" + Utils.convertTimeToString(message.getCreatedAt()));
        } else if (isTempMessage) {
            textTime.setText("전송 중\n" + Utils.convertTimeToString(message.getCreatedAt()));
        } else {
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
}
