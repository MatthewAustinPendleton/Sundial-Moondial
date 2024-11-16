package com.example.sundial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;

public class SundialView extends View {

    private Paint paint;

    public SundialView(Context context, AttributeSet attributeSet) {

        super(context, attributeSet);
        init();

    }

    private void init() {

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextAlign(Paint.Align.CENTER);

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);
        int width = getWidth();
        int height = getHeight();
        int radius = Math.min(width, height) / 2 - 90;
        int centerX = width/2;
        int centerY = height/2;

        // Outer Circle
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawCircle(centerX, centerY, radius, paint);

        // Concentric Circles
        paint.setStrokeWidth(4);
        canvas.drawCircle(centerX, centerY, radius - 20, paint);
        canvas.drawCircle(centerX, centerY, radius - 40, paint);

        // Draw hour and compass markings
        drawMarkings(canvas, centerX, centerY, radius);

    }

    private void drawMarkings(Canvas canvas, int centerX, int centerY, int radius) {

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);

        // Roman numeral hour labels
        String[] romanNumerals = {"VI", "VII", "VIII", "IX", "X", "XI", "XII", "I", "II", "III", "IV", "V"};

        for (int i = 0; i < 12; i++) {

            double angle = Math.toRadians((i * 15) - 170);

            int startRadius = radius - 20;
            int endRadius = radius - 10;

            int startX = (int) (centerX + startRadius * Math.cos(angle));
            int startY = (int) (centerY + startRadius * Math.sin(angle));
            int endX = (int) (centerX + endRadius * Math.cos(angle));
            int endY = (int) (centerY + endRadius * Math.sin(angle));
            paint.setStrokeWidth(3);
            canvas.drawLine(startX, startY, endX, endY, paint);

            int textX = (int) (centerX + (startRadius - 40) * Math.cos(angle));
            int textY = (int) (centerY + (startRadius - 40) * Math.sin(angle) + 12);
            paint.setTextSize(36);
            canvas.drawText(romanNumerals[i], textX, textY, paint);

        }

        paint.setColor(Color.BLACK);
        paint.setTextSize(48);
        canvas.drawText("N", centerX, centerY - radius - 10, paint);
        canvas.drawText("E", centerX + radius + 20, centerY + 20, paint);
        canvas.drawText("S", centerX, centerY + radius + 50, paint);
        canvas.drawText("W", centerX - radius - 30, centerY + 20, paint);

    }

}
