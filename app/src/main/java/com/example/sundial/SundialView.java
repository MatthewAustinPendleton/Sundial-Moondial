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
        int radius = Math.min(width, height) / 2 - 20;

        // Draw the circle
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(8);
        canvas.drawCircle((float) width / 2, (float) height / 2, radius, paint);

        // Draw hour and compass markings
        drawMarkings(canvas, width / 2, height / 2, radius);

    }

    private void drawMarkings(Canvas canvas, int centerX, int centerY, int radius) {

        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextSize(36);

        for (int i = 0; i < 24; i++) {

            double angle = Math.toRadians((i * 15) - 90); // 15 degrees per hour
            int markRadius = radius - 30;
            int endRadius = (i % 6 == 0) ? radius - 10 : radius - 20;

            // Calculate line start and end positions for markings
            int startX = (int) (centerX + markRadius * Math.cos(angle));
            int startY = (int) (centerY + markRadius * Math.sin(angle));
            int endX = (int) (centerX + endRadius * Math.cos(angle));
            int endY = (int) (centerY + endRadius * Math.sin(angle));
            canvas.drawLine(startX, startY, endX, endY, paint);

            // Add hour numbers
            if (i % 2 == 0) {
                int textX = (int) (centerX + (markRadius - 40) * Math.cos(angle));
                int textY = (int) (centerY + (markRadius - 40) * Math.sin(angle) + 12);
                canvas.drawText(String.valueOf((i == 0 ? 12 : i / 2)), textX, textY, paint);
            }

        }

        // Draw compass directions
        paint.setTextSize(48);
        canvas.drawText("N", centerX, centerY - radius + 80, paint);
        canvas.drawText("E", centerX + radius - 50, centerY + 20, paint);
        canvas.drawText("S", centerX, centerY + radius - 30, paint);
        canvas.drawText("W", centerX - radius + 50, centerY + 20, paint);

    }

}
