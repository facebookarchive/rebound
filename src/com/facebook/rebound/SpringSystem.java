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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Sets;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * SpringSystem maintains the set of springs within an Application context. It is responsible for
 * Running the spring integration loop and maintains a registry of all the Springs it solves for.
 * In addition to listening to physics events on the individual Springs in the system, listeners
 * can be added to the SpringSystem itself to provide pre and post integration setup.
 */
public class SpringSystem {

  private final Map<String, Spring> mSpringRegistry = new HashMap<String, Spring>();
  private final Set<Spring> mActiveSprings =
      Sets.newSetFromMap(new ConcurrentHashMap<Spring, Boolean>());
  private final ChoreographerWrapper mChoreographer;
  private final SpringSystemFrameCallbackWrapper mLoopFrameCallback;
  private final SpringClock mClock;
  private long mLastTimeMillis = -1;
  private ReentrantCallback<SpringSystemListener> mListeners = new ReentrantCallback<SpringSystemListener>();
  private boolean mIdle = true;

  /**
   * Create a SpringSystem with dependencies
   * @return a new SpringSystem
   */
  public static SpringSystem create() {
    return new SpringSystem(
        new SpringClock(),
        new ChoreographerWrapper(),
        new SpringSystemFrameCallbackWrapper());
  }

  /**
   * create a new SpringSystem
   * @param clock parameterized Clock to allow testability of the physics loop
   * @param choreographerWrapper parameterized choreographerWrapper to allow testability of the
   *        physics loop
   * @param loopFrameCallback parameterized SpringSystemFrameCallbackWrapper to allow testability
   *        of the physics loop
   */
  public SpringSystem(
      SpringClock clock,
      ChoreographerWrapper choreographerWrapper,
      SpringSystemFrameCallbackWrapper loopFrameCallback) {
    mClock = Preconditions.checkNotNull(clock);
    mChoreographer = Preconditions.checkNotNull(choreographerWrapper);
    mLoopFrameCallback = Preconditions.checkNotNull(loopFrameCallback);
    mLoopFrameCallback.setSpringSystem(this);
  }

  /**
   * check if the system is idle
   * @return is the system idle
   */
  public boolean getIsIdle() {
    return mIdle;
  }

  /**
   * create a spring with a random uuid for its name.
   * @return the spring
   */
  public Spring createSpring() {
    Spring spring = new Spring(this);
    registerSpring(spring);
    return spring;
  }

  /**
   * Destroys a certain spring from this system.
   *
   * DEPRECATED: use {@link Spring#destroy()} instead
   *
   * @param spring the spring to be destroyed
   */
  @Deprecated
  public void destroySpring(Spring spring) {
    spring.destroy();
  }

  /**
   * get a spring by name
   * @param id id of the spring to retrieve
   * @return Spring with the specified key
   */
  public Spring getSpringById(String id) {
    return mSpringRegistry.get(Preconditions.checkNotNull(id));
  }

  /**
   * return all the springs in the simulator
   * @return all the springs
   */
  public ImmutableList<Spring> getAllSprings() {
    return ImmutableList.copyOf(mSpringRegistry.values());
  }

  /**
   * Registers a Spring to this SpringSystem so it can be iterated if active.
   * @param spring the Spring to register
   */
  @VisibleForTesting
  void registerSpring(Spring spring) {
    Preconditions.checkNotNull(spring);
    Preconditions.checkState(!mSpringRegistry.containsKey(spring.getId()));
    mSpringRegistry.put(spring.getId(), spring);
  }

  /**
   * Deregisters a Spring from this SpringSystem, so it won't be iterated anymore. The Spring should
   * not be used anymore after doing this.
   *
   * @param spring the Spring to deregister
   */
  @VisibleForTesting
  void deregisterSpring(Spring spring) {
    Preconditions.checkNotNull(spring);
    mActiveSprings.remove(spring);
    mSpringRegistry.remove(spring.getId());
  }

  /**
   * update the springs in the system
   * @param time system time millis
   * @param deltaTime delta since last update in millis
   */
  void advance(long time, long deltaTime) {
    for (Spring spring : mActiveSprings) {
      // advance time in seconds
      if (spring.systemShouldAdvance()) {
        spring.advance(time / 1000.0, deltaTime / 1000.0);
      } else {
        mActiveSprings.remove(spring);
      }
    }
  }

  /**
   * loop the system until idle
   */
  void loop() {
    long currentTimeMillis = mClock.now();
    if (mLastTimeMillis == -1) {
      mLastTimeMillis = currentTimeMillis - 1;
    }
    long ellapsedMillis = currentTimeMillis - mLastTimeMillis;
    mLastTimeMillis = currentTimeMillis;

    for (SpringSystemListener listener : mListeners) {
      listener.onBeforeIntegrate(this);
    }
    advance(currentTimeMillis, ellapsedMillis);
    synchronized (this) {
      if (mActiveSprings.size() == 0) {
        mIdle = true;
        mLastTimeMillis = -1;
      }
    }
    for (SpringSystemListener listener : mListeners) {
      listener.onAfterIntegrate(this);
    }

    mChoreographer.removeFrameCallback(mLoopFrameCallback);
    if (!mIdle) {
      mChoreographer.postFrameCallback(mLoopFrameCallback);
    }
  }

  /**
   * This is used internally by the {@link Spring}s created by this {@link SpringSystem} to notify
   * it has reached a state where it needs to be iterated. This will add the spring to the list of
   * active springs on this system and start the iteration if the system was idle before this call.
   * @param springId the id of the Spring to be activated
   */
  @VisibleForTesting
  void activateSpring(String springId) {
    Spring spring = mSpringRegistry.get(springId);
    Preconditions.checkNotNull(spring, "Tried to activate Spring with id " + springId +
        " not registered to SpringSystem.");
    synchronized (this) {
      mActiveSprings.add(spring);
      if (getIsIdle()) {
        mIdle = false;
        mChoreographer.postFrameCallback(mLoopFrameCallback);
      }
    }
  }

  /** listeners **/

  public void addListener(SpringSystemListener newListener) {
    Preconditions.checkNotNull(newListener);
    mListeners.addListener(newListener);
  }

  public void removeListener(SpringSystemListener listenerToRemove) {
    Preconditions.checkNotNull(listenerToRemove);
    mListeners.removeListener(listenerToRemove);
  }

  public void removeAllListeners() {
    mListeners.clear();
  }
}


