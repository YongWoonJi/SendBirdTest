package com.example.yongwoon.sendbirdtest;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by JYW on 2017-03-18 018.
 */

public class EllipsizeEndTextView extends TextView {
    public final static String NEW_LINE_STR = "\n";

    private int mLineSpacing;

    // the inform listener when measure is done
    private OnMeasureDoneListener mOnMeasureDoneListener;

    public EllipsizeEndTextView(Context context, AttributeSet attrs) {
        super(context, attrs);

        TypedArray typeArray = context.obtainStyledAttributes(attrs, R.styleable.EllipsizeEndTextView);

        mStrEllipsis = "...";
        mMaxLineCount = typeArray.getInteger(R.styleable.EllipsizeEndTextView_maxLines, 5);
        mLineSpacing = typeArray.getDimensionPixelSize(R.styleable.EllipsizeEndTextView_lineSpacing, 0);

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(typeArray.getColor(R.styleable.EllipsizeEndTextView_textColor, Color.BLACK));
        mPaint.setTextSize(typeArray.getDimensionPixelSize(R.styleable.EllipsizeEndTextView_textSize, 0));

        typeArray.recycle();
    }

    // the text ascent by Paint, use to compute the text height
    private int mAscent;

    private int mMaxLineCount;

    // when measure's done, drawLineCount will decide by expand mode
    private int mDrawLineCount;

    // the input string
    private String mText;

    private Paint mPaint;

    private boolean mExpanded = false;

    private String mStrEllipsis;

    // Beginning and end indices for the input string
    private ArrayList<int[]> mLines;

    /**
     * Sets the text to display in this widget.
     *
     * @param text The text to display.
     */
    public void setText(String text) {
        mText = text;
        requestLayout();
        invalidate();
    }

    /**
     * @see View#measure(int, int)
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
        if (mOnMeasureDoneListener != null) mOnMeasureDoneListener.onMeasureDone(this);
    }

    /**
     * Determines the width of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The width of the view, honoring constraints from measureSpec
     */
    private int measureWidth(int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        if (specMode == MeasureSpec.EXACTLY) {
            // Format the text using this exact width, and the current mode.
            breakWidth(specSize);
            // We were told how big to be.
            return specSize;
        } else if (specMode == MeasureSpec.AT_MOST) {
            // Use the AT_MOST size - if we had very short text, we may need even less
            // than the AT_MOST value, so return the minimum.
            return Math.min(breakWidth(specSize), specSize);
        } else {
            // We're not given any width - so in this case we assume we have an unlimited width?
            return breakWidth(specSize);
        }
    }

    /**
     * use Paint.breakText() to calculate widget entire line count
     * and measure first line width
     *
     * @param availableWidth The available width
     * @return The width of the view
     */
    private int breakWidth(int availableWidth) {
        int maxWidth = availableWidth - getPaddingLeft() - getPaddingRight();

        // we assume the width always equals first measure, so we just measure once
        if (mLines == null) {
            // If maxWidth is -1, interpret that as meaning to render the string on a single
            // line. Skip everything.
            if (maxWidth == -1) {
                mLines = new ArrayList<int[]>(1);
                mLines.add(new int[]{0, mText.length()});
            } else {
                int index = 0;
                int newlineIndex = 0;
                int endCharIndex = 0;
                mLines = new ArrayList<int[]>(mMaxLineCount * 2);

                // breakText line by line and store line's indices
                while (index < mText.length()) {
                    if (index == newlineIndex) {
                        newlineIndex = mText.indexOf(NEW_LINE_STR, newlineIndex);
                        endCharIndex = (newlineIndex != -1) ? newlineIndex : mText.length();
                    }

                    int charCount = mPaint.breakText(mText, index, endCharIndex, true, maxWidth, null);
                    if (charCount > 0) {
                        mLines.add(new int[]{index, index + charCount});
                        index += charCount;
                    }

                    if (index == newlineIndex) {
                        newlineIndex++;
                        index++;
                    }
                }
            }
        }

        int widthUsed;
        // If we required only one line, return its length, otherwise we used
        // whatever the maxWidth supplied was.
        switch (mLines.size()) {
            case 1:
                widthUsed = (int) (mPaint.measureText(mText) + 0.5f);
                break;
            case 0:
                widthUsed = 0;
                break;
            default:
                widthUsed = maxWidth;
                break;
        }

        return widthUsed + getPaddingLeft() + getPaddingRight();
    }

    /**
     * Determines the height of this view
     *
     * @param measureSpec A measureSpec packed into an int
     * @return The height of the view, honoring constraints from measureSpec
     */
    private int measureHeight(int measureSpec) {
        int result;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);

        mAscent = (int) mPaint.ascent();
        if (specMode == MeasureSpec.EXACTLY) {
            // We were told how big to be, so nothing to do.
            result = specSize;
        } else {
            // The lines should already be broken up. Calculate our max desired height
            // for our current mode.
            if (mExpanded) {
                mDrawLineCount = mLines.size();
            } else if (mLines.size() > mMaxLineCount) {
                mDrawLineCount = mMaxLineCount;
            } else {
                // when collapse mode on, but entire line count less then or equals
                // max line count, we should turn expand mode on
                mDrawLineCount = mLines.size();
                mExpanded = true;
            }

            int textHeight = (int) (-mAscent + mPaint.descent());
            result = getPaddingTop() + getPaddingBottom();
            if (mDrawLineCount > 0) {
                result += mDrawLineCount * textHeight + (mDrawLineCount - 1) * mLineSpacing;
            } else {
                result += textHeight;
            }

            // Respect AT_MOST value if that was what is called for by measureSpec.
            if (specMode == MeasureSpec.AT_MOST) result = Math.min(result, specSize);
        }
        return result;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int renderWidth = canvas.getWidth() - getPaddingLeft() - getPaddingRight();
        float x = getPaddingLeft();
        float y = getPaddingTop() - mAscent;

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < mDrawLineCount; i++) {
            // obtain current line to draw
            sb.append(mText, mLines.get(i)[0], mLines.get(i)[1]);

            // draw the ellipsis if necessary
            if (!mExpanded && mDrawLineCount - i == 1) {
                float lineDrawWidth = mPaint.measureText(sb, 0, sb.length());
                float ellipsisWidth = mPaint.measureText(mStrEllipsis);

                // delete one char then measure until enough to draw ellipsize text
                while (lineDrawWidth + ellipsisWidth > renderWidth) {
                    sb.deleteCharAt(sb.length() - 1);
                    lineDrawWidth = mPaint.measureText(sb, 0, sb.length());
                }
                sb.append(mStrEllipsis);
            }

            // draw the current line.
            canvas.drawText(sb, 0, sb.length(), x, y, mPaint);

            y += (-mAscent + mPaint.descent()) + mLineSpacing;
            // stop if canvas not enough space to draw next line
            if (y > canvas.getHeight()) break;

            // clean the line buffer
            sb.delete(0, sb.length());
        }
    }

    public void elipsizeSwitch() {
        if (mExpanded) collapse();
        else expand();
    }

    public boolean isExpanded() {
        return mExpanded;
    }

    public void expand() {
        mExpanded = true;
        requestLayout();
        invalidate();
    }

    public void collapse() {
        mExpanded = false;
        requestLayout();
        invalidate();
    }

    public void setOnMeasureDoneListener(OnMeasureDoneListener onMeasureDoneListener) {
        mOnMeasureDoneListener = onMeasureDoneListener;
    }

    public interface OnMeasureDoneListener {
        void onMeasureDone(View v);
    }
}
