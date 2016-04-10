package cn.kejin.gitbooktest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Author: Kejin ( Liang Ke Jin )
 * Date: 2016/4/10
 */
public class MyFrameLayout extends FrameLayout
{

    private Drawable mInsetForeground;

    private Rect mInsets;

    private Rect mTempRect = new Rect();

    public MyFrameLayout(Context context) {
        this(context, null);
    }

    public MyFrameLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyFrameLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

//        final TypedArray a = context.obtainStyledAttributes(attrs,
//                                                            android.support.design.R.styleable.ScrimInsetsFrameLayout, defStyleAttr,
//                                                            android.support.design.R.style.Widget_Design_ScrimInsetsFrameLayout);
//        mInsetForeground = a.getDrawable(android.support.design.R.styleable.ScrimInsetsFrameLayout_insetForeground);
//        a.recycle();
        mInsetForeground = new ColorDrawable(Color.RED);
        setWillNotDraw(true); // No need to draw until the insets are adjusted

        ViewCompat.setOnApplyWindowInsetsListener(this,
                                                  new android.support.v4.view.OnApplyWindowInsetsListener() {
                                                      @Override
                                                      public WindowInsetsCompat onApplyWindowInsets(View v,
                                                                                                    WindowInsetsCompat insets) {
                                                          if (null == mInsets) {
                                                              mInsets = new Rect();
                                                          }
                                                          mInsets.set(insets.getSystemWindowInsetLeft(),
                                                                      insets.getSystemWindowInsetTop(),
                                                                      insets.getSystemWindowInsetRight(),
                                                                      insets.getSystemWindowInsetBottom());
                                                          onInsetsChanged(mInsets);
                                                          setWillNotDraw(mInsets.isEmpty() || mInsetForeground == null);
                                                          ViewCompat.postInvalidateOnAnimation(MyFrameLayout.this);
                                                          return insets.consumeSystemWindowInsets();
                                                      }
                                                  });
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        super.draw(canvas);

        int width = getWidth();
        int height = getHeight();
        if (mInsets != null && mInsetForeground != null) {
            int sc = canvas.save();
            canvas.translate(getScrollX(), getScrollY());

            // Top
            mTempRect.set(0, 0, width, mInsets.top);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            // Bottom
            mTempRect.set(0, height - mInsets.bottom, width, height);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            // Left
            mTempRect.set(0, mInsets.top, mInsets.left, height - mInsets.bottom);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            // Right
            mTempRect.set(width - mInsets.right, mInsets.top, width, height - mInsets.bottom);
            mInsetForeground.setBounds(mTempRect);
            mInsetForeground.draw(canvas);

            canvas.restoreToCount(sc);
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mInsetForeground != null) {
            mInsetForeground.setCallback(this);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mInsetForeground != null) {
            mInsetForeground.setCallback(null);
        }
    }

    protected void onInsetsChanged(Rect insets) {
    }
}
