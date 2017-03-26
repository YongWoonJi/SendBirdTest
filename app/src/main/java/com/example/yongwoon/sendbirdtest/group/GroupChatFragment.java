package com.example.yongwoon.sendbirdtest.group;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
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

import com.example.yongwoon.sendbirdtest.PreferenceManager;
import com.example.yongwoon.sendbirdtest.R;
import com.example.yongwoon.sendbirdtest.Utils;
import com.example.yongwoon.sendbirdtest.main.MainActivity;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.FileMessage;
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

import java.io.File;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_group_chat)
public class GroupChatFragment extends Fragment implements View.OnClickListener {

    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_CHAT";
    private static final String CONNECTION_HANDLER_ID = "CONNECTION_HANDLER_GROUP_CHANNEL_CHAT";

    private static final int INTENT_REQUEST_CHOOSE_MEDIA = 301;
    private static final int PERMISSION_WRITE_EXTERNAL_STORAGE = 13;
    private static final int INTENT_REQUEST_CAPTURE_IMAGE = 100;
    private static final int INTENT_REQUEST_GET_IMAGE = 200;

    @InstanceState
    @FragmentArg
    String mChannelUrl;

    @ViewById(R.id.root)
    RelativeLayout rootView;

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

    File savedFile;
    Uri cameraUri;


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
        ((MainActivity)getActivity()).setOnBackPressListener(new MainActivity.OnBackPressListener() {
            @Override
            public boolean onBack() {
                if (isFabOpen) {
                    animateFAB();
                    return true;
                }
                return false;
            }
        });
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
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermissions();
                } else {
                    Intent intent = new Intent();
                    intent.setType("*/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "파일 선택"), INTENT_REQUEST_CHOOSE_MEDIA);
                    SendBird.setAutoBackgroundDetection(false);
                }
                animateFAB();
                break;
            }
            case R.id.fab2:{
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermissions();
                } else {
                    cameraUri = FileProvider.getUriForFile(getContext(), getContext().getPackageName() + ".provider", getSaveFile());
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUri);
                    startActivityForResult(intent, INTENT_REQUEST_CAPTURE_IMAGE);
                    SendBird.setAutoBackgroundDetection(false);
                }
                animateFAB();
                break;
            }
            case R.id.fab3:{
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    requestStoragePermissions();
                } else {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    intent.setType("image/*");
                    startActivityForResult(intent, INTENT_REQUEST_GET_IMAGE);
                    SendBird.setAutoBackgroundDetection(false);
                }
                animateFAB();
                break;
            }
        }
    }



    private void requestStoragePermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            Snackbar.make(rootView, "파일 전송을 위해 권한 허용이 필요합니다", Snackbar.LENGTH_LONG)
                    .setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    PERMISSION_WRITE_EXTERNAL_STORAGE);
                        }
                    })
                    .show();
        } else {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    PERMISSION_WRITE_EXTERNAL_STORAGE);
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == INTENT_REQUEST_CHOOSE_MEDIA && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }

            sendFileWithThumbnail(data.getData());
        } else if (requestCode == INTENT_REQUEST_CAPTURE_IMAGE && resultCode == RESULT_OK) {
            if (cameraUri == null) {
                return;
            }

            sendFileWithThumbnail(cameraUri);
        } else if (requestCode == INTENT_REQUEST_GET_IMAGE && resultCode == RESULT_OK) {
            if (data == null) {
                return;
            }

            sendFileWithThumbnail(data.getData());
        }

        SendBird.setAutoBackgroundDetection(true);
    }

    private void sendFileWithThumbnail(Uri uri) {
        // Specify two dimensions of thumbnails to generate
        List<FileMessage.ThumbnailSize> thumbnailSizes = new ArrayList<>();
        thumbnailSizes.add(new FileMessage.ThumbnailSize(240, 240));
        thumbnailSizes.add(new FileMessage.ThumbnailSize(320, 320));

        Hashtable<String, Object> info = Utils.getFileInfo(getActivity(), uri);

        if (info == null) {
            Toast.makeText(getActivity(), "Extracting file information failed.", Toast.LENGTH_LONG).show();
            return;
        }

        final String path = (String) info.get("path");
        final File file = new File(path);
        final String name = file.getName();
        final String mime = (String) info.get("mime");
        final int size = (Integer) info.get("size");

        if (path.equals("")) {
            Toast.makeText(getActivity(), "File must be located in local storage.", Toast.LENGTH_LONG).show();
        } else {
            // Send image with thumbnails in the specified dimensions
            FileMessage tempFileMessage = mChannel.sendFileMessage(file, name, mime, size, "", null, thumbnailSizes, new BaseChannel.SendFileMessageHandler() {
                @Override
                public void onSent(FileMessage fileMessage, SendBirdException e) {
                    if (e != null) {
                        Toast.makeText(getActivity(), "" + e.getCode() + ":" + e.getMessage(), Toast.LENGTH_SHORT).show();
                        mChatAdapter.markMessageFailed(fileMessage.getRequestId());
                        return;
                    }

                    mChatAdapter.markMessageSent(fileMessage);
                }
            });

            mChatAdapter.addTempFileMessageInfo(tempFileMessage, uri);
            mChatAdapter.addFirst(tempFileMessage);
        }
    }


    private File getSaveFile() {
        File dir = getContext().getExternalFilesDir("capture");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        savedFile = new File(dir, "image_" + System.currentTimeMillis() + ".jpeg");
        return savedFile;
    }




}
