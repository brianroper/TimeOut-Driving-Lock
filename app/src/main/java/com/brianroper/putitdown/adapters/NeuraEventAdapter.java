package com.brianroper.putitdown.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.model.realmObjects.NeuraEventLog;
import com.brianroper.putitdown.utils.Utils;

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

        if(position == getItemCount() - 1){
            holder.mDivider.setVisibility(View.GONE);
        }

        if(mRealmResults.get(position).getEventName().equals("userStartedDriving")){
            holder.mEventNameTextView.setText("userStartedDriving");
        }
        else if(mRealmResults.get(position).getEventName().equals("userFinishedDriving")){
            holder.mEventNameTextView.setText("userFinishedDriving");
        }
        else if(mRealmResults.get(position).getEventName().equals("userStartedWalking")){
            holder.mEventNameTextView.setText("userStartedWalking");
        }
        else if(mRealmResults.get(position).getEventName().equals("userStartedRunning")){
            holder.mEventNameTextView.setText("userStartedRunning");
        }
        holder.mEventDateTextView.setText(Utils.returnDateStringFromDate(mRealmResults.get(position).getDate()));
        holder.mEventTimeTextView.setText(mRealmResults.get(position).getTime());
    }

    @Override
    public int getItemCount() {
        return mRealmResults.size();
    }

    public class NeuraEventViewHolder extends RecyclerView.ViewHolder{
        @BindView(R.id.event_name)
        TextView mEventNameTextView;
        @BindView(R.id.event_date)
        TextView mEventDateTextView;
        @BindView(R.id.event_time)
        TextView mEventTimeTextView;
        @BindView(R.id.divider)
        RelativeLayout mDivider;

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
}
