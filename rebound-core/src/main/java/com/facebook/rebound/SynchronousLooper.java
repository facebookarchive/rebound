/**
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.rebound;

public class SynchronousLooper extends SpringLooper {

  public static double SIXTY_FPS = 16.6667;
  private double mTimeStep;
  private boolean mRunning;

  public SynchronousLooper() {
    mTimeStep = SIXTY_FPS;
  }

  public double getTimeStep() {
    return mTimeStep;
  }

  public void setTimeStep(double timeStep) {
    mTimeStep = timeStep;
  }

  @Override
  public void start() {
    mRunning = true;
    while (!mSpringSystem.getIsIdle()) {
      if (mRunning == false) {
        break;
      }
      mSpringSystem.loop(mTimeStep);
    }
  }

  @Override
  public void stop() {
    mRunning = false;
  }
}

