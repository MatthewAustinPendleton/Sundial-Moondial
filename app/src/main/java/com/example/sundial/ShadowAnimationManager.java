package com.example.sundial;

import android.animation.ValueAnimator;
import android.util.Log;
import android.view.animation.LinearInterpolator;

public class ShadowAnimationManager {

    private float shadowLength;
    private float shadowWidth;
    private float shadowDirection;

    private final SundialView sundialView;
    private final ShadowManager shadowManager;
    private final long animationDuration = 1000; // Duration for all animations in milliseconds

    public ShadowAnimationManager(SundialView sundialView, ShadowManager shadowManager) {

        this.sundialView = sundialView;
        this.shadowManager = shadowManager;

    }

    public void animateShadowLength(double solarAltitude, double phonePitch) {

        float start = (float) shadowLength;
        float end = (float) shadowManager.calculateShadowLength(solarAltitude, phonePitch);

        ValueAnimator lengthAnimator = ValueAnimator.ofFloat(start, end);
        lengthAnimator.setDuration(animationDuration);
        lengthAnimator.setInterpolator(new LinearInterpolator());
        lengthAnimator.addUpdateListener(animation -> {
            shadowLength = (float) animation.getAnimatedValue();
            updateSundialView();
        });
        lengthAnimator.start();

    }

    public void animateShadowWidth(float width) {

        float start = shadowWidth;
        float end = width;

        ValueAnimator widthAnimator = ValueAnimator.ofFloat(start, end);
        widthAnimator.setDuration(animationDuration);
        widthAnimator.setInterpolator(new LinearInterpolator());
        widthAnimator.addUpdateListener(animation -> {
            shadowWidth = (float) animation.getAnimatedValue();
            updateSundialView();
        });
        widthAnimator.start();

    }

    public void animateShadowDirection(double shadowDirection) {
        // Get the current direction
        float start = this.shadowDirection;
        float end = (float) shadowDirection;

        // Calculate the shortest path between angles
        float diff = ((end - start + 180 + 360) % 360) - 180;
        end = start + diff;

        Log.d("ShadowDebug", String.format(
                "Animating direction: start=%.2f, target=%.2f, diff=%.2f",
                start, shadowDirection, diff));

        ValueAnimator directionAnimator = ValueAnimator.ofFloat(start, end);
        directionAnimator.setDuration(animationDuration);
        directionAnimator.setInterpolator(new LinearInterpolator());
        directionAnimator.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            this.shadowDirection = (value + 360) % 360; // Normalize to 0-360
            updateSundialView();
        });
        directionAnimator.start();
    }


    public void startAnimation(double solarAltitude, double shadowDirection, double phonePitch) {

        animateShadowLength(solarAltitude, phonePitch);
        float angularWidth = (float) shadowManager.calculateAngularWidth(shadowDirection);
        animateShadowWidth(angularWidth);
        animateShadowDirection(shadowDirection);

    }

    private void updateSundialView() {

        sundialView.updateShadow((float) shadowLength, (float) shadowWidth, (float) shadowDirection);
        sundialView.invalidate(); // triggers onDraw in SundialView

    }

}