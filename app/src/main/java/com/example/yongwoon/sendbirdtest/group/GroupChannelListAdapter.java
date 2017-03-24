package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.util.Base64;
import android.view.View;
import android.view.ViewGroup;

import com.example.yongwoon.sendbirdtest.BaseAdapter;
import com.example.yongwoon.sendbirdtest.Utils;
import com.example.yongwoon.sendbirdtest.ViewWrapper;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;

import org.androidannotations.annotations.EBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongWoon on 2017-03-23.
 */
@EBean
public class GroupChannelListAdapter extends BaseAdapter {

    private static final int VIEW_TYPE_GROUP = 0;
    private static final int VIEW_TYPE_PRIVATE = 1;

    List<GroupChannel> mChannelList;
    Context mContext;

    OnItemClickListener itemClickListener;
    OnItemLongClickListener itemLongClickListener;

    interface OnItemClickListener {
        void onItemClick(GroupChannel channel);
    }

    interface  OnItemLongClickListener {
        void onItemLongClick(GroupChannel channel);
    }

    void setOnItemClickListener(OnItemClickListener listener) {
        itemClickListener = listener;
    }

    void setOnItemLongClickListener(OnItemLongClickListener listener) {
        itemLongClickListener = listener;
    }

    GroupChannelListAdapter(Context context) {
        mChannelList = new ArrayList<>();
        mContext = context;
    }

    void setGroupChannelList(List<GroupChannel> list) {
        mChannelList = list;
        notifyDataSetChanged();
    }

    void addLast(GroupChannel channel) {
        mChannelList.add(channel);
        notifyDataSetChanged();
    }


    void updateOrInsert(BaseChannel channel) {
        if (!(channel instanceof GroupChannel)) {
            return;
        }

        GroupChannel groupChannel = (GroupChannel) channel;

        for (int i = 0; i < mChannelList.size(); i++) {
            if (mChannelList.get(i).getUrl().equals(groupChannel.getUrl())) {
                mChannelList.remove(mChannelList.get(i));
                mChannelList.add(0, groupChannel);
                notifyDataSetChanged();
                return;
            }
        }

        mChannelList.add(0, groupChannel);
        notifyDataSetChanged();
    }


    public void load() {
        try {
            File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
            appDir.mkdirs();

            File dataFile = new File(appDir, Utils.generateMD5(SendBird.getCurrentUser().getUserId() + "channel_list") + ".data");

            String content = Utils.loadFromFile(dataFile);
            String [] dataArray = content.split("\n");

            mChannelList.clear();
            for(int i = 0; i < dataArray.length; i++) {
                mChannelList.add((GroupChannel) BaseChannel.buildFromSerializedData(Base64.decode(dataArray[i], Base64.DEFAULT | Base64.NO_WRAP)));
            }

            notifyDataSetChanged();
        } catch(Exception e) {
            // Nothing to load.
        }
    }

    public void save() {
        try {
            StringBuilder sb = new StringBuilder();
            if (mChannelList != null && mChannelList.size() > 0) {
                // Convert current data into string.
                GroupChannel channel = null;
                for (int i = 0; i < Math.min(mChannelList.size(), 100); i++) {
                    channel = mChannelList.get(i);
                    sb.append("\n");
                    sb.append(Base64.encodeToString(channel.serialize(), Base64.DEFAULT | Base64.NO_WRAP));
                }
                // Remove first newline.
                sb.delete(0, 1);

                String data = sb.toString();
                String md5 = Utils.generateMD5(data);

                // Save the data into file.
                File appDir = new File(mContext.getCacheDir(), SendBird.getApplicationId());
                appDir.mkdirs();

                File hashFile = new File(appDir, Utils.generateMD5(SendBird.getCurrentUser().getUserId() + "channel_list") + ".hash");
                File dataFile = new File(appDir, Utils.generateMD5(SendBird.getCurrentUser().getUserId() + "channel_list") + ".data");

                try {
                    String content = Utils.loadFromFile(hashFile);
                    // If data has not been changed, do not save.
                    if(md5.equals(content)) {
                        return;
                    }
                } catch(IOException e) {
                    // File not found. Save the data.
                }

                Utils.saveToFile(dataFile, data);
                Utils.saveToFile(hashFile, md5);
            }
        } catch(Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public int getItemViewType(int position) {
        int member = mChannelList.get(position).getMemberCount();
        if (member > 2) {
            return VIEW_TYPE_GROUP;
        } else {
            return VIEW_TYPE_PRIVATE;
        }
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_GROUP:
                return GroupChatListView_.build(mContext);
            case VIEW_TYPE_PRIVATE:
                return PrivateChatListView_.build(mContext);
        }

        throw new IllegalArgumentException("invalid view type");
    }

    @Override
    public void onBindViewHolder(ViewWrapper holder, int position) {
        int type = holder.getItemViewType();
        switch (type) {
            case VIEW_TYPE_GROUP: {
                GroupChatListView view = (GroupChatListView) holder.getView();
                view.bind(mChannelList.get(position), itemClickListener, itemLongClickListener);
                return;
            }
            case VIEW_TYPE_PRIVATE: {
                PrivateChatListView view = (PrivateChatListView) holder.getView();
                view.bind(mChannelList.get(position), itemClickListener, itemLongClickListener);
                return;
            }
        }

        throw new IllegalArgumentException("invalid position");
    }


    @Override
    public int getItemCount() {
        return mChannelList.size();
    }
}
