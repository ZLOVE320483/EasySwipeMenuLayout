package com.zlove.swipe.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Scroller;

import com.zlove.swipe.R;

import java.util.ArrayList;

/**
 * Created by zlove on 2017/6/27.
 */

public class EasySwipeMenuLayout extends ViewGroup {

    private final ArrayList<View> mMatchParentChildren = new ArrayList<>(1);

    private int mTouchSlop;
    private Scroller mScroller;

    private int mLeftViewResId;
    private int mRightViewResId;
    private int mContentViewResId;
    private View mLeftView;
    private View mRightView;
    private View mContentView;
    private MarginLayoutParams mContentViewLp;
    private boolean isSwipeing;
    private PointF mLastP;
    private PointF mFirstP;
    private float mFraction = 0.5f;
    private boolean mCanLeftSwipe = true;
    private boolean mCanRightSwipe = true;

    private static EasySwipeMenuLayout mViewCache;
    private static State mStateCache;

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
                } else if (attr == R.styleable.EasySwipeMenuLayout_contentView) {
                    mContentViewResId = typedArray.getResourceId(R.styleable.EasySwipeMenuLayout_contentView, -1);
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
        int count = getChildCount();
        final boolean measureMatchParentChildren = MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY
                || MeasureSpec.getMode(heightMeasureSpec) != MeasureSpec.EXACTLY;
        mMatchParentChildren.clear();
        int maxHeight = 0;
        int maxWidth = 0;
        int childState = 0;

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec, 0);
                MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                maxWidth = Math.max(maxWidth, child.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
                maxHeight = Math.max(maxHeight, child.getMeasuredHeight() + lp.topMargin + lp.bottomMargin);
                childState = combineMeasuredStates(childState, child.getMeasuredState());
                if (measureMatchParentChildren) {
                    if (lp.width == LayoutParams.MATCH_PARENT || lp.height == LayoutParams.MATCH_PARENT) {
                        mMatchParentChildren.add(child);
                    }
                }
            }
        }

        maxHeight = Math.max(maxHeight, getSuggestedMinimumHeight());
        maxWidth = Math.max(maxWidth, getSuggestedMinimumWidth());
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState), resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));

        count = mMatchParentChildren.size();
        if (count > 1) {
            for (int i = 0; i < count; i++) {
                final  View child = mMatchParentChildren.get(i);
                final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
                final int childWidthMeasureSpec;
                if (lp.width == LayoutParams.MATCH_PARENT) {
                    final int width = Math.max(0, getMeasuredWidth() - lp.leftMargin - lp.rightMargin);
                    childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
                } else {
                    childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec, lp.leftMargin + lp.rightMargin, lp.width);
                }

                final int childHeightMeasureSpec;
                if (lp.height == FrameLayout.LayoutParams.MATCH_PARENT) {
                    final int height = Math.max(0, getMeasuredHeight() - lp.topMargin - lp.bottomMargin);
                    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec( height, MeasureSpec.EXACTLY);
                } else {
                    childHeightMeasureSpec = getChildMeasureSpec(heightMeasureSpec, lp.topMargin + lp.bottomMargin, lp.height);
                }
                child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
            }
        }
    }

    @Override
    public LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new MarginLayoutParams(getContext(), attrs);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int count = getChildCount();
        int left = 0 + getPaddingLeft();
        int top = 0 + getPaddingTop();

        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (mLeftView == null && child.getId() == mLeftViewResId) {
                mLeftView = child;
                mLeftView.setClickable(true);
            } else if (mRightView == null && child.getId() == mRightViewResId) {
                mRightView = child;
                mRightView.setClickable(true);
            } else if (mContentView == null && child.getId() == mContentViewResId) {
                mContentView = child;
                mContentView.setClickable(true);
            }
        }

        int cRight;
        if (mContentView != null) {
            mContentViewLp = (MarginLayoutParams) mContentView.getLayoutParams();
            int cTop = top + mContentViewLp.topMargin;
            int cLeft = left + mContentViewLp.leftMargin;
            cRight = cLeft + mContentView.getMeasuredWidth();
            int cBottom = cTop + mContentView.getMeasuredHeight();
            mContentView.layout(cLeft, cTop, cRight, cBottom);
        }

        if (mLeftView != null) {
            MarginLayoutParams leftViewLp = (MarginLayoutParams) mLeftView.getLayoutParams();
            int lTop = top + leftViewLp.topMargin;
            int lLeft = 0 - mLeftView.getMeasuredWidth() + leftViewLp.leftMargin + leftViewLp.rightMargin;
            int lRight = 0 - leftViewLp.rightMargin;
            int lBottom = lTop + mLeftView.getMeasuredHeight();
            mLeftView.layout(lLeft, lTop, lRight, lBottom);
        }

        if (mRightView != null) {
            MarginLayoutParams rightViewLp = (MarginLayoutParams) mRightView.getLayoutParams();
            int lTop = top + rightViewLp.topMargin;
            int lLeft = mContentView.getRight() + mContentViewLp.rightMargin + rightViewLp.leftMargin;
            int lRight = lLeft + mRightView.getMeasuredWidth();
            int lBottom = lTop + mRightView.getMeasuredHeight();
            mRightView.layout(lLeft, lTop, lRight, lBottom);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                isSwipeing = false;
                if (mLastP == null) {
                    mLastP = new PointF();
                }
                mLastP.set(ev.getRawX(), ev.getRawY());

                if (mFirstP == null) {
                    mFirstP = new PointF();
                }
                mFirstP.set(ev.getRawX(), ev.getRawY());
                if (mViewCache != null) {
                    if (mViewCache != this) {
                        mViewCache.handleSwipeMenu(State.CLOSE);
                    }
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                break;

            case MotionEvent.ACTION_MOVE:
                float distanceX = mLastP.x - ev.getRawX();
                float distanceY = mLastP.y - ev.getRawY();
                if (Math.abs(distanceY) > mTouchSlop * 2) {
                    break;
                }
                if (Math.abs(distanceX) > mTouchSlop * 2 || Math.abs(getScrollX()) > mTouchSlop * 2) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                    scrollBy((int) (distanceX), 0);

                    if (getScrollX() < 0) {
                        if (!mCanRightSwipe || mLeftView == null) {
                            scrollTo(0, 0);
                        } else {
                            if (getScrollX() < mLeftView.getLeft()) {
                                scrollTo(mLeftView.getLeft(), 0);
                            }
                        }
                    }  else if (getScrollX() > 0) {
                        if (!mCanLeftSwipe || mRightView == null) {
                            scrollTo(0, 0);
                        } else {
                            if (getScrollX() > mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin) {
                                scrollTo(mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin, 0);
                            }
                        }
                    }
                }
                mLastP.set(ev.getRawX(), ev.getRawY());
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                distanceX = mLastP.x - ev.getRawX();
                if (Math.abs(distanceX) > mTouchSlop * 2) {
                    isSwipeing = true;
                }
                State result = isShouldOpen(getScrollX());
                handleSwipeMenu(result);
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                break;

            case MotionEvent.ACTION_MOVE:
                //滑动时拦截点击时间
                float distance = mLastP.x - ev.getRawX();
                if (Math.abs(distance) > mTouchSlop) {
                    // 当手指拖动值大于mScaledTouchSlop值时，认为应该进行滚动，拦截子控件的事件
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (isSwipeing) {
                    return true;
                }

        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        }
    }

    /**
     *
     */
    private void handleSwipeMenu(State result) {
        if (result == State.LEFTOPEN) {
            mScroller.startScroll(getScrollX(), 0, mLeftView.getLeft() - getScrollX(), 0);
            mViewCache = this;
            mStateCache = result;
        } else if (result == State.RIGHTOPEN) {
            mViewCache = this;
            mScroller.startScroll(getScrollX(), 0, mRightView.getRight() - mContentView.getRight() - mContentViewLp.rightMargin - getScrollX(), 0);
            mStateCache = result;
        } else {
            mScroller.startScroll(getScrollX(), 0, -getScrollX(), 0);
            mViewCache = null;
            mStateCache = null;
        }
        invalidate();
    }


    /**
     *
     */
    private State isShouldOpen(int scrollX) {
        if (scrollX < 0 && mLeftView != null) {
            //➡滑动
            //获得leftView的测量长度
            if (Math.abs(mLeftView.getWidth() * mFraction) < Math.abs(scrollX)) {
                return State.LEFTOPEN;
            }

        } else if (scrollX > 0 && mRightView != null) {
            //⬅️滑动
            if (Math.abs(mRightView.getWidth() * mFraction) < Math.abs(scrollX)) {
                return State.RIGHTOPEN;
            }
        }
        return State.CLOSE;
    }

    @Override
    protected void onDetachedFromWindow() {
        if (this == mViewCache) {
            mViewCache.handleSwipeMenu(State.CLOSE);
        }
        super.onDetachedFromWindow();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (this == mViewCache) {
            mViewCache.handleSwipeMenu(mStateCache);
        }
    }

    public void resetStatus() {
        if (mViewCache != null) {
            if (mStateCache != null && mStateCache != State.CLOSE && mScroller != null) {
                mScroller.startScroll(mViewCache.getScrollX(), 0, -mViewCache.getScrollX(), 0);
                mViewCache.invalidate();
                mViewCache = null;
                mStateCache = null;
            }
        }
    }


    public float getFraction() {
        return mFraction;
    }

    public void setFraction(float mFraction) {
        this.mFraction = mFraction;
    }

    public boolean isCanLeftSwipe() {
        return mCanLeftSwipe;
    }

    public void setCanLeftSwipe(boolean mCanLeftSwipe) {
        this.mCanLeftSwipe = mCanLeftSwipe;
    }

    public boolean isCanRightSwipe() {
        return mCanRightSwipe;
    }

    public void setCanRightSwipe(boolean mCanRightSwipe) {
        this.mCanRightSwipe = mCanRightSwipe;
    }

    public static EasySwipeMenuLayout getViewCache() {
        return mViewCache;
    }


    public static State getStateCache() {
        return mStateCache;
    }

}
