package com.sdcz.endpass.callback;

import android.transition.Transition;

/**
 * @Description     动画执行监听
 */
public interface PopupTransitionListener extends Transition.TransitionListener {

    @Override
    default void onTransitionStart(Transition transition) {

    }

    @Override
    default void onTransitionEnd(Transition transition) {

    }

    @Override
    default void onTransitionCancel(Transition transition) {

    }

    @Override
    default void onTransitionPause(Transition transition) {

    }

    @Override
    default void onTransitionResume(Transition transition) {

    }
}
