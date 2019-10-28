package com.xp.legend.lin17.adapter;

import com.xp.legend.lin17.bean.Result;

public interface OnItemClick {

    void itemClick(int position, Result result);

    void itemLongClick(int position, Result result);
}
