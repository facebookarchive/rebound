package com.facebook.rebound;

/**
 * The spring looper is a platform-dependent system that knows how and when to run the spring
 * system's update logic.
 */
public interface SpringLooper {

  /**
   * The SpringSystem has requested that the looper begins running this {@link Runnable}
   * on every frame. The {@link Runnable} will continue running on every frame until
   * {@link #stop()} is called.
   * If an existing {@link Runnable} had been started on this looper, it will be cancelled.
   */
  void start(Runnable runnable);

  /**
   * The looper will no longer run the {@link Runnable}.
   */
  void stop();
}
