package com.mfh.framework.uikit.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;

/**
 * Created by kun on 15/8/17.
 */
public class AnimationUtil {
    /**
     * 旋转 Rotate
     */
    public static Animation getRotateAnimation(float fromDegrees,
                                               float toDegrees, long durationMillis) {
        RotateAnimation rotate = new RotateAnimation(fromDegrees, toDegrees,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        rotate.setDuration(durationMillis);
        rotate.setFillAfter(true);
        return rotate;
    }

    /**
     * 位移 Translate
     */
    public static Animation getTranslateAnimation(float fromXDelta,
                                                  float toXDelta, float fromYDelta, float toYDelta,
                                                  long durationMillis) {
        TranslateAnimation translate = new TranslateAnimation(fromXDelta,
                toXDelta, fromYDelta, toYDelta);
        translate.setDuration(durationMillis);
        translate.setFillAfter(true);
        return translate;
    }

    /**
     * 透明度 Alpha
     */
    public static Animation getAlphaAnimation(float fromAlpha, float toAlpha,
                                              long durationMillis) {
        AlphaAnimation alpha = new AlphaAnimation(fromAlpha, toAlpha);
        alpha.setDuration(durationMillis);
        alpha.setFillAfter(true);
        return alpha;
    }

    /**
     * 缩放 Scale
     */
    public static Animation getScaleAnimation(float scaleXY, long durationMillis) {
        ScaleAnimation scale = new ScaleAnimation(1.0f, scaleXY, 1.0f, scaleXY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scale.setDuration(durationMillis);
        return scale;
    }
    public static Animation getScaleAnimation(float fromScaleXY, float toScaleXY, long durationMillis) {
        ScaleAnimation scale = new ScaleAnimation(fromScaleXY, toScaleXY, fromScaleXY, toScaleXY,
                Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,
                0.5f);
        scale.setDuration(durationMillis);
        return scale;
    }

    public static Animation clickAnimation(float scaleXY, long durationMillis) {
        AnimationSet set = new AnimationSet(true);
        set.addAnimation(getScaleAnimation(scaleXY, durationMillis));
        set.setDuration(durationMillis);
        return set;
    }
}
