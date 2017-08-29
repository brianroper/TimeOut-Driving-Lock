package com.brianroper.putitdown.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.brianroper.putitdown.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LogActivity extends AppCompatActivity {

    @BindView(R.id.all_time_surface)
    CardView mAllTimeSurface;
    @BindView(R.id.this_month_surface)
    CardView mThisMonthSurface;
    @BindView(R.id.this_week_surface)
    CardView mThisWeekSurface;

    @BindView(R.id.all_time_log)
    RecyclerView mAllTimeRecycler;
    @BindView(R.id.this_month_log)
    RecyclerView mThisMonthRecycler;
    @BindView(R.id.this_week_log)
    RecyclerView mThisWeekRecycler;

    @BindView(R.id.log_toolbar)
    Toolbar mLogToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ButterKnife.bind(this);

        handleToolbarBehavior(mLogToolbar);
        handleCardViewBackgroundColors();
    }

    /**
     * handles toolbar behavior
     */
    public void handleToolbarBehavior(Toolbar toolbar){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Trip Logs");
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * handles cardview background colors
     */
    public void handleCardViewBackgroundColors(){
        mAllTimeSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
        mThisWeekSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
        mThisMonthSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
    }
}
