package com.xp.legend.lin17.utils;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.xp.legend.lin17.R;

public class ItemSpace extends RecyclerView.ItemDecoration {

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
//        super.getItemOffsets(outRect, view, parent, state);

        int space=view.getContext().getResources().getDimensionPixelSize(R.dimen.item_space);

        outRect.top=space;

    }
}
