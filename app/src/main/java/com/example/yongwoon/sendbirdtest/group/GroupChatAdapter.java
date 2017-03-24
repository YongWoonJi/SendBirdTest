package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.yongwoon.sendbirdtest.BaseAdapter;
import com.example.yongwoon.sendbirdtest.ViewWrapper;
import com.sendbird.android.AdminMessage;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongWoon on 2017-03-23.
 */
@EBean
public class GroupChatAdapter extends BaseAdapter {

    public static final String URL_PREVIEW_CUSTOM_TYPE = "url_preview";

    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 20;

    private Context mContext;
    private GroupChannel mChannel;
    private List<BaseMessage> mMessageList;

    private ArrayList<String> mFailedMessageIdList = new ArrayList<>();
    private boolean mIsMessageListLoading;

    public GroupChatAdapter (Context context) {
        mContext = context;
        mMessageList = new ArrayList<>();
    }


    @Override
    public int getItemViewType(int position) {
        BaseMessage message = mMessageList.get(position);

        if (message instanceof UserMessage) {
            UserMessage userMessage = (UserMessage) message;

            if (userMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                return VIEW_TYPE_USER_MESSAGE_ME;
            } else {
                return VIEW_TYPE_USER_MESSAGE_OTHER;
            }

        } else if (message instanceof AdminMessage) {
            return VIEW_TYPE_ADMIN_MESSAGE;
        }

        throw new IllegalArgumentException("invalid type");
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        switch (viewType) {
            case VIEW_TYPE_USER_MESSAGE_ME:
                return ChatSendView_.build(mContext);
            case VIEW_TYPE_USER_MESSAGE_OTHER:
                return ChatReceiveView_.build(mContext);
        }

        throw new IllegalArgumentException("invalid view type");
    }

    @Override
    public void onBindViewHolder(ViewWrapper holder, int position) {
        BaseMessage message = mMessageList.get(position);
        boolean isContinuous = false;
        boolean isNewDay = false;
        boolean isTempMessage = false;
        boolean isFailedMessage = false;

//        if (position < mMessageList.size() - 1) {
//            BaseMessage prevMessage = mMessageList.get(position + 1);
//
//            if (!Utils.hasSameDate(message.getCreatedAt(), prevMessage.getCreatedAt())) {
//                isNewDay = true;
//                isContinuous = false;
//            } else {
//                isContinuous = isContinuous(message, prevMessage);
//            }
//        }

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_USER_MESSAGE_ME: {
                ChatSendView view = (ChatSendView) holder.getView();
                view.bind((UserMessage) message, mChannel, isContinuous, isNewDay, isTempMessage, isFailedMessage);
                return;
            }
            case VIEW_TYPE_USER_MESSAGE_OTHER: {
                ChatReceiveView view = (ChatReceiveView) holder.getView();
                view.bind((UserMessage) message, mChannel, isNewDay, isContinuous);
                return;
            }
            case VIEW_TYPE_ADMIN_MESSAGE: {
                return;
            }
        }

        throw new IllegalArgumentException("invalid position");
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }


    public void markMessageFailed(String requestId) {
        mFailedMessageIdList.add(requestId);
        notifyDataSetChanged();
    }

    public void markMessageSent(BaseMessage message) {
        Object msg = null;

        for (int i = mMessageList.size() - 1; i >= 0; i--) {
            msg = mMessageList.get(i);
            if (message instanceof UserMessage && msg instanceof UserMessage) {
                if (((UserMessage) msg).getRequestId().equals(((UserMessage) message).getRequestId())) {
                    mMessageList.set(i, message);
                    notifyDataSetChanged();
                    return;
                }
            } else if (message instanceof FileMessage && msg instanceof FileMessage) {
                if (((FileMessage) msg).getRequestId().equals(((FileMessage)message).getRequestId())) {
                    //..
                }
            }
        }
    }

    void addFirst(BaseMessage message) {
        mMessageList.add(0, message);
        notifyDataSetChanged();
    }

    void loadPreviousMessage(int limit, final BaseChannel.GetMessagesHandler handler) {
        if (isMessageListLoading()) {
            return;
        }

        long oldestMessageCreateAt = Long.MAX_VALUE;
        if (mMessageList.size() > 0) {
            oldestMessageCreateAt = mMessageList.get(mMessageList.size() - 1).getCreatedAt();
        }

        setMessageListLoading(true);
        mChannel.getPreviousMessagesByTimestamp(oldestMessageCreateAt, false, limit, true, BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (handler != null) {
                    handler.onResult(list, e);
                }

                setMessageListLoading(false);
                if (e != null) {
                    // error
                    e.printStackTrace();
                    return;
                }

                for(BaseMessage message : list) {
                    mMessageList.add(message);
                }

                notifyDataSetChanged();
            }
        });
    }

    private synchronized boolean isMessageListLoading() {
        return mIsMessageListLoading;
    }

    private synchronized void setMessageListLoading(boolean tf) {
        mIsMessageListLoading = tf;
    }


    public void markAllMessagesAsRead() {
        mChannel.markAsRead();
        notifyDataSetChanged();
    }

    public void setChannel(GroupChannel channel) {
        mChannel = channel;
    }

    public void loadLatestMessage(int limit, final BaseChannel.GetMessagesHandler handler) {
        if (isMessageListLoading()) {
            return;
        }

        setMessageListLoading(true);
        mChannel.getPreviousMessagesByTimestamp(Long.MAX_VALUE, true, limit, true, BaseChannel.MessageTypeFilter.ALL, null, new BaseChannel.GetMessagesHandler() {
            @Override
            public void onResult(List<BaseMessage> list, SendBirdException e) {
                if (handler != null) {
                    handler.onResult(list, e);
                }

                setMessageListLoading(false);
                if (e != null) {
                    e.printStackTrace();
                    return;
                }

                if (list.size() <= 0) {
                    return;
                }
                for (BaseMessage message : mMessageList) {
                    if (isTempMessage(message) || isFailedMessage(message)) {
                        list.add(0, message);
                    }
                }

                mMessageList.clear();

                for (BaseMessage message : list) {
                    mMessageList.add(message);
                }

                notifyDataSetChanged();
            }
        });
    }

    public boolean isTempMessage(BaseMessage message) {
        return message.getMessageId() == 0;
    }

    public boolean isFailedMessage(BaseMessage message) {
        if (!isTempMessage(message)) {
            return false;
        }

        if (message instanceof UserMessage) {
            int index = mFailedMessageIdList.indexOf(((UserMessage) message).getRequestId());
            if (index > 0) {
                return true;
            }
        } else if (message instanceof FileMessage) {
            int index = mFailedMessageIdList.indexOf(((FileMessage) message).getRequestId());
            if (index > 0) {
                return true;
            }
        }

        return false;
    }
}
