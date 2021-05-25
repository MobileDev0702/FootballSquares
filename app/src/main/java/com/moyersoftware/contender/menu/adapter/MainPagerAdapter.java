package com.moyersoftware.contender.menu.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.moyersoftware.contender.menu.FriendsFragment;
import com.moyersoftware.contender.menu.GamesFragment;
import com.moyersoftware.contender.menu.NewsFragment;
import com.moyersoftware.contender.menu.SettingsFragment;

public class MainPagerAdapter extends FragmentPagerAdapter {

    public MainPagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);
    }

    /**
     * Returns total number of pages.
     */
    @Override
    //public int getCount() {
    //    return 3;
    //}
    public int getCount() {
        return 3;
    }

    /**
     * Returns the fragment to display for that page.
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return GamesFragment.newInstance();
            case 1:
                return NewsFragment.newInstance();
            case 2:
                return SettingsFragment.newInstance();
            default:
                return null;
        }
    }
}