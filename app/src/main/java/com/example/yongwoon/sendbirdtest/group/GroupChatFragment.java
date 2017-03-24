package com.example.yongwoon.sendbirdtest.group;


import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.yongwoon.sendbirdtest.main.MainActivity;
import com.example.yongwoon.sendbirdtest.PreferenceManager;
import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserMessage;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.InstanceState;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_group_chat)
public class GroupChatFragment extends Fragment implements View.OnClickListener {

    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT";
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_CHAT";

    @InstanceState
    @FragmentArg
    String mChannelUrl;

    @ViewById(R.id.rv_view)
    RecyclerView rView;

    @ViewById
    ImageView fab, fab1, fab2, fab3;

    @ViewById
    RelativeLayout fabBackground;

    @ViewById
    TextView textSend, textTyping;

    @ViewById
    ImageView imageAdd;

    @ViewById
    EditText editText;

    @ViewById
    LinearLayout layoutTyping;

    @Bean
    GroupChatAdapter mChatAdapter;

    private GroupChannel mChannel;
    private boolean mIsTyping;

    boolean isFabOpen = false;
    Animation fab_open,fab_close,rotate_forward,rotate_backward;


    @AfterViews
    void init() {
        setRetainInstance(true);
        mIsTyping = false;
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mIsTyping) {
                    setTypingStatus(true);
                }
                if (s.length() == 0) {
                    setTypingStatus(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        setUpRecyclerView();
        setUpChatListAdapter();
        initFab();
        updateActionBarTitle();
    }

    private void setUpRecyclerView() {
        final LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, true);
        rView.setLayoutManager(manager);
        rView.setAdapter(mChatAdapter);
        rView.addItemDecoration(new ChatItemDecoration());
        rView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (manager.findLastVisibleItemPosition() == mChatAdapter.getItemCount() - 1) {
                    mChatAdapter.loadPreviousMessage(30, null);
                }
            }
        });
    }

    private void setUpChatListAdapter() {
        //..
    }

    void updateActionBarTitle() {
        String title = "";
        if (mChannel != null) {
            title = Utils.getGroupChannelTitle(mChannel);
        }
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setActionBarTitle(title);
        }
    }


    @Override
    public void onResume() {
        super.onResume();

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                if (baseChannel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.markAllMessagesAsRead();
                    mChatAdapter.addFirst(baseMessage);
                }
            }

            @Override
            public void onReadReceiptUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(mChannelUrl)) {
                    mChatAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                if (channel.getUrl().equals(mChannelUrl)) {
                    List<User> typingUsers = channel.getTypingMembers();
                    displayTyping(typingUsers);
                }
            }
        });
        
        SendBird.addConnectionHandler(CONNECTION_HANDLER_ID, new SendBird.ConnectionHandler() {
            @Override
            public void onReconnectStarted() {}

            @Override
            public void onReconnectSucceeded() {
                refresh();
            }

            @Override
            public void onReconnectFailed() {}
        });
        
        if (SendBird.getConnectionState() == SendBird.ConnectionState.OPEN) {
            refresh();
        } else {
            if (SendBird.reconnect()) {
                
            } else {
                String userId = PreferenceManager.getUserId();
                if (userId == null) {
                    Toast.makeText(getContext(), "메신저에 연결하려면 사용자 ID가 필요합니다", Toast.LENGTH_SHORT).show();
                    return;
                }

                SendBird.connect(userId, new SendBird.ConnectHandler() {
                    @Override
                    public void onConnected(User user, SendBirdException e) {
                        if (e != null) {
                            e.printStackTrace();
                            return;
                        }
                        refresh();
                    }
                });
            }
        }
    }

    private void refresh() {
        if (mChannel == null) {
            GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
                @Override
                public void onResult(GroupChannel groupChannel, SendBirdException e) {
                    if (e != null) {
                        // error
                        e.printStackTrace();
                        return;
                    }

                    mChannel = groupChannel;
                    mChatAdapter.setChannel(mChannel);
                    mChatAdapter.loadLatestMessage(30, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            mChatAdapter.markAllMessagesAsRead();
                        }
                    });
                    updateActionBarTitle();
                }
            });
        } else {
            mChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        e.printStackTrace();
                        return;
                    }

                    mChatAdapter.loadLatestMessage(30, new BaseChannel.GetMessagesHandler() {
                        @Override
                        public void onResult(List<BaseMessage> list, SendBirdException e) {
                            mChatAdapter.markAllMessagesAsRead();
                        }
                    });
                    updateActionBarTitle();
                }
            });
        }
    }



    @Override
    public void onPause() {
        setTypingStatus(false);
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        SendBird.removeConnectionHandler(CONNECTION_HANDLER_ID);
        super.onPause();
    }

    private void displayTyping(List<User> typingUsers) {
        if (typingUsers.size() > 0) {
            layoutTyping.setVisibility(View.VISIBLE);
            String text;
            
            if (typingUsers.size() == 1) {
                text = typingUsers.get(0).getNickname() + " 님이 메세지 입력 중입니다";
            } else if (typingUsers.size() == 2) {
                text = typingUsers.get(0).getNickname() + ", " + typingUsers.get(1).getNickname() + " 님이 메세지 입력 중입니다";
            } else {
                text = "3명 이상의 유저들이 메세지 입력 중입니다";
            }
            textTyping.setText(text);
        } else {
            layoutTyping.setVisibility(View.GONE);
        }
    }
    

    @Click
    void textSend() {
        String userInput = editText.getText().toString();
        if (userInput == null || userInput.length() <= 0) {
            return;
        }

        sendUserMessage(userInput);
        editText.setText("");
    }


    private void sendUserMessage(String text) {
        List<String> urls = Utils.extractUrls(text);
        if (urls.size() > 0) {
//            senduserMessageWithUrl(text, urls.get(0));
            return;
        }

        UserMessage tempUserMessage = mChannel.sendUserMessage(text, new BaseChannel.SendUserMessageHandler() {
            @Override
            public void onSent(UserMessage userMessage, SendBirdException e) {
                if (e != null) {
                    // error
                    Toast.makeText(getContext(), "채팅 전송 에러", Toast.LENGTH_SHORT).show();
                    mChatAdapter.markMessageFailed(userMessage.getRequestId());
                    return;
                }

                mChatAdapter.markMessageSent(userMessage);
            }
        });

        mChatAdapter.addFirst(tempUserMessage);
    }

    private void setTypingStatus(boolean typing) {
        if (mChannel == null) {
            return;
        }

        if (typing) {
            mIsTyping = true;
            mChannel.startTyping();
        } else {
            mIsTyping = false;
            mChannel.endTyping();
        }
    }


    @FocusChange
    void editText() {
        if (isFabOpen) {
            animateFAB();
        }
    }

    private void initFab() {
        fabBackground.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getActionMasked() == MotionEvent.ACTION_UP) {
                    animateFAB();
                }
                return true;
            }
        });

        fab_open = AnimationUtils.loadAnimation(getContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getContext(),R.anim.rotate_backward);
        imageAdd.setOnClickListener(this);
        fab1.setOnClickListener(this);
        fab2.setOnClickListener(this);
        fab3.setOnClickListener(this);
    }

    public void animateFAB() {
        if(isFabOpen){
            fabBackground.setVisibility(View.GONE);
            imageAdd.startAnimation(rotate_backward);
            fab1.startAnimation(fab_close);
            fab2.startAnimation(fab_close);
            fab3.startAnimation(fab_close);
            fab1.setClickable(false);
            fab2.setClickable(false);
            fab3.setClickable(false);
            isFabOpen = false;
        } else {
            fabBackground.setVisibility(View.VISIBLE);
            imageAdd.startAnimation(rotate_forward);
            fab1.startAnimation(fab_open);
            fab2.startAnimation(fab_open);
            fab3.startAnimation(fab_open);
            fab1.setClickable(true);
            fab2.setClickable(true);
            fab3.setClickable(true);
            isFabOpen = true;
        }
    }

    @Override
    public void onClick(View view) {
        editText.clearFocus();
        switch (view.getId()){
            case R.id.imageAdd:
                animateFAB();
                break;
            case R.id.fab1:{
                Toast.makeText(getContext(), "파일", Toast.LENGTH_SHORT).show();
                animateFAB();
                break;
            }
            case R.id.fab2:{
                Toast.makeText(getContext(), "카메라", Toast.LENGTH_SHORT).show();
                animateFAB();
                break;
            }
            case R.id.fab3:{
                Toast.makeText(getContext(), "사진", Toast.LENGTH_SHORT).show();
                animateFAB();
                break;
            }
        }
    }



}
