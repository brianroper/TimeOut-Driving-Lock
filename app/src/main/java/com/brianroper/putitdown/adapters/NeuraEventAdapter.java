package com.brianroper.putitdown.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.NeuraEventLog;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;

/**
 * Created by brianroper on 5/3/17.
 */

public class NeuraEventAdapter extends RecyclerView.Adapter<NeuraEventAdapter.NeuraEventViewHolder> {

    private Context mContext;
    private RealmResults<NeuraEventLog> mRealmResults;

    public NeuraEventAdapter(Context context) {
        mContext = context;
    }

    @Override
    public NeuraEventAdapter.NeuraEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View root = inflater.inflate(R.layout.neura_log_item, parent, false);
        final NeuraEventViewHolder neuraEventViewHolder = new NeuraEventViewHolder(root);
        return neuraEventViewHolder;
    }

    @Override
    public void onBindViewHolder(NeuraEventAdapter.NeuraEventViewHolder holder, int position) {
        if(mRealmResults.get(position).getEventName().equals("userStartedDriving")){
            holder.mEventNameTextView.setText(mContext.getString(R.string.event_started_driving));
        }
        else if(mRealmResults.get(position).getEventName().equals("userFinishedDriving")){
            holder.mEventNameTextView.setText(mContext.getString(R.string.event_finished_driving));
        }
        holder.mEventTimeStampTextView.setText(formatTimeStamp(mRealmResults.get(position).getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return mRealmResults.size();
    }

    public class NeuraEventViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.event_name)
        TextView mEventNameTextView;
        @BindView(R.id.event_timestamp)
        TextView mEventTimeStampTextView;

        public NeuraEventViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

    /**
     * returns NeuraEventLog data from realm db
     */
    public RealmResults<NeuraEventLog> getNeuraEventLogDataFromRealm(){
        Realm realm;
        Realm.init(mContext);
        RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        realm = Realm.getInstance(realmConfiguration);
        mRealmResults = realm.where(NeuraEventLog.class).findAll();
        return mRealmResults;
    }

    /**
     * convert the NeuraEventData timestamp into a date object
     */
    public String formatTimeStamp(long timestamp){
        String time;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        Date date = calendar.getTime();
        time = date.toString();
        Log.i("Time: ", date + "");
        return time;
    }
}
