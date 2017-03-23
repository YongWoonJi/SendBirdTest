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
 * Created by YongWoon on 2017-03-23.
 */
@EBean
public class SelectUserListAdapter extends BaseAdapter {

    private List<User> mUsers;
    private Context mContext;
    private static List<String> mSelectedUserIds;

    private OnItemCheckedChangeListener mCheckedChangeListener;


    public interface OnItemCheckedChangeListener {
        void OnItemChecked(User user, boolean checked);
    }

    public void setOnItemCheckedChangeListener(OnItemCheckedChangeListener listener) {
        mCheckedChangeListener = listener;
    }


    public void setUserList(List<User> users) {
        mUsers = users;
        notifyDataSetChanged();
    }

    public SelectUserListAdapter(Context context) {
        mContext = context;
        mUsers = new ArrayList<>();
        mSelectedUserIds = new ArrayList<>();
    }


    @Override
    protected View onCreateItemView(ViewGroup parent, int viewType) {
        return SelectUserView_.build(mContext);
    }

    @Override
    public void onBindViewHolder(ViewWrapper holder, int position) {
        SelectUserView view = (SelectUserView) holder.getView();
        view.setOnAdapterCheckedChangeListener(new SelectUserView.OnAdapterCheckedChangeListener() {
            @Override
            public void OnItemCheckedAdapter(User user, boolean isChecked) {
                if (isChecked) {
                    mSelectedUserIds.add(user.getUserId());
                } else {
                    mSelectedUserIds.remove(user.getUserId());
                }
            }
        });
        view.bind(mUsers.get(position), isSelected(mUsers.get(position)), mCheckedChangeListener);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    boolean isSelected(User user) {
        return mSelectedUserIds.contains(user.getUserId());
    }

    public void addLast(User user) {
        mUsers.add(user);
        notifyDataSetChanged();
    }

}
