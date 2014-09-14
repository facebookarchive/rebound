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

public class SpringChain implements SpringListener {

  private final SpringSystem mSpringSystem;
  private final CopyOnWriteArrayList<SpringListener> mListeners;
  private final CopyOnWriteArrayList<Spring> mSprings;
  private static final SpringConfig ATTACHMENT_SPRING = SpringConfig.fromOrigamiTensionAndFriction(70, 10);
  private static final SpringConfig MAIN_SPRING = SpringConfig.fromOrigamiTensionAndFriction(40, 6);
  private int mControlSpringIndex;

  static {
    SpringConfigRegistry.getInstance().addSpringConfig(MAIN_SPRING, "main spring");
    SpringConfigRegistry.getInstance().addSpringConfig(ATTACHMENT_SPRING, "attachment spring");
  }

  public static SpringChain create() {
    return new SpringChain();
  }

  public SpringChain() {
    mSpringSystem = SpringSystem.create();
    mListeners = new CopyOnWriteArrayList<SpringListener>();
    mSprings = new CopyOnWriteArrayList<Spring>();
  }

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
        .setSpringConfig(ATTACHMENT_SPRING);
    mSprings.add(spring);
    mListeners.add(listener);
    return this;
  }

  /**
   * Set the index of the control spring. This spring will drive the positions of all the springs
   * before and after it in the list when moved.
   * @param i the index to use for the control spring
   * @return this SpringChain for chaining
   */
  public SpringChain setControlSpringIndex(int i) {
    mControlSpringIndex = i;
    for (Spring spring : mSpringSystem.getAllSprings()) {
      spring.setSpringConfig(ATTACHMENT_SPRING);
    }
    getControlSpring().setSpringConfig(MAIN_SPRING);
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
