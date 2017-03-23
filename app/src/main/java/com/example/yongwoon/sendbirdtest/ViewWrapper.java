package com.example.yongwoon.sendbirdtest;

import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by YongWoon on 2017-03-23.
 */

public class ViewWrapper extends RecyclerView.ViewHolder {

    private View view;

    public ViewWrapper(View itemView) {
        super(itemView);
        view = itemView;
    }

    public View getView() {
        return view;
    }
}
