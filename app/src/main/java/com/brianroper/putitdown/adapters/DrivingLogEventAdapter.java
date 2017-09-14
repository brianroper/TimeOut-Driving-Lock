package com.brianroper.putitdown.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.realmObjects.DrivingEventLog;
import com.brianroper.putitdown.utils.Utils;
import com.brianroper.putitdown.views.DashboardActivity;
import com.brianroper.putitdown.views.IntroActivity;

import java.util.ArrayList;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by brianroper on 8/29/17.
 */

public class DrivingLogEventAdapter extends RecyclerView.Adapter<DrivingLogEventAdapter.DrivingLogEventViewHolder> {

    private Context mContext;
    private RealmResults<DrivingEventLog> mRealmResults;
    private ArrayList<DrivingEventLog> mResults = new ArrayList<>();

    public DrivingLogEventAdapter(Context context) {
        mContext = context;
    }

    @Override
    public DrivingLogEventAdapter.DrivingLogEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View root = inflater.inflate(R.layout.driving_log_item, parent, false);
        final DrivingLogEventViewHolder drivingLogEventViewHolder = new DrivingLogEventViewHolder(root);
        return drivingLogEventViewHolder;
    }

    @Override
    public void onBindViewHolder(DrivingLogEventAdapter.DrivingLogEventViewHolder holder, int position) {

        if(position == (getItemCount() - getItemCount())){
            holder.mDivider.setVisibility(View.GONE);
        }

        if(mResults.get(position).isSuccessful() == true){
            holder.mEventNameTextView.setText("you had a safe trip");
        }
        else if(mResults.get(position).isSuccessful() == false){
            holder.mEventNameTextView.setText("you used your device while driving");
        }
        holder.mEventDateTextView.setText(Utils.returnDateStringFromDate(mResults.get(position).getDate()));
        holder.mEventTimeTextView.setText(mResults.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return mResults.size();
    }

    public class DrivingLogEventViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.event_name)
        TextView mEventNameTextView;
        @BindView(R.id.event_date)
        TextView mEventDateTextView;
        @BindView(R.id.event_time)
        TextView mEventTimeTextView;
        @BindView(R.id.divider)
        RelativeLayout mDivider;

        public DrivingLogEventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * returns NeuraEventLog data from realm db
     */
    public RealmResults<DrivingEventLog> getDrivingEventLogFromRealm(){
        Realm realm;
        Realm.init(mContext);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);
        mRealmResults = realm.where(DrivingEventLog.class).findAll();
        return mRealmResults;
    }

    /**
     * returns all time driving logs
     */
    public ArrayList<DrivingEventLog> returnAllTimeDrivingEventLogs(){
        ArrayList<DrivingEventLog> results = new ArrayList<>();

        for (int i = 0; i < mRealmResults.size(); i++) {

            results.add(mRealmResults.get(i));

        }
        mResults = results;
        return results;
    }

    /**
     * returns this week driving logs
     */
    public ArrayList<DrivingEventLog> returnThisWeekDrivingEventLogs(){
        ArrayList<DrivingEventLog> results = new ArrayList<>();
        Date today = Utils.returnDateAsDate();

        for (int i = 0; i < mRealmResults.size(); i++) {

            Date currentDate = mRealmResults.get(i).getDate();

            if(currentDate.getYear() == today.getYear()
                    && currentDate.getMonth() == today.getMonth()
                    && today.getDate() - currentDate.getDate() < 6
                    && today.getDay() - currentDate.getDay() >=0)

                results.add(mRealmResults.get(i));
        }
        mResults = results;
        return results;
    }

    /**
     * returns this month driving logs
     */
    public ArrayList<DrivingEventLog> returnThisMonthDrivingEventLogs(){
        ArrayList<DrivingEventLog> results = new ArrayList<>();
        Date today = Utils.returnDateAsDate();

        for (int i = 0; i < mRealmResults.size() ; i++) {

            Date currentDate = mRealmResults.get(i).getDate();

            if (currentDate.getYear() == today.getYear()
                    && currentDate.getMonth() == today.getMonth())

                results.add(mRealmResults.get(i));
        }
        mResults = results;
        return results;
    }
}
