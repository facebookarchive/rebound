package com.facebook.rebound.android;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Handler;
import android.view.Choreographer;
import com.facebook.rebound.SpringChoreographer;

/**
 * Android version of the spring choreographer. Uses the most appropriate frame callback mechanism
 * available. It uses Android's {@link Choreographer} when available, otherwise it uses a
 * {@link Handler}.
 */
public class AndroidSpringChoreographer implements SpringChoreographer {

  /**
   * The base implementation of the Android spring choreographer, using a {@link Handler} for the
   * frame callbacks.
   */
  private static class AndroidSpringChoreographerBase implements SpringChoreographer {

    private final Handler mHandler;
    private Runnable mChoreographerRunnable;

    /**
     * @return an Android spring choreographer using a new {@link Handler} instance
     */
    public static AndroidSpringChoreographerBase create() {
      return new AndroidSpringChoreographerBase(new Handler());
    }

    public AndroidSpringChoreographerBase(Handler handler) {
      mHandler = handler;
    }

    @Override
    public void start(final Runnable runnable) {
      if (mChoreographerRunnable != null) {
        mHandler.removeCallbacks(mChoreographerRunnable);
      }
      mChoreographerRunnable = new Runnable() {
        @Override
        public void run() {
          runnable.run();
          if (mChoreographerRunnable != null) {
            mHandler.post(mChoreographerRunnable);
          }
        }
      };
      mHandler.post(mChoreographerRunnable);
    }

    @Override
    public void stop() {
      if (mChoreographerRunnable != null) {
        mHandler.removeCallbacks(mChoreographerRunnable);
        mChoreographerRunnable = null;
      }
    }
  }

  /**
   * The Jelly Bean and up implementation of the spring choreographer that uses Android's
   * {@link Choreographer} instead of a {@link Handler}
   */
  @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
  private static class AndroidSpringChoreographerJB implements SpringChoreographer {

    private final Choreographer mChoreographer;
    private Choreographer.FrameCallback mFrameCallback;

    /**
     * @return an Android spring choreographer using the system {@link Choreographer}
     */
    public static AndroidSpringChoreographerJB create() {
      return new AndroidSpringChoreographerJB(Choreographer.getInstance());
    }

    public AndroidSpringChoreographerJB(Choreographer choreographer) {
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

  private final SpringChoreographer mImpl;

  public AndroidSpringChoreographer() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
      mImpl = AndroidSpringChoreographerJB.create();
    } else {
      mImpl = AndroidSpringChoreographerBase.create();
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
