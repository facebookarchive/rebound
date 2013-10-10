/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.facebook.rebound;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.view.Choreographer;

/**
 * Wrapper class for abstracting away availability of the JellyBean Choreographer. If Choreographer
 * is unavailable we fallback to using a normal Handler.
 */
public class ChoreographerWrapper {

  private static boolean IS_JELLYBEAN_OR_HIGHER =
      Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
  private static final long ONE_FRAME_MILLIS = 17;

  private Handler mHandler;
  private Choreographer mChoreographer;

  public ChoreographerWrapper() {
    if (IS_JELLYBEAN_OR_HIGHER) {
      mChoreographer = getChoreographer();
    } else {
      mHandler = new Handler();
    }
  }

  public void postFrameCallback(FrameCallbackWrapper callbackWrapper) {
    if (IS_JELLYBEAN_OR_HIGHER) {
      choreographerPostFrameCallback(callbackWrapper.getFrameCallback());
    } else {
      mHandler.postDelayed(callbackWrapper.getRunnable(), 0);
    }
  }

  public void postFrameCallbackDelayed(FrameCallbackWrapper callbackWrapper, long delayMillis) {
    if (IS_JELLYBEAN_OR_HIGHER) {
      choreographerPostFrameCallbackDelayed(callbackWrapper.getFrameCallback(), delayMillis);
    } else {
      mHandler.postDelayed(callbackWrapper.getRunnable(), delayMillis + ONE_FRAME_MILLIS);
    }
  }

  public void removeFrameCallback(FrameCallbackWrapper callbackWrapper) {
    if (IS_JELLYBEAN_OR_HIGHER) {
      choreographerRemoveFrameCallback(callbackWrapper.getFrameCallback());
    } else {
      mHandler.removeCallbacks(callbackWrapper.getRunnable());
    }
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private Choreographer getChoreographer() {
    return Choreographer.getInstance();
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private void choreographerPostFrameCallback(Choreographer.FrameCallback frameCallback) {
    mChoreographer.postFrameCallback(frameCallback);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private void choreographerPostFrameCallbackDelayed(
      Choreographer.FrameCallback frameCallback,
      long delayMillis) {
    mChoreographer.postFrameCallbackDelayed(frameCallback, delayMillis);
  }

  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private void choreographerRemoveFrameCallback(Choreographer.FrameCallback frameCallback) {
    mChoreographer.removeFrameCallback(frameCallback);
  }
}
