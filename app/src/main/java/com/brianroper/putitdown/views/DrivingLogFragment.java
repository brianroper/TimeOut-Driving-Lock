package com.brianroper.putitdown.views;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.DrivingLogEventAdapter;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmResults;

/**
 * Created by brianroper on 9/13/17.
 */

public class DrivingLogFragment extends Fragment {

    @BindView(R.id.log_top_card)
    CardView mLogTripSurface;
    @BindView(R.id.this_week_surface)
    CardView mLogSurface;

    @BindView(R.id.this_week_log)
    RecyclerView mLogRecycler;

    @BindView(R.id.empty_view_this_week)
    RelativeLayout mLogEmptyView;

    private DrivingLogEventAdapter mDLogAdapter;
    private RealmResults<DrivingEventLog> mRealmResults;
    public int mCurrentFragment;

    final int THIS_WEEK_FRAGMENT = 001;
    final int THIS_MONTH_FRAGMENT = 002;
    final int ALL_TIME_FRAGMENT = 003;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_log, container, false);

        ButterKnife.bind(this, root);

        handleUIUtilities();

        initializeAdapter();

        handleEmptyViews(mRealmResults);
        populateAllViews();

        return root;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        mCurrentFragment = getArguments().getInt("currentFragment");
        super.onCreate(savedInstanceState);
    }

    /**
     * returns a new instance of this fragment that can be called in any activity
     * also stores the passed in identifier and stores it in a bundle so we know
     * which fragment we need to create
     */
    public static DrivingLogFragment newInstance(int currentFragment){
        DrivingLogFragment fragment = new DrivingLogFragment();
        Bundle args = new Bundle();
        args.putInt("currentFragment", currentFragment);
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * UI UTILITIES
     */

    /**
     * handles all ui utilities for this activity
     */
    public void handleUIUtilities(){
        handleCardViewBackgroundColors();
    }

    /**
     * handles visibility status for all views
     *
     * note: currently mRealmResults serves as a place holder until TJ finished the logic algorithms
     * different lists of results will be passed in with the appropriate data
     */
    public void handleEmptyViews(RealmResults<DrivingEventLog> results){
        if(results.size() != 0){
            mLogRecycler.setVisibility(View.VISIBLE);
            mLogEmptyView.setVisibility(View.GONE);
        }
        else{
            mLogRecycler.setVisibility(View.GONE);
            mLogEmptyView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * handles cardview background colors
     */
    public void handleCardViewBackgroundColors(){
        mLogTripSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
        mLogSurface.setCardBackgroundColor(getResources().getColor(R.color.white));
    }

    /**
     * END UI UTILITIES
     */

    /**
     * EXTERNAL ACTIVITY COMPONENTS
     */

    /**
     * initializes the driving log event adapter depending on the current passed in fragment key
     *
     * note: currently all fragments return the full list as a placeholder until sorting logic
     * is completed. The commented methods will return the sorted lists
     */
    public void initializeAdapter(){
        mDLogAdapter = new DrivingLogEventAdapter(getActivity());
        if(mCurrentFragment == THIS_WEEK_FRAGMENT){
            //mRealmResults= mDLogAdapter.returnThisWeekDrivingEventLogs();
            mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
        }
        else if(mCurrentFragment == THIS_MONTH_FRAGMENT){
            //mRealmResults = mDLogAdapter.returnThisMonthDrivingEventLogs();
            mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
        }
        else if(mCurrentFragment == ALL_TIME_FRAGMENT){
            //mRealmResults = mDLogAdapter.returnAllTimeDrivingEventLogs();
            mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
        }
    }

    /**
     * END EXTERNAL ACTIVITY COMPONENTS
     */

    /**
     * VIEW UPDATING
     */

    /**
     * populates all views for this activity
     */
    public void populateAllViews(){
        populateLogRecycler();
    }

    /**
     * populates this weeks recycler data
     */
    public void populateLogRecycler(){
        mLogRecycler.setAdapter(mDLogAdapter);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        mLogRecycler.setLayoutManager(layoutManager);
        mDLogAdapter.returnThisWeekDrivingEventLogs();
        mDLogAdapter.notifyDataSetChanged();
    }

    /**
     * END VIEW UPDATING
     */
}
