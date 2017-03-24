package com.example.yongwoon.sendbirdtest.group;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.View;

import com.example.yongwoon.sendbirdtest.main.BaseApplication;

/**
 * Created by YongWoon on 2017-03-20.
 */

public class ChatItemDecoration extends RecyclerView.ItemDecoration {


    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10, BaseApplication.getContext().getResources().getDisplayMetrics());
        outRect.set(0, 0, 0, margin);
    }
}
