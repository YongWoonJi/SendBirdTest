package com.example.yongwoon.sendbirdtest.group;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by YongWoon on 2017-03-27 027.
 */
@EViewGroup(R.layout.chat_item_file_send)
public class ChatFileSendView extends RelativeLayout {

    Context context;

    @ViewById
    TextView textContent, textTime;

    public ChatFileSendView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isTempMessage, boolean isFailedMessage, Uri tempFileMessageUri) {
        textContent.setText(message.getName());

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

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setMessage("파일을 다운로드 하시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (which == DialogInterface.BUTTON_POSITIVE) {
                                    Utils.downloadFile(context, message.getUrl(), message.getName());
                                }
                            }
                        })
                        .setNegativeButton("취소", null).show();
            }
        });
    }

}
