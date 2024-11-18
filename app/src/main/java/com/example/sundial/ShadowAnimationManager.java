package com.example.sundial;

import android.animation.ValueAnimator;
import android.renderscript.Sampler;
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

    public void animateShadowWidth(double solarAltitude) {

        float start = (float) shadowWidth;
        float end = (float) shadowManager.calculateShadowWidth(solarAltitude);

        ValueAnimator widthAnimator = ValueAnimator.ofFloat(start, end);
        widthAnimator.setDuration(animationDuration);
        widthAnimator.setInterpolator(new LinearInterpolator());
        widthAnimator.addUpdateListener(animation -> {
            shadowWidth = (float) animation.getAnimatedValue();
            updateSundialView();
        });
        widthAnimator.start();

    }

    public void animateShadowDirection(double solarAzimuth, double phoneRoll) {

        float start = (float) shadowDirection;
        float end = (float) shadowManager.calculateShadowDirection(solarAzimuth, phoneRoll);

        ValueAnimator directionAnimator = ValueAnimator.ofFloat(start, end);
        directionAnimator.setDuration(animationDuration);
        directionAnimator.setInterpolator(new LinearInterpolator());
        directionAnimator.addUpdateListener(animation -> {
            shadowDirection = (float) animation.getAnimatedValue();
            updateSundialView();
        });
        directionAnimator.start();

    }

    public void startAnimation(double solarAltitude, double solarAzimuth, double phonePitch, double phoneRoll) {

        animateShadowLength(solarAltitude, phonePitch);
        animateShadowWidth(solarAltitude);
        animateShadowDirection(solarAzimuth, phoneRoll);

    }

    private void updateSundialView() {

        sundialView.updateShadow((float) shadowLength, (float) shadowWidth, (float) shadowDirection);
        sundialView.invalidate(); // triggers onDraw in SundialView

    }

}
