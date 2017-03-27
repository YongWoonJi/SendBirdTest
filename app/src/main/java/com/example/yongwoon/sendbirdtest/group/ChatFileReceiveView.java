package com.example.yongwoon.sendbirdtest.group;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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

import jp.wasabeef.glide.transformations.RoundedCornersTransformation;

/**
 * Created by YongWoon on 2017-03-27 027.
 */
@EViewGroup(R.layout.chat_item_file_receive)
public class ChatFileReceiveView extends RelativeLayout {

    Context context;

    @ViewById
    ImageView imageProfile;

    @ViewById
    TextView textName, textContent, textTime;

    public ChatFileReceiveView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(final FileMessage message, GroupChannel channel, boolean isNewDay, boolean isContinuous) {
        Glide.with(context).load(message.getSender().getProfileUrl())
                .bitmapTransform(new CenterCrop(context), new RoundedCornersTransformation(context, 200, 0))
                .into(imageProfile);
        textName.setText(message.getSender().getNickname());
        textContent.setText(message.getName());

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
