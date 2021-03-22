package com.scan.barcode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.ViewGroup;

import androidx.core.content.ContextCompat;

import info.androidhive.barcode.R;

/**
 * Created by ravi on 04/05/17.
 */

public class ScannerOverlay extends ViewGroup {
    private final Paint image = new Paint();
    private int left, top, endY;
    private int rectWidth, rectHeight;
    private int frames;
    private boolean revAnimation;
    private int lineColor, lineWidth, frameColor;
    private Paint eraser = new Paint();
    private Bitmap original;
    private Bitmap scale;

    public ScannerOverlay(Context context) {
        super(context);
    }

    public ScannerOverlay(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScannerOverlay(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.ScannerOverlay,
                0, 0);
        rectWidth = a.getInteger(R.styleable.ScannerOverlay_square_width, getResources().getInteger(R.integer.scanner_rect_width));
        rectHeight = a.getInteger(R.styleable.ScannerOverlay_square_height, getResources().getInteger(R.integer.scanner_rect_height));
        lineColor = a.getColor(R.styleable.ScannerOverlay_line_color, ContextCompat.getColor(context, R.color.scanner_line));
        lineWidth = a.getInteger(R.styleable.ScannerOverlay_line_width, getResources().getInteger(R.integer.line_width));
        frames = a.getInteger(R.styleable.ScannerOverlay_line_speed, getResources().getInteger(R.integer.line_width));


    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onLayout(boolean changed, int left, int top, int right, int bottom) {
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        left = (w - dpToPx(rectWidth)) / 2;
        top = (h - dpToPx(rectHeight)) / 2;
        endY = top;
        super.onSizeChanged(w, h, oldw, oldh);
    }

    public int dpToPx(int dp) {
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        return Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    @Override
    public boolean shouldDelayChildPressedState() {
        return false;
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // draw transparent rect
        original = BitmapFactory.decodeResource(getResources(), R.drawable.frame);
        scale = Bitmap.createScaledBitmap(original, (dpToPx(rectWidth) + 10), dpToPx(rectHeight) + 10, true);
        int cornerRadius = 0;
        canvas.drawBitmap(scale, left - 5, top - 5, image);
        eraser.setAntiAlias(true);
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        RectF rect = new RectF(left, top, dpToPx(rectWidth) + left, dpToPx(rectHeight) + top);
        canvas.drawRoundRect(rect, (float) cornerRadius, (float) cornerRadius, eraser);
        // draw horizontal line
        final Paint line = new Paint();
        line.setColor(lineColor);
        line.setStrokeWidth(Float.valueOf(lineWidth));

        // draw the line to product animation
        if (endY >= top + dpToPx(rectHeight) + frames) {
            revAnimation = true;
        } else if (endY == top + frames) {
            revAnimation = false;
        }

        // check if the line has reached to bottom
        if (revAnimation) {
            endY -= frames;
        } else {
            endY += frames;
        }
        canvas.drawLine(left, endY, left + dpToPx(rectWidth), endY, line);
        invalidate();
    }
}