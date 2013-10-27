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
 * SpringSystemFrameCallbackWrapper is a FrameCallbackWrapper that can be configured to work on a
 * specific SpringSystem. This improves testability by allowing an outside caller to manipulate the
 * PhysicsSystem that is acted on when resolving a non-idle system. Specifically this improves our
 * ability to use mock/spy objects to verify SpringSystem functionality while iterating.
 */
public class SpringSystemFrameCallbackWrapper extends FrameCallbackWrapper {

  private SpringSystem mSpringSystem;

  /**
   * Set a SpringSystem for the physics loop to run on.
   * @param springSystem the SpringSystem to use
   */
  public void setSpringSystem(SpringSystem springSystem) {
    mSpringSystem = springSystem;
  }

  /**
   * Run the PhysicsSystem run loop.
   */
  @Override
  public void doFrame(long frameTimeNanos) {
    mSpringSystem.loop();
  }
}

