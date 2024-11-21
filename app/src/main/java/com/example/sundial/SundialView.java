package com.example.sundial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;

public class SundialView extends View {

    private Paint paint;
    private float shadowLength = 0f;
    private float shadowWidth = 0f;
    private float shadowDirection = 0f;
    private int outermostRadius;
    private int middleRadius2;

    private static float SHADOW_ANGLE_OFFSET = 5.0f;

    public SundialView(Context context, AttributeSet attributeSet) {

        super(context, attributeSet);
        init();

    }

    private void init() {

        paint = new Paint();
        paint.setColor(Color.DKGRAY);
        paint.setStyle(Paint.Style.STROKE);
        paint.setAntiAlias(true);

    }

    public void updateShadow(float length, float width, float direction) {

        this.shadowLength = length;
        this.shadowWidth = width;
        this.shadowDirection = direction;

    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {

        super.onDraw(canvas);

        // Calculate center of the sundial
        int centerX = getWidth() / 2;
        int centerY = getHeight() / 2;

        // Draw the Sundial circles
        outermostRadius = Math.min(getWidth(), getHeight()) / 2 - 60;
        int middleRadius = outermostRadius - 75;
        middleRadius2 = middleRadius - 40;
        int innermostRadius = middleRadius2 - 40;

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

        drawShadow(canvas, centerX, centerY);

    }

    private void drawMarkings(Canvas canvas, int centerX, int centerY, int outermostRadius, int middleRadius, int middleRadius2, int innermostRadius) {

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);

        // Roman numeral hour labels (upper half only for horizontal sundial)
        String[] romanNumerals = {"VI", "VII", "VIII", "IX", "X", "XI", "XII", "I", "II", "III", "IV", "V"};

        // Create a Rect to measure text bounds
        Paint.FontMetrics fontMetrics = paint.getFontMetrics();

        // Distribute Roman numerals over the top 180° (from -90° to +90°)
        for (int i = 0; i < 12; i++) {

            // Calculate angle for each numeral in the top half (15° increments)
            double angle = Math.toRadians((i * 15) - 173); // Range: -90° (left) to +90° (right)

            // Calculate raw position
            int rawX = (int) (centerX + (middleRadius + 20) * Math.cos(angle));
            int rawY = (int) (centerY + (middleRadius + 20) * Math.sin(angle));

            // Measure text bounds to adjust for width and height
            float textWidth = paint.measureText(romanNumerals[i]);
            float textHeight = fontMetrics.bottom - fontMetrics.top;

            // Center the text around its raw position
            float adjustedX = rawX - (textWidth / 2); // Offset by half the width
            float adjustedY = rawY + (textHeight / 4); // Offset vertically for alignment

            if (romanNumerals[i].equals("VI")) {
                adjustedX -= 10;
            }
            else if (romanNumerals[i].equals("VII")) {
                adjustedX -= 5;
            }

            // Draw the numeral
            paint.setTextSize(36);
            canvas.drawText(romanNumerals[i], adjustedX, adjustedY, paint);
        }

        // Draw hour lines from the center to the inner circle for the top half
        for (int i = 0; i < 12; i++) {
            double angle = Math.toRadians((i * 15) - 173);
            int startX = (int) (centerX + middleRadius * Math.cos(angle));
            int startY = (int) (centerY + middleRadius * Math.sin(angle));
            int endX = (int) (centerX + innermostRadius * Math.cos(angle));
            int endY = (int) (centerY + innermostRadius * Math.sin(angle));
            paint.setStrokeWidth(3);
            canvas.drawLine(startX, startY, endX, endY, paint);
        }

        // Draw compass directions
        paint.setColor(Color.BLACK);
        paint.setTextSize(48);
        canvas.drawText("N", centerX, centerY - outermostRadius - 20, paint);
        canvas.drawText("E", centerX + outermostRadius + 20, centerY + 20, paint);
        canvas.drawText("W", centerX - outermostRadius - 50, centerY + 20, paint);
        canvas.drawText("S", centerX, centerY + outermostRadius + 50, paint);
    }


    private void drawCenterCircle(Canvas canvas, int centerX, int centerY) {

        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.DKGRAY);
        int filledCircleRadius = 10;
        canvas.drawCircle(centerX, centerY, filledCircleRadius, paint);

    }

    private void drawShadow(Canvas canvas, int centerX, int centerY) {
        if (shadowLength > 0) {
            // Clamp shadow length to the second inner circle
            float maxShadowLength = middleRadius2;
            float scaledShadowLength = Math.max(shadowLength, maxShadowLength);

            // Adjust azimuth for shadow direction and map to canvas coordinates
            float adjustedDirection = 360 - shadowDirection + 90 - SHADOW_ANGLE_OFFSET; // Correct for coordinate system

            // Normalize to [0°, 360°] only once
            adjustedDirection = (adjustedDirection + 360) % 360;

            // Calculate the end point of the shadow
            float endX = (float) (centerX + scaledShadowLength * Math.cos(Math.toRadians(adjustedDirection)));
            float endY = (float) (centerY - scaledShadowLength * Math.sin(Math.toRadians(adjustedDirection)));

            // Log for debugging
            Log.d("ShadowDraw", String.format(
                    "Shadow Draw - Start: (%d, %d), End: (%.2f, %.2f), Length: %.2f, Azimuth: %.2f, Adjusted: %.2f",
                    centerX, centerY, endX, endY, scaledShadowLength, shadowDirection, adjustedDirection
            ));

            // Draw the shadow
            paint.setStrokeWidth(Math.min(shadowWidth, 10)); // Clamp shadow width for visibility
            paint.setColor(Color.DKGRAY);
            canvas.drawLine(centerX, centerY, endX, endY, paint);
        }
    }

    public int getOutermostRadius() {

        return outermostRadius;

    }

    public int getMiddleRadius2() {

        return middleRadius2;

    }

}