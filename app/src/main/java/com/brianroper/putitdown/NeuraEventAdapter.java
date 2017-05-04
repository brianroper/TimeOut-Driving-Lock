package com.brianroper.putitdown;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        holder.mEventNameTextView.setText(mRealmResults.get(position).getEventName());
        holder.mEventTimeStampTextView.setText(mRealmResults.get(position).getTimestamp() + "");
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
