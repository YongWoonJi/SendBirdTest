package com.example.yongwoon.sendbirdtest.group;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;

import com.example.yongwoon.sendbirdtest.R;
import com.sendbird.android.GroupChannel;
import com.sendbird.android.SendBirdException;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_create_group_channel)
public class CreateGroupChannelActivity extends AppCompatActivity implements SelectUserFragment.UsersSelectedListener, SelectDistinctFragment.DistinctSelectedListener {

    public static final String EXTRA_NEW_CHANNEL_URL = "EXTRA_NEW_CHANNEL_URL";

    static final int STATE_SELECT_USERS = 0;
    static final int STATE_SELECT_DISTINCT = 1;

    private List<String> mSelectedIds;
    private boolean mIsDistinct;

    private int mCurrentState;

    @ViewById
    Toolbar toolbar;

    @ViewById
    Button btnNext, btnCreate;


    @AfterViews
    void init() {
        mSelectedIds = new ArrayList<>();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container_create_channel, new SelectUserFragment_())
                .commit();
        btnNext.setEnabled(false);
        setSupportActionBar(toolbar);
    }


    @Click
    void btnNext() {
        if (mCurrentState == STATE_SELECT_USERS) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container_create_channel, new SelectDistinctFragment_())
                    .addToBackStack(null)
                    .commit();
        }
    }

    @Click
    void btnCreate() {
        if (mCurrentState == STATE_SELECT_DISTINCT) {
            createGroupChannel(mSelectedIds, mIsDistinct);
        }
    }

    private void createGroupChannel(List<String> userIds, boolean distinct) {
        GroupChannel.createChannelWithUserIds(userIds, distinct, new GroupChannel.GroupChannelCreateHandler() {
            @Override
            public void onResult(GroupChannel groupChannel, SendBirdException e) {
                if (e != null) {
                    // error
                    return;
                }
                Intent intent = new Intent();
                intent.putExtra(EXTRA_NEW_CHANNEL_URL, groupChannel.getUrl());
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    void setState(int state) {
        if (state == STATE_SELECT_USERS) {
            mCurrentState = STATE_SELECT_USERS;
            btnCreate.setVisibility(View.GONE);
            btnNext.setVisibility(View.VISIBLE);
        } else if (state == STATE_SELECT_DISTINCT) {
            mCurrentState = STATE_SELECT_DISTINCT;
            btnCreate.setVisibility(View.VISIBLE);
            btnNext.setVisibility(View.GONE);
        }
    }


    @Override
    public void onUserSelected(boolean selected, String userId) {
        if (selected) {
            mSelectedIds.add(userId);
        } else {
            mSelectedIds.remove(userId);
        }

        if (mSelectedIds.size() > 0) {
            btnNext.setEnabled(true);
        } else {
            btnNext.setEnabled(false);
        }
    }

    @Override
    public void onDistinctSelected(boolean distinct) {
        mIsDistinct = distinct;
    }
}
