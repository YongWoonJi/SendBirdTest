package com.example.yongwoon.sendbirdtest;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by YongWoon on 2017-03-23.
 */

public abstract class BaseAdapter extends RecyclerView.Adapter<ViewWrapper> {


    @Override
    public ViewWrapper onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewWrapper(onCreateItemView(parent, viewType));
    }

    protected abstract View onCreateItemView(ViewGroup parent, int viewType);

}
