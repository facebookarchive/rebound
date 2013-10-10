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

public class SpringUtil {

  /**
   * Map a value within a given range to another range.
   * @param value the value to map
   * @param fromLow the low end of the range the value is within
   * @param fromHigh the high end of the range the value is within
   * @param toLow the low end of the range to map to
   * @param toHigh the high end of the range to map to
   * @return the mapped value
   */
  public static double mapValueFromRangeToRange(
      double value,
      double fromLow,
      double fromHigh,
      double toLow,
      double toHigh) {
    double fromRangeSize = fromHigh - fromLow;
    double toRangeSize = toHigh - toLow;
    double valueScale = (value - fromLow) / fromRangeSize;
    return toLow + (valueScale * toRangeSize);
  }

  /**
   * Clamp a value to be within the provided range.
   * @param value the value to clamp
   * @param low the low end of the range
   * @param high the high end of the range
   * @return the clamped value
   */
  public static double clamp(double value, double low, double high) {
    return Math.min(Math.max(value, low), high);
  }
}

