package com.facebook.rebound.android;

import com.facebook.rebound.SpringClock;
import com.facebook.rebound.SpringSystem;

/**
 *
 */
public abstract class AndroidSpringSystem {

  public static SpringSystem create() {
    return new SpringSystem(new SpringClock(), new AndroidSpringLooper());
  }
}
