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
        int outermostRadius = Math.min(width, height) / 2 - 60; // Increase for padding
        int middleRadius = outermostRadius - 75;
        int middleRadius2 = middleRadius - 40;
        int innermostRadius = middleRadius2 - 40;
        int centerX = width / 2;
        int centerY = height / 2;

        // Outer Circle
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawCircle(centerX, centerY, outermostRadius, paint);

        // Middle Circle
        paint.setStrokeWidth(4);
        canvas.drawCircle(centerX, centerY, middleRadius, paint);

        // Second Middle Circle
        canvas.drawCircle(centerX, centerY, middleRadius2, paint);

        // Inner circle
        canvas.drawCircle(centerX, centerY, innermostRadius, paint);

        // Draw hour and compass markings
        drawMarkings(canvas, centerX, centerY, outermostRadius, middleRadius, middleRadius2, innermostRadius);

        drawCenterCircle(canvas, centerX, centerY);

    }

    private void drawMarkings(Canvas canvas, int centerX, int centerY, int outermostRadius, int middleRadius, int middleRadius2, int innermostRadius) {

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);

        // Roman numeral hour labels
        String[] romanNumerals = {"VI", "VII", "VIII", "IX", "X", "XI", "XII", "I", "II", "III", "IV", "V"};

        for (int i = 0; i < 12; i++) {

            double angle = Math.toRadians((i * 15) - 172); // Upper semicircle

            // Roman numeral positioning (near the outermost circle)
            int textX = (int) (centerX + (outermostRadius - 30) * Math.cos(angle));
            int textY = (int) (centerY + (outermostRadius - 30) * Math.sin(angle) + 12);
            paint.setTextSize(36);
            canvas.drawText(romanNumerals[i], textX, textY, paint);

            // Hour line positioning (between the inner and middle circle)
            int startX = (int) (centerX + middleRadius * Math.cos(angle));
            int startY = (int) (centerY + middleRadius * Math.sin(angle));
            int endX = (int) (centerX + innermostRadius * Math.cos(angle));
            int endY = (int) (centerY + innermostRadius * Math.sin(angle));
            paint.setStrokeWidth(3);
            canvas.drawLine(startX, startY, endX, endY, paint);

            // Extend the hour line to the center
            paint.setStrokeWidth(3.0f);
            canvas.drawLine(endX, endY, centerX, centerY, paint);

        }

        for (int i = 0; i < 48; i++) {
            if (i % 4 == 0) continue;
            double angle = Math.toRadians((i * 7.5) - 172);
            int startX = (int) (centerX + (middleRadius2 - 10) * Math.cos(angle));
            int startY = (int) (centerY + (middleRadius2 - 10) * Math.sin(angle));
            int endX = (int) (centerX + innermostRadius * Math.cos(angle));
            int endY = (int) (centerY + innermostRadius * Math.sin(angle));

            paint.setStrokeWidth(2);
            canvas.drawLine(startX, startY, endX, endY, paint);
        }

        // compass directions
        paint.setColor(Color.BLACK);
        paint.setTextSize(48);
        canvas.drawText("N", centerX, centerY - outermostRadius - 20, paint);
        canvas.drawText("E", centerX + outermostRadius + 20, centerY + 20, paint);
        canvas.drawText("S", centerX, centerY + outermostRadius + 50, paint);
        canvas.drawText("W", centerX - outermostRadius - 30, centerY + 20, paint);

    }

    private void drawCenterCircle(Canvas canvas, int centerX, int centerY) {
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);
        int filledCircleRadius = 10;
        canvas.drawCircle(centerX, centerY, filledCircleRadius, paint);
    }

}