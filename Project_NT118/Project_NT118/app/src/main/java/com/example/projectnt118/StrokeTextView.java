package com.example.projectnt118;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Color;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatTextView;

public class StrokeTextView extends AppCompatTextView {
    private Paint strokePaint;

    public StrokeTextView(Context context) {
        super(context);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StrokeTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        strokePaint = new Paint();
        strokePaint.setStyle(Paint.Style.STROKE);
        strokePaint.setStrokeWidth(30); // Set the stroke width
        strokePaint.setColor(Color.WHITE); // Set the stroke color
    }

    @Override
    protected void onDraw(Canvas canvas) {
        int textColor = getCurrentTextColor();
        setTextColor(strokePaint.getColor());
        getPaint().setStrokeWidth(strokePaint.getStrokeWidth());
        getPaint().setStyle(Paint.Style.STROKE);
        super.onDraw(canvas);

        setTextColor(textColor);
        getPaint().setStyle(Paint.Style.FILL);
        super.onDraw(canvas);
    }
}
