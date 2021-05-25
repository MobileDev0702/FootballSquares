package com.moyersoftware.contender.game.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.moyersoftware.contender.game.JoinFragment;

public class JoinPagerAdapter extends FragmentPagerAdapter {

    public JoinPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Returns total number of pages.
     */
    @Override
    public int getCount() {
        return 1;
    }

    /**
     * Returns the fragment to display for that page.
     */
    @Override
    public Fragment getItem(int position) {
        return JoinFragment.newInstance();
    }
}