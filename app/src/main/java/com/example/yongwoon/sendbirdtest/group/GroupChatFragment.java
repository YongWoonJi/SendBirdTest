package com.example.yongwoon.sendbirdtest.group;


import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
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

import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.UserMessage;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FocusChange;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_group_chat)
public class GroupChatFragment extends Fragment implements View.OnClickListener {

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

    private GroupChannel mChannel;



    boolean isFabOpen = false;
    Animation fab_open,fab_close,rotate_forward,rotate_backward;


    @AfterViews
    void init() {
        setRetainInstance(true);

        initFab();
        refresh();




    }

    private void refresh() {
        if (mChannel == null) {
            GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
                @Override
                public void onResult(GroupChannel groupChannel, SendBirdException e) {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }

//                    mChannel = groupChannel;
//                    mChatAdapter.setChannel(mChannel);
//                    mChatAdapter.loadLatestMessages(30, new BaseChannel.GetMessagesHandler() {
//                        @Override
//                        public void onResult(List<BaseMessage> list, SendBirdException e) {
//                            mChatAdapter.markAllMessagesAsRead();
//                        }
//                    });
//                    updateActionBarTitle();
                }
            });
        } else {
            mChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
                @Override
                public void onResult(SendBirdException e) {
                    if (e != null) {
                        // Error!
                        e.printStackTrace();
                        return;
                    }

//                    mChatAdapter.loadLatestMessages(30, new BaseChannel.GetMessagesHandler() {
//                        @Override
//                        public void onResult(List<BaseMessage> list, SendBirdException e) {
//                            mChatAdapter.markAllMessagesAsRead();
//                        }
//                    });
//                    updateActionBarTitle();
                }
            });
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
//                    mChatAdapter.markMessageFailed(userMessage.getRequestId());
                    return;
                }

//                mChatAdapter.markMessageSent(userMessage);
            }
        });

//        mChatAdapter.addFirst(tempUserMessage);
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
