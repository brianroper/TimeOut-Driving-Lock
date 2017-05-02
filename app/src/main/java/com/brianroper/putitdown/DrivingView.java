package com.brianroper.putitdown;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static android.content.Context.WINDOW_SERVICE;

/**
 * Created by brianroper on 5/2/17.
 */

public class DrivingView {

    private Context mContext;
    private RelativeLayout mRelativeLayout;

    public DrivingView(Context context) {
        mContext = context;
    }

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
    }

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
}
