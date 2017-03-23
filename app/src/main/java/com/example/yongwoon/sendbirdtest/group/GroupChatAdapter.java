package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.yongwoon.sendbirdtest.BaseAdapter;
import com.example.yongwoon.sendbirdtest.ViewWrapper;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongWoon on 2017-03-23.
 */

public class GroupChatAdapter extends BaseAdapter {

    public static final String URL_PREVIEW_CUSTOM_TYPE = "url_preview";

    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;

    private Context mContext;
    private GroupChannel mChannel;
    private List<BaseMessage> mMessageList;

    private ArrayList<String> mFiledMessageIdList = new ArrayList<>();
    private boolean mIsMessageListLoading;

    public GroupChatAdapter (Context context) {
        mContext = context;
        mMessageList = new ArrayList<>();
    }


    public void load(String channelUrl) {
        File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
        appDir.mkdirs();

//        File dataFile = new File(appDir, Utils.generateMD5())
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewWrapper holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }
}
