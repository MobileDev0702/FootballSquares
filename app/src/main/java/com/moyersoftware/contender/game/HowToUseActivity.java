package com.moyersoftware.contender.game;

import android.animation.ArgbEvaluator;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.interstitial.InterstitialAd;
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.moyersoftware.contender.R;
import com.moyersoftware.contender.util.Util;
import com.viewpagerindicator.CirclePageIndicator;

public class HowToUseActivity extends AppCompatActivity {

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
            R.drawable.how_to_use_1,
            R.drawable.how_to_use_2,
            R.drawable.how_to_use_3,
            R.drawable.how_to_use_4,
            R.drawable.how_to_use_5,
            R.drawable.how_to_use_6
    };

    private static final String AD_UNIT_ID = "ca-app-pub-1761468736156699/5482271983";

    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private MyPagerAdapter mAdapter;
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();
    private CirclePageIndicator mPageIndicator;
    private View mNextBtn;

    private InterstitialAd interstitialAd;

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

    private void showInterstitial() {
        //Toast.makeText(MainActivity.this, "ad 8 ", Toast.LENGTH_SHORT).show();
        // Show the ad if it's ready. Otherwise toast and restart the game.
        if (interstitialAd != null) {
            //Toast.makeText(MainActivity.this, "ad 9 ", Toast.LENGTH_SHORT).show();
            interstitialAd.show(this);
        } else {
            //Toast.makeText(this, "Ad did not load", Toast.LENGTH_SHORT).show();
            //
            //Toast.makeText(MainActivity.this, "ad 10 ", Toast.LENGTH_SHORT).show();
        }
    }

    public void loadAd() {
        //Toast.makeText(MainActivity.this, "ad 1 ", Toast.LENGTH_SHORT).show();
        AdRequest adRequest = new AdRequest.Builder().build();
        InterstitialAd.load(
                this,
                AD_UNIT_ID,
                adRequest,
                new InterstitialAdLoadCallback() {

                    @Override
                    public void onAdLoaded(@NonNull InterstitialAd interstitialAd) {
                        //Toast.makeText(MainActivity.this, "ad 2 ", Toast.LENGTH_SHORT).show();
                        // The mInterstitialAd reference will be null until
                        // an ad is loaded.
                        HowToUseActivity.this.interstitialAd = interstitialAd;
                        //Log.i(TAG, "onAdLoaded");
                        //Toast.makeText(MainActivity.this, "onAdLoaded()", Toast.LENGTH_SHORT).show();
                        interstitialAd.setFullScreenContentCallback(
                                new FullScreenContentCallback() {
                                    @Override
                                    public void onAdDismissedFullScreenContent() {
                                        //Toast.makeText(MainActivity.this, "ad 3 ", Toast.LENGTH_SHORT).show();
                                        // Called when fullscreen content is dismissed.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        HowToUseActivity.this.interstitialAd = null;
                                        //Log.d("TAG", "The ad was dismissed.");
                                        finish();
                                    }

                                    @Override
                                    public void onAdFailedToShowFullScreenContent(AdError adError) {
                                        //Toast.makeText(MainActivity.this, "ad 4 ", Toast.LENGTH_SHORT).show();
                                        // Called when fullscreen content failed to show.
                                        // Make sure to set your reference to null so you don't
                                        // show it a second time.
                                        HowToUseActivity.this.interstitialAd = null;
                                        //Log.d("TAG", "The ad failed to show.");
                                    }

                                    @Override
                                    public void onAdShowedFullScreenContent() {
                                        //Toast.makeText(MainActivity.this, "ad 5 ", Toast.LENGTH_SHORT).show();
                                        // Called when fullscreen content is shown.
                                        //Log.d("TAG", "The ad was shown.");

                                    }
                                });
                    }

                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error
                        //Log.i(TAG, loadAdError.getMessage());
                        interstitialAd = null;
                        //Toast.makeText(MainActivity.this, "ad 6 ", Toast.LENGTH_SHORT).show();

                        String error =
                                String.format(
                                        "domain: %s, code: %d, message: %s",
                                        loadAdError.getDomain(), loadAdError.getCode(), loadAdError.getMessage());
                        //Toast.makeText(
                        //        MainActivity.this, "onAdFailedToLoad() with error: " + error, Toast.LENGTH_SHORT)
                        //        .show();
                    }
                });
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
        showInterstitial();
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
            return HowToUseFragment.newInstance(position);
        }

        // Returns the page title for the top indicator
        @Override
        public CharSequence getPageTitle(int position) {
            return "Page " + position;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        showInterstitial();
        return true;
    }
}
