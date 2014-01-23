/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.facebook.rebound.android;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.view.Choreographer;
import com.facebook.rebound.SpringLooper;

/**
 * Android version of the spring looper. Uses the most appropriate frame callback mechanism
 * available. It uses Android's {@link Choreographer} when available, otherwise it uses a
 * {@link Handler}.
 */
public class AndroidSpringLooper implements SpringLooper {

  /**
   * The base implementation of the Android spring looper, using a {@link Handler} for the
   * frame callbacks.
   */
  private static class AndroidSpringLooperBase implements SpringLooper {

    private final Handler mHandler;
    private Runnable mLooperRunnable;

    /**
     * @return an Android spring looper using a new {@link Handler} instance
     */
    public static AndroidSpringLooperBase create() {
      return new AndroidSpringLooperBase(new Handler());
    }

    public AndroidSpringLooperBase(Handler handler) {
      mHandler = handler;
    }

    @Override
    public void start(final Runnable runnable) {
      if (mLooperRunnable != null) {
        mHandler.removeCallbacks(mLooperRunnable);
      }
      mLooperRunnable = new Runnable() {
        @Override
        public void run() {
          runnable.run();
          if (mLooperRunnable != null) {
            mHandler.post(mLooperRunnable);
          }
        }
      };
      mHandler.post(mLooperRunnable);
    }

    @Override
    public void stop() {
      if (mLooperRunnable != null) {
        mHandler.removeCallbacks(mLooperRunnable);
        mLooperRunnable = null;
      }
    }
  }

  /**
   * The Jelly Bean and up implementation of the spring looper that uses Android's
   * {@link Choreographer} instead of a {@link Handler}
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private static class AndroidSpringLooperJB implements SpringLooper {

    private final Choreographer mChoreographer;
    private Choreographer.FrameCallback mFrameCallback;

    /**
     * @return an Android spring choreographer using the system {@link Choreographer}
     */
    public static AndroidSpringLooperJB create() {
      return new AndroidSpringLooperJB(Choreographer.getInstance());
    }

    public AndroidSpringLooperJB(Choreographer choreographer) {
      mChoreographer = choreographer;
    }

    @Override
    public void start(final Runnable runnable) {
      if (mFrameCallback != null) {
        mChoreographer.removeFrameCallback(mFrameCallback);
      }
      mFrameCallback = new Choreographer.FrameCallback() {
        @Override
        public void doFrame(long frameTimeNanos) {
          runnable.run();
          if (mFrameCallback != null) {
            mChoreographer.postFrameCallback(mFrameCallback);
          }
        }
      };
      mChoreographer.postFrameCallback(mFrameCallback);
    }

    @Override
    public void stop() {
      if (mFrameCallback != null) {
        mChoreographer.removeFrameCallback(mFrameCallback);
        mFrameCallback = null;
      }
    }
  }

  private final SpringLooper mImpl;

  public AndroidSpringLooper() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      mImpl = AndroidSpringLooperJB.create();
    } else {
      mImpl = AndroidSpringLooperBase.create();
    }
  }

  @Override
  public void start(Runnable runnable) {
    mImpl.start(runnable);
  }

  @Override
  public void stop() {
    mImpl.stop();
  }
}
