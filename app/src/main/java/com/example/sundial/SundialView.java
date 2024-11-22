package com.example.sundial;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
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
    private int middleRadius;

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
        middleRadius = outermostRadius - 75;
        int middleRadius2 = middleRadius - 40;
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
            // Clamp shadow length
            float maxShadowLength = outermostRadius - 10;
            float scaledShadowLength = Math.min(shadowLength, maxShadowLength);

            // Convert shadow direction to canvas angle
            // Shadow direction is in compass coords (0° = North, clockwise)
            // Need to convert to canvas coords (0° = East, clockwise)
            float canvasAngle = (shadowDirection + 270) % 360;

            Log.d("ShadowDebug", String.format(
                    "Shadow calc: direction=%.2f, canvasAngle=%.2f",
                    shadowDirection, canvasAngle));

            // Draw only in upper half of sundial (angle between 180° and 360°)
            if (canvasAngle < 180) {
                return;
            }

            // Clamp the shadow width
            float cappedWidth = Math.min(shadowWidth, scaledShadowLength / 3.0f);
            float halfAngularWidth = Math.min(cappedWidth / 2.0f, 7.5f); // Max 15° total width

            // Calculate angles for the pizza slice
            float leftAngle = canvasAngle - halfAngularWidth;
            float rightAngle = canvasAngle + halfAngularWidth;

            // Clamp angles to stay in upper half
            if (leftAngle < 180) leftAngle = 180;
            if (rightAngle > 360) rightAngle = 360;

            // Create the pizza slice path
            Path shadowPath = new Path();
            shadowPath.moveTo(centerX, centerY);

            RectF arcBounds = new RectF(
                    centerX - scaledShadowLength,
                    centerY - scaledShadowLength,
                    centerX + scaledShadowLength,
                    centerY + scaledShadowLength
            );

            float sweepAngle = rightAngle - leftAngle;

            shadowPath.arcTo(arcBounds, leftAngle, sweepAngle, false);
            shadowPath.lineTo(centerX, centerY);
            shadowPath.close();

            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.DKGRAY);
            paint.setAlpha(128);
            canvas.drawPath(shadowPath, paint);
        }
    }

    public int getOutermostRadius() {

        return outermostRadius;

    }

    public int getMiddleRadius() {

        return middleRadius;

    }

}