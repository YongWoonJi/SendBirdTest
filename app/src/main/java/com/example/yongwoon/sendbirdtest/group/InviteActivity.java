package com.example.yongwoon.sendbirdtest.group;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.Button;

import com.example.yongwoon.sendbirdtest.R;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_invite)
public class InviteActivity extends AppCompatActivity {

    @ViewById
    Toolbar toolbar;

    @ViewById
    RecyclerView rView;

    @ViewById
    Button btnInvite;

    @Bean
    SelectUserListAdapter mAdapter;

    @Extra
    String mChannelUrl;

    List<String> mSelectedUserIds;
    UserListQuery mUserListQuery;



    @AfterViews
    void init() {
        mSelectedUserIds = new ArrayList<>();

        mAdapter.setOnItemCheckedChangeListener(new SelectUserListAdapter.OnItemCheckedChangeListener() {
            @Override
            public void OnItemChecked(User user, boolean checked) {
                if (checked) {
                    mSelectedUserIds.add(user.getUserId());
                } else {
                    mSelectedUserIds.remove(user.getUserId());
                }

                if (mSelectedUserIds.size() > 0) {
                    btnInvite.setEnabled(true);
                } else {
                    btnInvite.setEnabled(false);
                }
            }
        });

        setSupportActionBar(toolbar);
        btnInvite.setEnabled(false);
        setupRecyclerView();
        loadInitialUserList(15);
    }

    private void setupRecyclerView() {
        final LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rView.setLayoutManager(manager);
        rView.setAdapter(mAdapter);
        rView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        rView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (manager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                    loadNextUserList(10);
                }
            }
        });
    }

    private void loadNextUserList(int limit) {
        mUserListQuery = SendBird.createUserListQuery();
        mUserListQuery.setLimit(limit);
        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    return;
                }
                mAdapter.setUserList(list);
            }
        });
    }

    private void loadInitialUserList(int limit) {
        mUserListQuery = SendBird.createUserListQuery();
        mUserListQuery.setLimit(limit);
        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    return;
                }
                mAdapter.setUserList(list);
            }
        });
    }

    @Click
    void btnInvite() {
        if (mSelectedUserIds.size() > 0) {
            inviteSelectedMembersWithUserIds();
        }
    }

    private void inviteSelectedMembersWithUserIds() {
        GroupChannel.getChannel(mChannelUrl, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    return;
                }

                groupChannel.inviteWithUserIds(mSelectedUserIds, new GroupChannel.GroupChannelInviteHandler() {
                    @Override
                    public void onResult(SendBirdException e) {
                        if (e != null) {
                            return;
                        }
                        finish();
                    }
                });
            }
        });
    }
}
