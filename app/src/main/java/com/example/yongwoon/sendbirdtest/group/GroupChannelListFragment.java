package com.example.yongwoon.sendbirdtest.group;


import android.content.DialogInterface;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.yongwoon.sendbirdtest.MainActivity;
import com.example.yongwoon.sendbirdtest.R;
import com.sendbird.android.BaseChannel;
import com.sendbird.android.BaseMessage;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.GroupChannelListQuery;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import static android.app.Activity.RESULT_OK;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_group_channel_list)
public class GroupChannelListFragment extends Fragment {

    private static final int INTENT_REQUEST_NEW_GROUP_CHANNEL = 100;
    private static final String CHANNEL_HANDLER_ID = "CHANNEL_HANDLER_GROUP_CHANNEL_LIST";


    @ViewById
    SwipeRefreshLayout swipeRefreshLayout;

    @ViewById
    RecyclerView recyclerView;

    @ViewById
    FloatingActionButton fab;

    @Bean
    GroupChannelListAdapter mChannelListAdapter;

    GroupChannelListQuery mChannelListQuery;

    @AfterViews
    void init() {
        setRetainInstance(true);
        if (getActivity() != null) {
            ((MainActivity) getActivity()).setActionBarTitle("그룹 채팅");
        }
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                refreshChannelList(15);
            }
        });

        setUpRecyclerView();
        setUpChannelListAdapter();
    }

    @Override
    public void onStart() {
        super.onStart();
        mChannelListAdapter.load();
    }

    @Override
    public void onStop() {
        super.onStop();
        mChannelListAdapter.save();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshChannelList(15);

        SendBird.addChannelHandler(CHANNEL_HANDLER_ID, new SendBird.ChannelHandler() {
            @Override
            public void onMessageReceived(BaseChannel baseChannel, BaseMessage baseMessage) {
                mChannelListAdapter.updateOrInsert(baseChannel);
            }

            @Override
            public void onTypingStatusUpdated(GroupChannel channel) {
                mChannelListAdapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    public void onPause() {
        SendBird.removeChannelHandler(CHANNEL_HANDLER_ID);
        super.onPause();
    }

    private void setUpRecyclerView() {
        final LinearLayoutManager manager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setAdapter(mChannelListAdapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (manager.findLastVisibleItemPosition() == mChannelListAdapter.getItemCount() - 1) {
                    loadNextChannelList();
                }
            }
        });
    }

    private void loadNextChannelList() {
        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    return;
                }

                for (GroupChannel channel : list) {
                    mChannelListAdapter.addLast(channel);
                }
            }
        });
    }


    private void setUpChannelListAdapter() {
        mChannelListAdapter.setOnItemClickListener(new GroupChannelListAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupChannel channel) {
                enterGroupChannel(channel);
            }
        });

        mChannelListAdapter.setOnItemLongClickListener(new GroupChannelListAdapter.OnItemLongClickListener() {
            @Override
            public void onItemLongClick(final GroupChannel channel) {
                new AlertDialog.Builder(getContext())
                        .setTitle(channel.getName() + " 채널을 나가시겠습니까?")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                leaveChannel(channel);
                            }
                        })
                        .setNegativeButton("취소", null)
                        .create().show();
            }
        });
    }

    private void enterGroupChannel(GroupChannel channel) {
        String channelUrl = channel.getUrl();
        enterGroupChannel(channelUrl);
    }

    private void enterGroupChannel(String channelUrl) {
        getFragmentManager().beginTransaction()
                .replace(R.id.container, GroupChatFragment_.builder().mChannelUrl(channelUrl).build())
                .addToBackStack(null)
                .commit();
    }

    private void leaveChannel(GroupChannel channel) {
        channel.leave(new GroupChannel.GroupChannelLeaveHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    // error
                    return;
                }
                refreshChannelList(30);
            }
        });
    }



    @Click
    void fab() {
        CreateGroupChannelActivity_.intent(this).startForResult(INTENT_REQUEST_NEW_GROUP_CHANNEL);
    }

    @OnActivityResult(INTENT_REQUEST_NEW_GROUP_CHANNEL)
    void onResult(int resultCode, @OnActivityResult.Extra(value = CreateGroupChannelActivity.EXTRA_NEW_CHANNEL_URL) String newChannelUrl) {
        if (resultCode == RESULT_OK) {
            if (newChannelUrl != null) {
                enterGroupChannel(newChannelUrl);
            }
        } else {
            // error
        }


    }



    private void refreshChannelList(int numChannels) {
        mChannelListQuery = GroupChannel.createMyGroupChannelListQuery();
        mChannelListQuery.setLimit(numChannels);

        mChannelListQuery.next(new GroupChannelListQuery.GroupChannelListQueryResultHandler() {
            @Override
            public void onResult(List<GroupChannel> list, SendBirdException e) {
                if (e != null) {
                    // error
                    return;
                }
                mChannelListAdapter.setGroupChannelList(list);
            }
        });

        if (swipeRefreshLayout.isRefreshing()) {
            swipeRefreshLayout.setRefreshing(false);
        }
    }


}
