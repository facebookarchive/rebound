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

/**
 * The spring looper is an interface for implementing platform-dependent run loops.
 */
public abstract class SpringLooper {

  protected BaseSpringSystem mSpringSystem;

  /**
   * Set the BaseSpringSystem that the SpringLooper will call back to.
   * @param springSystem the spring system to call loop on.
   */
  public void setSpringSystem(BaseSpringSystem springSystem) {
    mSpringSystem = springSystem;
  }

  /**
   * The BaseSpringSystem has requested that the looper begins running this {@link Runnable}
   * on every frame. The {@link Runnable} will continue running on every frame until
   * {@link #stop()} is called.
   * If an existing {@link Runnable} had been started on this looper, it will be cancelled.
   */
  public abstract void start();

  /**
   * The looper will no longer run the {@link Runnable}.
   */
  public abstract void stop();
}