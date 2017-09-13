package com.brianroper.putitdown.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.RelativeLayout;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.DrivingLogEventAdapter;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

public class DrivingLogActivity extends AppCompatActivity {

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

    @BindView(R.id.empty_view_this_week)
    RelativeLayout mThisWeekEmptyView;
    @BindView(R.id.empty_view_this_month)
    RelativeLayout mThisMonthEmptyView;
    @BindView(R.id.empty_view_all_time)
    RelativeLayout mAllTimeEmptyView;

    @BindView(R.id.log_toolbar)
    Toolbar mLogToolbar;

    private DrivingLogEventAdapter mDLogAdapter;
    private RealmResults<DrivingEventLog> mRealmResults;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log);

        ButterKnife.bind(this);

        handleUIUtilities();

        initializeAdapter();

        handleEmptyViews();
        populateAllViews();
    }

    /**
     * UI UTILITIES
     */

    /**
     * handles all ui ulititles for this activity
     */
    public void handleUIUtilities(){
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
     * handles visibility status for all views
     *
     * note: currently mRealmResults serves as a place holder until TJ finished the logic algorithms
     * different lists of results will be passed in with the appropriate data
     */
    public void handleEmptyViews(){
        handleThisWeekEmptyView(mRealmResults);
        handleThisMonthEmptyView(mRealmResults);
        handleAllTimeEmptyView(mRealmResults);
    }

    /**
     * handles visibility status for this week views
     */
    public void handleThisWeekEmptyView(RealmResults<DrivingEventLog> results){
        if(results.size() != 0){
            mThisWeekRecycler.setVisibility(View.VISIBLE);
            mThisWeekEmptyView.setVisibility(View.GONE);
        }
        else{
            mThisWeekRecycler.setVisibility(View.GONE);
            mThisWeekEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * handles visibility status for this month views
     */
    public void handleThisMonthEmptyView(RealmResults<DrivingEventLog> results){
        if(results.size() != 0){
            mThisMonthRecycler.setVisibility(View.VISIBLE);
            mThisMonthEmptyView.setVisibility(View.GONE);
        }
        else{
            mThisMonthEmptyView.setVisibility(View.VISIBLE);
            mThisMonthRecycler.setVisibility(View.GONE);
        }
    }

    /**
     * handles visibility status for all time views
     */
    public void handleAllTimeEmptyView(RealmResults<DrivingEventLog> results){
        if(results.size() != 0){
            mAllTimeRecycler.setVisibility(View.VISIBLE);
            mAllTimeEmptyView.setVisibility(View.GONE);
        }
        else{
            mAllTimeRecycler.setVisibility(View.GONE);
            mAllTimeEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * handles cardview background colors
     */
    public void handleCardViewBackgroundColors(){
        mAllTimeSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
        mThisWeekSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
        mThisMonthSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
    }

    /**
     * END UI UTILITIES
     */

    /**
     * EXTERNAL ACTIVITY COMPONENTS
     */

    /**
     * initializes the driving log event adapter
     */
    public void initializeAdapter(){
        mDLogAdapter = new DrivingLogEventAdapter(getApplicationContext());
        mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
    }

    /**
     * END EXTERNAL ACTIVITY COMPONENETS
     */

    /**
     * VIEW UPDATING
     */

    /**
     * populates all views for this activity
     */
    public void populateAllViews(){
        populateThisWeekRecycler();
        populateAllTimeRecycler();
        populateThisMonthRecycler();
    }

    /**
     * populates this weeks recycler data
     */
    public void populateThisWeekRecycler(){
        mThisWeekRecycler.setAdapter(mDLogAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mThisWeekRecycler.setLayoutManager(layoutManager);
        mDLogAdapter.returnThisWeekDrivingEventLogs();
        mDLogAdapter.notifyDataSetChanged();
    }

    /**
     * populates this months recycler data
     */
    public void populateThisMonthRecycler(){
        mThisMonthRecycler.setAdapter(mDLogAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mThisMonthRecycler.setLayoutManager(layoutManager);
        mDLogAdapter.returnThisMonthDrivingEventLogs();
        mDLogAdapter.notifyDataSetChanged();
    }

    /**
     * populates all time recycler data
     */
    public void populateAllTimeRecycler(){
        mAllTimeRecycler.setAdapter(mDLogAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mAllTimeRecycler.setLayoutManager(layoutManager);
        mDLogAdapter.returnAllTimeDrivingEventLogs();
        mDLogAdapter.notifyDataSetChanged();
    }

    /**
     * END VIEW UPDATING
     */
}
