package com.brianroper.putitdown.model;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.brianroper.putitdown.R;

/**
 * Created by brianroper on 1/16/17.
 */

public class RecyclerViewDivider extends RecyclerView.ItemDecoration {
    private Drawable mDivider;

    public RecyclerViewDivider(Context context) {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            mDivider = context.getDrawable(R.drawable.recycler_view_divider);
        }
        else{
            mDivider = context.getResources().getDrawable(R.drawable.recycler_view_divider);
        }
    }

    @Override
    public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();
        int childCount = parent.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + mDivider.getIntrinsicHeight();

            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}