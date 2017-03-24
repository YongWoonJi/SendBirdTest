package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.UserMessage;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by YongWoon on 2017-03-20.
 */

@EViewGroup(R.layout.chat_item_date)
public class ChatDateView extends LinearLayout {

    Context context;

    @ViewById
    TextView textDate;

    public ChatDateView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(UserMessage message) {
        textDate.setText(Utils.convertTimeToString3(message.getCreatedAt()));
    }


}
