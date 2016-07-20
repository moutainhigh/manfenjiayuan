package com.mfh.framework.uikit.widget;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * 自适应改变文本字体大小
 * */
public class FontFitTextView extends TextView {
    private final static float DEFAULT_MIN_TEXT_SIZE = 12;
    private final static float DEFAULT_MAX_TEXT_SIZE = 64;

    public FontFitTextView(Context context) {
        super(context);
        initialise();
    }

    public FontFitTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialise();
    }

    private void initialise() {
        testPaint = new Paint();
        testPaint.set(this.getPaint());
        //max size defaults to the intially specified text size unless it is too small
        maxTextSize = this.getTextSize();
//        ZLogger.d(String.format("refitText (%f,%f)", minTextSize, maxTextSize));
        if (maxTextSize < DEFAULT_MIN_TEXT_SIZE || maxTextSize > DEFAULT_MAX_TEXT_SIZE) {
            maxTextSize = DEFAULT_MAX_TEXT_SIZE;
        }
        minTextSize = DEFAULT_MIN_TEXT_SIZE;
//        ZLogger.d(String.format("refitText (%f,%f)", minTextSize, maxTextSize));
    }

    /* Re size the font so the specified text fits in the text box
     * assuming the text box is the specified width.
     */
    private void refitText(String text, int textWidth) {

//        ZLogger.d(String.format("refitText start:%s,%d(%f,%f)", text, textWidth, minTextSize, maxTextSize));
        if (textWidth > 0) {
            int availableWidth = textWidth - this.getPaddingLeft() - this.getPaddingRight();
            float trySize = maxTextSize;

            testPaint.setTextSize(trySize);
            while ((trySize > minTextSize) && (testPaint.measureText(text) > availableWidth)) {
                trySize -= 1;
                if (trySize <= minTextSize) {
                    trySize = minTextSize;
                    break;
                }
                testPaint.setTextSize(trySize);
            }

            this.setTextSize(trySize);

//            ZLogger.d(String.format("refitText start:%s,%f(%f,%f)", text, trySize, minTextSize, maxTextSize));
        }
    }

    @Override
    protected void onTextChanged(final CharSequence text, final int start, final int before, final int after) {
        refitText(text.toString(), this.getWidth());
    }

    @Override
    protected void onSizeChanged (int w, int h, int oldw, int oldh) {
        if (w != oldw) {
            refitText(this.getText().toString(), w);
        }
    }

    //Getters and Setters
    public float getMinTextSize() {
        return minTextSize;
    }

    public void setMinTextSize(int minTextSize) {
        this.minTextSize = minTextSize;
    }

    public float getMaxTextSize() {
        return maxTextSize;
    }

    public void setMaxTextSize(int minTextSize) {
        this.maxTextSize = minTextSize;
    }

    //Attributes
    private Paint testPaint;
    private float minTextSize;
    private float maxTextSize;

}