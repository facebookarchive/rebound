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
 * Data structure for storing spring configuration.
 */
public class SpringConfig {
  public double friction;
  public double tension;

  public static SpringConfig defaultConfig = SpringConfig.fromOrigamiTensionAndFriction(40, 7);

  /**
   * constructor for the SpringConfig
   * @param tension tension value for the SpringConfig
   * @param friction friction value for the SpringConfig
   */
  public SpringConfig(double tension, double friction) {
    this.tension = tension;
    this.friction = friction;
  }

  /**
   * A helper to make creating a SpringConfig easier with values mapping to the Origami values.
   * @param qcTension tension as defined in the Quartz Composition
   * @param qcFriction friction as defined in the Quartz Composition
   * @return a SpringConfig that maps to these values
   */
  public static SpringConfig fromOrigamiTensionAndFriction(double qcTension, double qcFriction) {
    return new SpringConfig(
        OrigamiValueConverter.tensionFromOrigamiValue(qcTension),
        OrigamiValueConverter.frictionFromOrigamiValue(qcFriction)
    );
  }

  /**
   * Map values from the Origami POP Animation patch, which are based on a bounciness and speed
   * value.
   * @param bounciness bounciness of the POP Animation
   * @param speed speed of the POP Animation
   * @return a SpringConfig mapping to the specified POP Animation values.
   */
  public static SpringConfig fromBouncinessAndSpeed(double bounciness, double speed) {
    BouncyConversion bouncyConversion = new BouncyConversion(speed, bounciness);
    return fromOrigamiTensionAndFriction(
        bouncyConversion.getBouncyTension(),
        bouncyConversion.getBouncyFriction());
  }
}
