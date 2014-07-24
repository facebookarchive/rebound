package com.facebook.rebound;

/**
 * This is a wrapper for BaseSpringSystem that provides the convenience of automatically providing
 * the AndroidSpringLooper dependency in {@link SpringSystem#create}.
 */
public class SpringSystem extends BaseSpringSystem {

  /**
   * Create a new SpringSystem providing the appropriate constructor parameters to work properly
   * in an Android environment.
   * @return the SpringSystem
   */
  public static SpringSystem create() {
    return new SpringSystem(new AndroidClock(), AndroidSpringLooperFactory.createSpringLooper());
  }

  private SpringSystem(SpringClock clock, SpringLooper springLooper) {
    super(clock, springLooper);
  }

}