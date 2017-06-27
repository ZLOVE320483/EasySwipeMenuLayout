package com.zlove.swipe.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.Scroller;

import com.zlove.swipe.R;

/**
 * Created by zlove on 2017/6/27.
 */

public class EasySwipeMenuLayout extends ViewGroup {

    private int mTouchSlop;
    private Scroller mScroller;

    private int mLeftViewResId;
    private int mRightViewResId;
    private int mContentViewResId;
    private boolean mCanLeftSwipe;
    private boolean mCanRightSwipe;
    private float mFraction;

    public EasySwipeMenuLayout(Context context) {
        this(context, null);
    }

    public EasySwipeMenuLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public EasySwipeMenuLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        mTouchSlop = viewConfiguration.getScaledTouchSlop();
        mScroller = new Scroller(context);

        TypedArray typedArray = context.getTheme().obtainStyledAttributes(attrs, R.styleable.EasySwipeMenuLayout, defStyleAttr, 0);
        try {
            int indexCount = typedArray.getIndexCount();
            for (int i = 0; i < indexCount; i++) {
                int attr = typedArray.getIndex(i);
                if (attr == R.styleable.EasySwipeMenuLayout_leftMenuView) {
                    mLeftViewResId = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_leftMenuView, -1);
                } else if (attr == R.styleable.EasySwipeMenuLayout_rightMenuView) {
                    mRightViewResId = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_rightMenuView, -1);
                } else if (attr == R.styleable.EasySwipeMenuLayout_canLeftSwipe) {
                    mCanLeftSwipe = typedArray.getBoolean(R.styleable.EasySwipeMenuLayout_canLeftSwipe, true);
                } else if (attr == R.styleable.EasySwipeMenuLayout_canRightSwipe) {
                    mCanRightSwipe = typedArray.getBoolean(R.styleable.EasySwipeMenuLayout_canRightSwipe, true);
                } else if (attr == R.styleable.EasySwipeMenuLayout_fraction) {
                    mFraction = typedArray.getFloat(R.styleable.EasySwipeMenuLayout_fraction, 0.5f);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            typedArray.recycle();
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

    }
}
