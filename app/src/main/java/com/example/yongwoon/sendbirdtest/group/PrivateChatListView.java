package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.UserMessage;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by YongWoon on 2017-03-20.
 */

@EViewGroup(R.layout.list_item_private_channel)
public class PrivateChatListView extends RelativeLayout {

    Context context;

    @ViewById
    ImageView imageProfile;

    @ViewById
    TextView textName, textTime, textNewCount, textContent;



    public PrivateChatListView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(final GroupChannel channel, @Nullable final GroupChannelListAdapter.OnItemClickListener clickListener,
                     @Nullable final GroupChannelListAdapter.OnItemLongClickListener longClickListener) {

        Glide.with(context).load(channel.getCoverUrl())
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, 100, 0))
                .into(imageProfile);
        textName.setText(Utils.getGroupChannelTitle(channel));
        if (channel.getUnreadMessageCount() > 0) {
            textNewCount.setVisibility(VISIBLE);
            textNewCount.setText(String.valueOf(channel.getUnreadMessageCount()));
        } else {
            textNewCount.setVisibility(INVISIBLE);
        }

        BaseMessage lastMessage = channel.getLastMessage();
        if (lastMessage != null) {
            textTime.setText(Utils.convertTimeToString(channel.getLastMessage().getCreatedAt()));

            if (lastMessage instanceof UserMessage) {
                textContent.setText(((UserMessage)lastMessage).getMessage());
            } else if (lastMessage instanceof AdminMessage) {
                textContent.setText(((AdminMessage)lastMessage).getMessage());
            } else {
                String message = String.format("%1$s 님이 파일을 보냈습니다", ((FileMessage)lastMessage).getSender().getNickname());
                textContent.setText(message);
            }
        }

        if (channel.isTyping()) {
            textContent.setText("상대방이 메세지 작성 중입니다");
        }
        if (clickListener != null) {
            setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(channel);
                }
            });
        }

        if (longClickListener != null) {
            setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    longClickListener.onItemLongClick(channel);
                    return true;
                }
            });
        }

    }
}
