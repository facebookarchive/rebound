package com.facebook.rebound;

/**
 * The spring choreographer is a platform-dependent system that knows how to run the spring
 * system's logic on every display frame.
 */
public interface SpringChoreographer {

  /**
   * The SpringSystem has requested that the choreographer begins running this {@link Runnable}
   * on every frame. The {@link Runnable} will continue running on every frame until
   * {@link #stop()} is called.
   * If an existing {@link Runnable} had been started on this choreographer, it will be cancelled.
   */
  void start(Runnable runnable);

  /**
   * The choreographer will no longer run the {@link Runnable}.
   */
  void stop();
}
