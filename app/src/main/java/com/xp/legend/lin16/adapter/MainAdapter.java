package com.xp.legend.lin16.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import java.util.List;

public class MainAdapter extends FragmentStatePagerAdapter {


    private List<Fragment> fragmentList;

    public MainAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        if (this.fragmentList==null){
            return null;
        }

        return this.fragmentList.get(position);
    }

    @Override
    public int getCount() {


        if (this.fragmentList==null) {
            return 0;
        }

        return fragmentList.size();
    }

    public void setFragmentList(List<Fragment> fragmentList) {
        this.fragmentList = fragmentList;
        notifyDataSetChanged();
    }
}
