package com.example.yongwoon.sendbirdtest.group;


import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.yongwoon.sendbirdtest.R;
import com.sendbird.android.SendBird;
import com.sendbird.android.SendBirdException;
import com.sendbird.android.User;
import com.sendbird.android.UserListQuery;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
@EFragment(R.layout.fragment_select_user)
public class SelectUserFragment extends Fragment {

    @Bean
    SelectUserListAdapter mAdapter;

    @ViewById
    RecyclerView recyclerView;

    private UserListQuery mUserListQuery;

    private UsersSelectedListener mListener;
    interface UsersSelectedListener {
        void onUserSelected(boolean selected, String userId);
    }


    @AfterViews
    void init() {
        mAdapter.setOnItemCheckedChangeListener(new SelectUserListAdapter.OnItemCheckedChangeListener() {
            @Override
            public void OnItemChecked(User user, boolean checked) {
                mListener.onUserSelected(checked, user.getUserId());
            }
        });

        mListener = (UsersSelectedListener) getActivity();

        setUpRecyclerView();
        loadUserList(15);
        ((CreateGroupChannelActivity) getActivity()).setState(CreateGroupChannelActivity.STATE_SELECT_USERS);
    }


    private void setUpRecyclerView() {
        final LinearLayoutManager manager = new LinearLayoutManager(getContext());
        recyclerView.setAdapter(mAdapter);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (manager.findLastVisibleItemPosition() == mAdapter.getItemCount() - 1) {
                    loadNextUserList(10);
                }
            }
        });
    }


    private void loadUserList(int size) {
        mUserListQuery = SendBird.createUserListQuery();

        mUserListQuery.setLimit(size);
        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    // error
                    return;
                }
                mAdapter.setUserList(list);
            }
        });
    }

    private void loadNextUserList(int size) {
        mUserListQuery.setLimit(size);
        mUserListQuery.next(new UserListQuery.UserListQueryResultHandler() {
            @Override
            public void onResult(List<User> list, SendBirdException e) {
                if (e != null) {
                    return;
                }
                for (User user : list) {
                    mAdapter.addLast(user);
                }
            }
        });
    }




}
