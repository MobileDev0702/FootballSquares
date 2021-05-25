package com.moyersoftware.contender.game;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.firebase.auth.FirebaseAuth;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.util.Util;
import com.viewpagerindicator.CirclePageIndicator;

import io.github.inflationx.viewpump.ViewPumpContextWrapper;

public class HowToPlayActivity extends AppCompatActivity {

    // Hey Ryan, you can change colors here
    private final int[] mColors = new int[]{
            Color.parseColor("#252525"),
            Color.parseColor("#252525"),
            Color.parseColor("#252525"),
            Color.parseColor("#252525"),
            Color.parseColor("#252525"),
            Color.parseColor("#252525")
    };
    // ... Images
    public static int[] mImages = new int[]{
            R.drawable.how_to_play_1,
            R.drawable.how_to_play_2,
            R.drawable.how_to_play_3,
            R.drawable.how_to_play_4,
            R.drawable.how_to_play_5,
            R.drawable.how_to_play_6
    };

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapter;
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    private CirclePageIndicator mPageIndicator;
    private View mNextBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_how_to_play);

        bindViews();
        initStatusBar();
        initActionBar();
        initViewPager();
        Util.setTutorialShown(FirebaseAuth.getInstance().getCurrentUser().getUid());
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }
    /**
     * Makes the status bar transparent.
     */
    private void initStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    private void bindViews() {
        mToolbar = findViewById(R.id.toolbar);
        mViewPager = findViewById(R.id.view_pager);
        mPageIndicator = findViewById(R.id.page_indicator);
        mNextBtn = findViewById(R.id.next_btn);
    }

    private void initActionBar() {
        setSupportActionBar(mToolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowHomeEnabled(true);
        }
    }

    private void initViewPager() {
        mAdapter = new MyPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mAdapter);
        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
                if (position < (mAdapter.getCount() - 1) && position < (mColors.length - 1)) {
                    mViewPager.setBackgroundColor((Integer) mArgbEvaluator.evaluate(positionOffset,
                            mColors[position], mColors[position + 1]));
                } else {
                    mViewPager.setBackgroundColor(mColors[mColors.length - 1]);
                }
            }

            @Override
            public void onPageSelected(int position) {
                mNextBtn.setVisibility(position == mColors.length - 1 ? View.GONE : View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        mPageIndicator.setViewPager(mViewPager);
    }

    public void onNextPageClicked(View view) {
        mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1);
    }

    public void onSkipButtonClicked(View view) {
        finish();
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);
        }

        // Returns total number of pages
        @Override
        public int getCount() {
            return mColors.length;
        }

        // Returns the fragment to display for that page
        @Override
        public Fragment getItem(int position) {
            return HowToPlayFragment.newInstance(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        finish();
        return true;
    }
}
