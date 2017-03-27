package com.example.yongwoon.sendbirdtest.group;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.example.yongwoon.sendbirdtest.R;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_members)
public class MembersActivity extends AppCompatActivity {

    @ViewById
    RecyclerView rView;

    @ViewById
    Toolbar toolbar;

    @Extra
    String mChannelUrl;

    @Bean
    UserListAdapter mAdapter;

    GroupChannel mChannel;

    @AfterViews
    void init() {
        if (mChannelUrl == null) {
            finish();
        }

        setSupportActionBar(toolbar);
        setupRecyclerView();
        getChannelFromUrl(mChannelUrl);
    }

    private void setupRecyclerView() {
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rView.setLayoutManager(manager);
        rView.setAdapter(mAdapter);
        rView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
    }

    private void getChannelFromUrl(String url) {
        GroupChannel.getChannel(url, new GroupChannel.GroupChannelGetHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    return;
                }
                mChannel = groupChannel;
                refreshChannel();
            }
        });
    }

    private void refreshChannel() {
        mChannel.refresh(new GroupChannel.GroupChannelRefreshHandler() {
            @Override
            public void onResult(SendBirdException e) {
                if (e != null) {
                    return;
                }
                mAdapter.setUserList(mChannel.getMembers());
            }
        });
    }


}
