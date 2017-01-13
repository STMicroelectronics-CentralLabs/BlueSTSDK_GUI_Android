/*******************************************************************************
 * COPYRIGHT(c) 2016 STMicroelectronics
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *   1. Redistributions of source code must retain the above copyright notice,
 *      this list of conditions and the following disclaimer.
 *   2. Redistributions in binary form must reproduce the above copyright notice,
 *      this list of conditions and the following disclaimer in the documentation
 *      and/or other materials provided with the distribution.
 *   3. Neither the name of STMicroelectronics nor the names of its contributors
 *      may be used to endorse or promote products derived from this software
 *      without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 ******************************************************************************/
package com.st.BlueSTSDK.gui.util;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

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
     * change the view where the animation will be done
     * @param view view where apply the animation
     */
    public void  setTarget(View view){
        mAnim.setTarget(view);
    }

    /**
     * tell if the animation is running
     * @return true if the animation is running
     */
    public boolean isRunning(){
        return mCurrentAnimationCount!=0 || mAnim.isStarted();
    }//isRunning

}//RepeatAnimator
