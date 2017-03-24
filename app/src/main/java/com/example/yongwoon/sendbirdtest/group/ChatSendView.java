package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.UserMessage;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by YongWoon on 2017-03-20.
 */
@EViewGroup(R.layout.chat_item_send)
public class ChatSendView extends RelativeLayout {

    Context context;

    @ViewById
    TextView textMessage, textTime;

    public ChatSendView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(UserMessage message, GroupChannel channel, boolean isContinuous, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage) {
//        SendData data = (SendData) chat;
        textMessage.setText(message.getMessage());

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
