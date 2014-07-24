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

import android.os.SystemClock;

class AndroidClock implements SpringClock {
  @Override
  public long now() {
    return SystemClock.uptimeMillis();
  }
}
