package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.net.Uri;
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
import java.util.Hashtable;
import java.util.List;

/**
 * Created by YongWoon on 2017-03-23.
 */
@EBean
public class GroupChatAdapter extends BaseAdapter {

    public static final String URL_PREVIEW_CUSTOM_TYPE = "url_preview";

    private static final int VIEW_TYPE_USER_MESSAGE_ME = 10;
    private static final int VIEW_TYPE_USER_MESSAGE_OTHER = 11;
    private static final int VIEW_TYPE_FILE_MESSAGE_ME = 20;
    private static final int VIEW_TYPE_FILE_MESSAGE_OTHER = 21;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_ME = 22;
    private static final int VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER = 23;
    private static final int VIEW_TYPE_FILE_MESSAGE_VIDEO_ME = 24;
    private static final int VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER = 25;
    private static final int VIEW_TYPE_ADMIN_MESSAGE = 30;

    private Context mContext;
    private GroupChannel mChannel;
    private List<BaseMessage> mMessageList;

    private ArrayList<String> mFailedMessageIdList = new ArrayList<>();
    private boolean mIsMessageListLoading;

    private Hashtable<String, Uri> mTempFileMessageUriTable = new Hashtable<>();

    public GroupChatAdapter (Context context) {
        mContext = context;
        mMessageList = new ArrayList<>();
    }

    OnItemClickListener listener;
    interface OnItemClickListener {
        void onFileMessageItemClick(FileMessage message);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
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
        } else if (message instanceof FileMessage) {
            FileMessage fileMessage = (FileMessage) message;
            if (fileMessage.getType().toLowerCase().startsWith("image")) {
                // If the sender is current user
                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER;
                }
            } else if (fileMessage.getType().toLowerCase().startsWith("video")) {
                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER;
                }
            } else {
                if (fileMessage.getSender().getUserId().equals(SendBird.getCurrentUser().getUserId())) {
                    return VIEW_TYPE_FILE_MESSAGE_ME;
                } else {
                    return VIEW_TYPE_FILE_MESSAGE_OTHER;
                }
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
            case VIEW_TYPE_ADMIN_MESSAGE:
            case VIEW_TYPE_FILE_MESSAGE_ME:
                return ChatFileSendView_.build(mContext);
            case VIEW_TYPE_FILE_MESSAGE_OTHER:
                return ChatFileReceiveView_.build(mContext);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME:
                return ChatImageSendView_.build(mContext);
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER:
                return ChatImageReceiveView_.build(mContext);
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME:
                return ChatVideoSendView_.build(mContext);
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER:
                return ChatVideoReceiveView_.build(mContext);
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

        isTempMessage = isTempMessage(message);
        Uri tempFileMessageUri = getTempFileMessageUri(message);
        isFailedMessage = isFailedMessage(message);

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
            case VIEW_TYPE_ADMIN_MESSAGE:
                return;
            case VIEW_TYPE_FILE_MESSAGE_ME: {
                ChatFileSendView view = (ChatFileSendView) holder.getView();
                view.bind((FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri);
                return;
            }
            case VIEW_TYPE_FILE_MESSAGE_OTHER: {
                ChatFileReceiveView view = (ChatFileReceiveView) holder.getView();
                view.bind((FileMessage) message, mChannel, isNewDay, isContinuous);
                return;
            }
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_ME: {
                ChatImageSendView view = (ChatImageSendView) holder.getView();
                view.bind((FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri);
                return;
            }
            case VIEW_TYPE_FILE_MESSAGE_IMAGE_OTHER: {
                ChatImageReceiveView view = (ChatImageReceiveView) holder.getView();
                view.bind((FileMessage) message, mChannel, isNewDay, isContinuous);
                return;
            }
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_ME: {
                ChatVideoSendView view = (ChatVideoSendView) holder.getView();
                view.bind((FileMessage) message, mChannel, isNewDay, isTempMessage, isFailedMessage, tempFileMessageUri, listener);
                return;
            }
            case VIEW_TYPE_FILE_MESSAGE_VIDEO_OTHER: {
                ChatVideoReceiveView view = (ChatVideoReceiveView) holder.getView();
                view.bind((FileMessage) message, mChannel, isNewDay, isContinuous, listener);
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
                if (((FileMessage) msg).getRequestId().equals(((FileMessage) message).getRequestId())) {
                    mTempFileMessageUriTable.remove(((FileMessage) message).getRequestId());
                    mMessageList.set(i, message);
                    notifyDataSetChanged();
                    return;
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

    public void addTempFileMessageInfo(FileMessage message, Uri uri) {
        mTempFileMessageUriTable.put(message.getRequestId(), uri);
    }

    public Uri getTempFileMessageUri(BaseMessage message) {
        if (!isTempMessage(message)) {
            return null;
        }

        if (!(message instanceof FileMessage)) {
            return null;
        }

        return mTempFileMessageUriTable.get(((FileMessage) message).getRequestId());
    }


}
