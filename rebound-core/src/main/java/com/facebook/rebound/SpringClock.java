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

public interface SpringClock {
  /**
   * The current time for the {@link BaseSpringSystem} to use when iterating over the springs.
   * Ideally this method would use a monotonic clock.
   * An example of this method would be:
   *
   * @Override
   * public long now() {
   *   return System.nanoTime() / 1000000;
   * }
   *
   * @return the current clock time in millis
   */
  long now();
}
