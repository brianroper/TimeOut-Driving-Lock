package com.brianroper.putitdown.views;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.brianroper.putitdown.R;
import com.brianroper.putitdown.adapters.NeuraEventAdapter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MomentTestActivity extends AppCompatActivity {

    @BindView(R.id.test_recycler)
    RecyclerView mRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_moment_test);

        ButterKnife.bind(this);

        NeuraEventAdapter adapter = new NeuraEventAdapter(getApplicationContext());
        adapter.getNeuraEventLogDataFromRealm();
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        mRecyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }
}
