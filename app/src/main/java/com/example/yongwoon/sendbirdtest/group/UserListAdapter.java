package com.example.yongwoon.sendbirdtest.group;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.example.yongwoon.sendbirdtest.BaseAdapter;
import com.example.yongwoon.sendbirdtest.ViewWrapper;
import com.sendbird.android.User;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by YongWoon on 2017-03-27 027.
 */
@EBean
public class UserListAdapter extends BaseAdapter {

    private Context mContext;
    private List<User> mUsers;

    public void setUserList(List<User> list) {
        mUsers = list;
        notifyDataSetChanged();
    }

    public UserListAdapter (Context context) {
        mContext = context;
        mUsers = new ArrayList<>();
    }

    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return UserListView_.build(mContext);
    }


    @Override
    public void onBindViewHolder(ViewWrapper holder, int position) {
        UserListView view = (UserListView) holder.getView();
        view.bind(mUsers.get(position));
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }




}
