package com.st.BlueSTSDK.gui.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;

/**
 * Helper class that restart an animator when it finish
 */
public class RepeatAnimator {

    /**
     * Animation animation to repeat
     */
    private Animator mAnim;

    /**
     * Number of times to repeat the animation
     */
    private int mRepeatCount;

    /**
     * Number of times that the animation run
     */
    private int mCurrentAnimationCount;


    /**
     * Prepare the animation for run repeatCount times
     * @param anim animation to run
     * @param repeatCount number of times to repeat the animation
     */
    public RepeatAnimator(Animator anim,int repeatCount){
        mAnim=anim;
        mRepeatCount = repeatCount;
        mCurrentAnimationCount =0;
    }

    /**
     * If the animation is not running it start the animation
     */
    public void start(){
        if(isRunning())
            return;
        mCurrentAnimationCount=mRepeatCount;
        //when the animation ends we decrease the counter and start again the animation
        mAnim.addListener(new AnimatorListenerAdapter() {

            @Override
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                mCurrentAnimationCount--;
                if(mCurrentAnimationCount>0){
                    animator.start();
                }else{
                    animator.removeListener(this);
                }//if-else
            }//onAnimationEnd
        });//addListener
        mAnim.start();
    }//start

    /**
     * tell if the animation is running
     * @return true if the animation is running
     */
    public boolean isRunning(){
        return mCurrentAnimationCount!=0 || mAnim.isStarted();
    }//isRunning

}//RepeatAnimator
