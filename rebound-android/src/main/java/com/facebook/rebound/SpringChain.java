/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.rebound;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * SpringChain is a helper class for creating spring animations with multiple springs in a chain.
 * Chains of springs can be used to create cascading animations that maintain individual physics
 * state for each member of the chain. One spring in the chain is chosen to be the control spring.
 * Springs before and after the control spring in the chain are pulled along by their predecessor.
 * You can change which spring is the control spring at any point by calling
 * {@link SpringChain#setControlSpringIndex(int)}.
 */
public class SpringChain implements SpringListener {

  // The main spring config defines the tension and friction for the control spring. Keeping these
  // values separate allows the behavior of the trailing springs to be different than that of the
  // control point.
  private static final SpringConfig MAIN_SPRING_CONFIG =
      SpringConfig.fromOrigamiTensionAndFriction(40, 6);
  // The attachment spring config defines the tension and friction for the rest of the springs in
  // the chain.
  private static final SpringConfig ATTACHMENT_SPRING_CONFIG =
      SpringConfig.fromOrigamiTensionAndFriction(70, 10);

  /**
   * Add these spring configs to the registry to support live tuning through the
   * {@link com.facebook.rebound.ui.SpringConfiguratorView}
   */
  static {
    SpringConfigRegistry registry = SpringConfigRegistry.getInstance();
    registry.addSpringConfig(MAIN_SPRING_CONFIG, "main spring");
    registry.addSpringConfig(ATTACHMENT_SPRING_CONFIG, "attachment spring");
  }

  /**
   * Static factor method for creating a new SpringChain.
   * @return the newly created SpringChain
   */
  public static SpringChain create() {
    return new SpringChain();
  }

  private final SpringSystem mSpringSystem = SpringSystem.create();
  private final CopyOnWriteArrayList<SpringListener> mListeners =
      new CopyOnWriteArrayList<SpringListener>();
  private final CopyOnWriteArrayList<Spring> mSprings = new CopyOnWriteArrayList<Spring>();
  private int mControlSpringIndex = -1;

  /**
   * Add a spring to the chain that will callback to the provided listener.
   * @param listener the listener to notify for this Spring in the chain
   * @return this SpringChain for chaining
   */
  public SpringChain addSpring(final SpringListener listener) {
    // We listen to each spring added to the SpringChain and dynamically chain the springs together
    // whenever the control spring state is modified.
    Spring spring = mSpringSystem
        .createSpring()
        .addListener(this)
        .setSpringConfig(ATTACHMENT_SPRING_CONFIG);
    mSprings.add(spring);
    mListeners.add(listener);
    return this;
  }

  /**
   * Set the index of the control spring. This spring will drive the positions of all the springs
   * before and after it in the list when moved.
   * @param i the index to use for the control spring
   * @return this SpringChain
   */
  public SpringChain setControlSpringIndex(int i) {
    mControlSpringIndex = i;
    Spring controlSpring = mSprings.get(mControlSpringIndex);
    if (controlSpring == null) {
      return null;
    }
    for (Spring spring : mSpringSystem.getAllSprings()) {
      spring.setSpringConfig(ATTACHMENT_SPRING_CONFIG);
    }
    getControlSpring().setSpringConfig(MAIN_SPRING_CONFIG);
    return this;
  }

  /**
   * Retrieve the control spring so you can manipulate it to drive the positions of the other
   * springs.
   * @return the control spring.
   */
  public Spring getControlSpring() {
    return mSprings.get(mControlSpringIndex);
  }

  /**
   * Retrieve the list of springs in the chain.
   * @return the list of springs
   */
  public List<Spring> getAllSprings() {
    return mSprings;
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    // Get the control spring index and update the endValue of each spring above and below it in the
    // spring collection triggering a cascading effect.
    int idx = mSprings.indexOf(spring);
    SpringListener listener = mListeners.get(idx);
    int above = -1;
    int below = -1;
    if (idx == mControlSpringIndex) {
      below = idx - 1;
      above = idx + 1;
    } else if (idx < mControlSpringIndex) {
      below = idx - 1;
    } else if (idx > mControlSpringIndex) {
      above = idx + 1;
    }
    if (above > -1 && above < mSprings.size()) {
      mSprings.get(above).setEndValue(spring.getCurrentValue());
    }
    if (below > -1 && below < mSprings.size()) {
      mSprings.get(below).setEndValue(spring.getCurrentValue());
    }
    listener.onSpringUpdate(spring);
  }

  @Override
  public void onSpringAtRest(Spring spring) {
    int idx = mSprings.indexOf(spring);
    mListeners.get(idx).onSpringAtRest(spring);
  }

  @Override
  public void onSpringActivate(Spring spring) {
    int idx = mSprings.indexOf(spring);
    mListeners.get(idx).onSpringActivate(spring);
  }

  @Override
  public void onSpringEndStateChange(Spring spring) {
    int idx = mSprings.indexOf(spring);
    mListeners.get(idx).onSpringEndStateChange(spring);
  }
}
