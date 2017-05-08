package com.brianroper.putitdown.views;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.support.v7.view.menu.MenuView;
import android.support.v7.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.brianroper.putitdown.R;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by brianroper on 5/2/17.
 */

public class DrivingView{

    private Context mContext;
    private RelativeLayout mRelativeLayout;
    private ImageButton mOverflowButton;
    private TextView mOverflowTextView;

    public DrivingView(Context context) {
        mContext = context;
    }

    /**
     * draws screen overlay
     */
    public void startDriving(){
        if(mRelativeLayout != null){
            return;
        }

        mRelativeLayout = new RelativeLayout(mContext);

        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.view_driving, mRelativeLayout);

        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_SYSTEM_ERROR,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        } else{
            layoutParams.flags = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        }

        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;

        layoutParams.screenOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

        mRelativeLayout.setSystemUiVisibility(uiOptions);

        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        windowManager.addView(mRelativeLayout, layoutParams);

        initializeViews(mRelativeLayout);
    }

    /**
     * removes the screen overlay
     */
    public void stopDriving(){
        if(mRelativeLayout == null){
            return;
        }

        WindowManager windowManager = (WindowManager) mContext.getSystemService(WINDOW_SERVICE);
        int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
        mRelativeLayout.setSystemUiVisibility(uiOptions);
        windowManager.removeView(mRelativeLayout);
        mRelativeLayout = null;
    }

    /**
     * initialize views for the screen overlay
     */
    public void initializeViews(RelativeLayout root){
        mOverflowButton = (ImageButton) root.findViewById(R.id.overflow_button);
        mOverflowTextView = (TextView) root.findViewById(R.id.overflow_textview);

        mOverflowButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOverflow();
            }
        });

        mOverflowTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopDriving();
            }
        });

        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOverflowTextView.getVisibility() == View.GONE){
                    return;
                }
                if(mOverflowTextView.getVisibility() == View.VISIBLE){
                    hideOverflow();
                }
            }
        });
    }

    /**
     * sets the overflow menu text view to visible
     */
    public void showOverflow(){
        mOverflowTextView.setVisibility(View.VISIBLE);
    }

    /**
     * sets the overflow menu text view to gone
     */
    public void hideOverflow(){
        mOverflowTextView.setVisibility(View.GONE);
    }
}
