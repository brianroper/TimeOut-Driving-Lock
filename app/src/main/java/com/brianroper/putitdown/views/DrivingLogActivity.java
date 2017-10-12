package com.brianroper.putitdown.views;

import android.os.Build;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Window;
import android.view.WindowManager;

import com.brianroper.putitdown.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DrivingLogActivity extends AppCompatActivity {

    @BindView(R.id.log_toolbar)
    Toolbar mLogToolbar;
    @BindView(R.id.log_view_pager)
    ViewPager mLogViewPager;

    final int THIS_WEEK_FRAGMENT = 001;
    final int THIS_MONTH_FRAGMENT = 002;
    final int ALL_TIME_FRAGMENT = 003;
    final int TODAY_FRAGMENT = 004;

    private DrivingLogFragment[] mDrivingLogFragments = {
            DrivingLogFragment.newInstance(TODAY_FRAGMENT),
            DrivingLogFragment.newInstance(THIS_WEEK_FRAGMENT),
            DrivingLogFragment.newInstance(THIS_MONTH_FRAGMENT),
            DrivingLogFragment.newInstance(ALL_TIME_FRAGMENT)
    };

    private String[] mFragmentNames = getResources().getStringArray(R.array.driving_log_frag_names);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ButterKnife.bind(this);

        handleUIUtilities();
    }

    /**
     * UI UTILITIES
     */

    /**
     * handles all ui utilities for this activity
     */
    public void handleUIUtilities(){
        handleToolbarBehavior(mLogToolbar);
        handleViewPagerBehavior(mLogViewPager);
        handleStatusBarColor();
    }

    /**
     * handles toolbar behavior
     */
    public void handleToolbarBehavior(Toolbar toolbar){
        setSupportActionBar(toolbar);
        //getSupportActionBar().setTitle("Trip Logs");
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * sets the color of the status bar
     */
    public void handleStatusBarColor(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(getResources().getColor(R.color.colorPrimaryDark));
        }
    }

    /**
     * handles the swipe behavior for the fragments
     */
    public void handleViewPagerBehavior(ViewPager viewPager) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        viewPager.setAdapter(new LogPagerAdapter(fragmentManager));

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position,
                                       float positionOffset,
                                       int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public class LogPagerAdapter extends FragmentPagerAdapter {

        public LogPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return mDrivingLogFragments[position];
        }

        @Override
        public int getCount() {
            return 4;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentNames[position];
        }
    }

}
