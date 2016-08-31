package com.mfh.owner.ui.shake;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.mfh.framework.anlaysis.logger.ZLogger;
import com.mfh.framework.uikit.utils.AnimationUtil;
import com.mfh.owner.R;

/**
 * Created by Administrator on 2015/5/22.
 * 摇一摇
 */
public class ShakeUtil {

    /**
     * 摇一摇类型
     * */
    public enum ShakeType{
        SHAKE_STORE,        //微店
        SHAKE_PARCEL,        //仓储
        SHAKE_PEOPLE,       //小伙伴
        SHAKE_REDENVELOPE   //红包
    }

    /**
     * 播放声音
     * Should have subtitle controller already set
     * */
    public static void playSound(Activity context){
        try{
            MediaPlayer player;
            player = MediaPlayer.create(context, R.raw.door_bell);
            player.setLooping(false);
//            player.prepare();
            player.start();
        }
        catch(Exception e){
            ZLogger.e("playSound:" + e.toString());
        }
    }

    public static void playSoundAndVibrate(Activity context){
        playSound(context);

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        //第一个｛｝里面是节奏数组， 第二个参数是重复次数，-1为不重复
        vibrator.vibrate( new long[]{500,200,500,200}, -1);
    }


    public static Animation shakeRotateAnimation(){
        AnimationSet set = new AnimationSet(true);
        Animation anim1 = AnimationUtil.getRotateAnimation(0, -30, 100);
        anim1.setStartOffset(100);
        set.addAnimation(anim1);

        Animation anim2 = AnimationUtil.getRotateAnimation(-30, 0, 200);
        anim2.setStartOffset(200);
        set.addAnimation(anim2);

        Animation anim3 = AnimationUtil.getRotateAnimation(0, 30, 200);
        anim3.setStartOffset(400);
        set.addAnimation(anim3);

        Animation anim4 = AnimationUtil.getRotateAnimation(30, 0, 100);
        anim4.setStartOffset(600);
        set.addAnimation(anim4);

        set.setFillAfter(true);
        set.setDuration(640);
        return set;
    }

    /**
     * 左右摆动
     * */
    public static Animation shakeAnimation(int X) {
        AnimationSet set = new AnimationSet(true);
        Animation anim1 = AnimationUtil.getTranslateAnimation(0, -200, 0, 0, 100);
        anim1.setStartOffset(100);
        set.addAnimation(anim1);
        Animation anim2 = AnimationUtil.getTranslateAnimation(-200, 400, 0, 0, 200);
        anim2.setStartOffset(300);
        set.addAnimation(anim2);
        Animation anim3 = AnimationUtil.getTranslateAnimation(400, -200, 0, 0, 200);
        anim3.setStartOffset(500);
        set.addAnimation(anim3);
        Animation anim4 = AnimationUtil.getTranslateAnimation(-200, 0, 0, 0, 100);
        anim4.setStartOffset(600);
        set.addAnimation(anim4);
        set.setFillAfter(true);
        set.setDuration(640);
        return set;
    }

    public static Animation jumpAnimation(int X) {
        AnimationSet set = new AnimationSet(true);
        Animation anim1 = AnimationUtil.getTranslateAnimation( 0, 0, 0, -200,100);
        anim1.setStartOffset(100);
        set.addAnimation(anim1);
        Animation anim2 = AnimationUtil.getTranslateAnimation( 0, 0, -200, 400,200);
        anim2.setStartOffset(300);
        set.addAnimation(anim2);
        Animation anim3 = AnimationUtil.getTranslateAnimation( 0, 0, 400, -200,200);
        anim3.setStartOffset(500);
        set.addAnimation(anim3);
        Animation anim4 = AnimationUtil.getTranslateAnimation( 0, 0, -200, 0,100);
        anim4.setStartOffset(600);
        set.addAnimation(anim4);
        set.setFillAfter(true);
        set.setDuration(640);
        return set;
    }

    /**
     * 播放摇一摇动画
     * */
    private void startAnimation(View view){
        AnimationSet animup = new AnimationSet(true);
        TranslateAnimation mytranslateanimup0 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                -0.5f);
        mytranslateanimup0.setDuration(600);
        TranslateAnimation mytranslateanimup1 = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF, 0f,
                Animation.RELATIVE_TO_SELF, 0f, Animation.RELATIVE_TO_SELF,
                +0.5f);
        mytranslateanimup1.setDuration(600);
        mytranslateanimup1.setStartOffset(600);
        animup.addAnimation(mytranslateanimup0);
        animup.addAnimation(mytranslateanimup1);
        view.startAnimation(animup);
    }

}
