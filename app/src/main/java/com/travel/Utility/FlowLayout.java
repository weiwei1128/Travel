package com.travel.Utility;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by wei on 2016/3/13.
 * LinearLayout with auto wrap and Gravity.center
 */
public class FlowLayout extends ViewGroup {

    private int line_height;

    /**
     * total width of need gravity child
     */
    int[] childWidth;

    public static class LayoutParams extends ViewGroup.LayoutParams {
        //0314
        public int gravity = Gravity.CENTER;


        public int horizontal_spacing;
        public int vertical_spacing;

        /**
         * @param horizontal_spacing Pixels between items, horizontally
         * @param vertical_spacing   Pixels between items, vertically
         */
        public LayoutParams(int horizontal_spacing, int vertical_spacing) {
            super(0, 0);
            this.horizontal_spacing = horizontal_spacing;
            this.vertical_spacing = vertical_spacing;

        }

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
        }
    }

    public FlowLayout(Context context) {
        super(context);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        assert (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED);

        // These keep track of the space we are using on the left and right for
        // views positioned there; we need member variables so we can also use
        // these for layout later.
        int lineCount = 0;


        final int width = MeasureSpec.getSize(widthMeasureSpec) - getPaddingLeft() - getPaddingRight();
        int height = MeasureSpec.getSize(heightMeasureSpec) - getPaddingTop() - getPaddingBottom();
        final int count = getChildCount();
        int line_height = 0;
//
        int xpos = getPaddingLeft();
        int ypos = getPaddingTop();

        int childHeightMeasureSpec;
        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.AT_MOST);
        } else {
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
        }

        Boolean wrap = false;
        childWidth = new int[count];
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.AT_MOST), childHeightMeasureSpec);
                final int childw = child.getMeasuredWidth();
                line_height = Math.max(line_height, child.getMeasuredHeight() + lp.vertical_spacing);

                if (xpos + childw > width) {//下一行
                    xpos = getPaddingLeft();
                    ypos += line_height;
                    //for set center!
                    lineCount++;
                }

                xpos += childw + lp.horizontal_spacing;
                //for set center!
                childWidth[lineCount] = xpos;
            }
        }
        this.line_height = line_height;

        if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.UNSPECIFIED) {
            height = ypos + line_height;

        } else if (MeasureSpec.getMode(heightMeasureSpec) == MeasureSpec.AT_MOST) {
            if (ypos + line_height < height) {
                height = ypos + line_height;
            }
        }
        setMeasuredDimension(width, height);
    }

    @Override
    protected ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams(1, 1); // default of 1px spacing
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        if (p instanceof LayoutParams) {
            return true;
        }
        return false;
    }

    @Override
    protected void onLayout(boolean changed, int left, int t, int right, int b) {
        final int count = getChildCount();
        final int width = right - left; //the width of the screen
        int leftPos = getPaddingLeft();
        int rightPos = right - left - getPaddingRight();


        int topPos = getPaddingTop();
        //for set center!
        int lineCount = 0;
        Boolean wraping = false;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() != GONE) {
                final int childw = child.getMeasuredWidth();
                final int childh = child.getMeasuredHeight();
                final LayoutParams lp = (LayoutParams) child.getLayoutParams();
                if (i == 0)
                    wraping = true;
                else wraping = false;
                if (leftPos + childw > width) {
//                    left = getPaddingLeft(); //原本的
                    topPos += line_height;//換下一行的意思！

                    //for set center!
                    lineCount++;
                    leftPos = (width - childWidth[lineCount]) / 2;
                    wraping = true;
                }
                if (wraping)
                    leftPos = (width - childWidth[lineCount]) / 2;

                child.layout(leftPos, topPos, leftPos + childw, topPos + childh);
                leftPos += childw + lp.horizontal_spacing;


            }
        }

    }
}