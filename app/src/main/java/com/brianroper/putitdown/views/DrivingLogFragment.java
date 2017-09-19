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
import android.widget.TextView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.DrivingLogEventAdapter;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;

import java.util.ArrayList;

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

    @BindView(R.id.empty_view)
    RelativeLayout mLogEmptyView;
    @BindView(R.id.empty_view_text)
    TextView mLogEmptyViewTextView;

    @BindView(R.id.log_trip_failed_count)
    TextView mLogTripFailedCountTextView;
    @BindView(R.id.log_trip_success_count)
    TextView mLogTripSuccessCountTextView;

    private DrivingLogEventAdapter mDLogAdapter;
    private RealmResults<DrivingEventLog> mRealmResults;
    public int mCurrentFragment;
    private ArrayList<DrivingEventLog> mResults;

    final int THIS_WEEK_FRAGMENT = 001;
    final int THIS_MONTH_FRAGMENT = 002;
    final int ALL_TIME_FRAGMENT = 003;
    final int TODAY_FRAGMENT = 004;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_log, container, false);

        ButterKnife.bind(this, root);

        handleUIUtilities();

        initializeAdapter();

        handleEmptyViews(mResults, mCurrentFragment);
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
    public void handleEmptyViews(ArrayList<DrivingEventLog> results, int currentFragment){
        if(results.size() != 0){
            mLogRecycler.setVisibility(View.VISIBLE);
            mLogEmptyView.setVisibility(View.GONE);
        }
        else{
            mLogRecycler.setVisibility(View.GONE);
            mLogEmptyView.setVisibility(View.VISIBLE);
            if(currentFragment == THIS_WEEK_FRAGMENT){
                mLogEmptyViewTextView.setText("You have no trip data for this week");
            }
            else if(currentFragment == THIS_MONTH_FRAGMENT){
                mLogEmptyViewTextView.setText("You have no trip data for this month");
            }
            else if(currentFragment == ALL_TIME_FRAGMENT){
                mLogEmptyViewTextView.setText("You currently have no trip data");
            }
            else if(currentFragment == TODAY_FRAGMENT){
                mLogEmptyViewTextView.setText("You currently have no trip data");
            }
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
            mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
            mResults= mDLogAdapter.returnThisWeekDrivingEventLogs();
            mDLogAdapter.notifyDataSetChanged();
        }
        else if(mCurrentFragment == THIS_MONTH_FRAGMENT){
            mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
            mResults = mDLogAdapter.returnThisMonthDrivingEventLogs();
            mDLogAdapter.notifyDataSetChanged();
        }
        else if(mCurrentFragment == ALL_TIME_FRAGMENT){
            mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
            mResults = mDLogAdapter.returnAllTimeDrivingEventLogs();
            mDLogAdapter.notifyDataSetChanged();
        }
        else if(mCurrentFragment == TODAY_FRAGMENT){
            mRealmResults = mDLogAdapter.getDrivingEventLogFromRealm();
            mResults = mDLogAdapter.returnTodayDrivingEventLogs();
            mDLogAdapter.notifyDataSetChanged();
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
        populateTripViews();
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
    }

    /**
     * populates trip text views. Checks to make sure realm results aren't null before
     * incrementing values
     */
    public void populateTripViews(){
        int failedCount = 0;
        int successCount = 0;
        if(mResults.size() != 0){
            for (int i = 0; i < mResults.size(); i++) {
                if(mResults.get(i).isSuccessful()){
                    successCount++;
                }
                else{
                    failedCount++;
                }
            }
        }
        mLogTripFailedCountTextView.setText(failedCount + "");
        mLogTripSuccessCountTextView.setText(successCount + "");
    }

    /**
     * END VIEW UPDATING
     */
}
