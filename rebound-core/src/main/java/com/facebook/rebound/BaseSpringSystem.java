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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * BaseSpringSystem maintains the set of springs within an Application context. It is responsible for
 * Running the spring integration loop and maintains a registry of all the Springs it solves for.
 * In addition to listening to physics events on the individual Springs in the system, listeners
 * can be added to the BaseSpringSystem itself to provide pre and post integration setup.
 */
public class BaseSpringSystem {

  private final Map<String, Spring> mSpringRegistry = new HashMap<String, Spring>();
  private final Set<Spring> mActiveSprings =
      Collections.newSetFromMap(new ConcurrentHashMap<Spring, Boolean>());
  private final SpringClock mClock;
  private final SpringLooper mSpringLooper;
  private long mLastTimeMillis = -1;
  private ReentrantCallback<SpringSystemListener> mListeners = new ReentrantCallback<SpringSystemListener>();
  private boolean mIdle = true;

  /**
   * create a new BaseSpringSystem
   * @param clock parameterized Clock to allow testability of the physics loop
   * @param springLooper parameterized springLooper to allow testability of the
   *        physics loop
   */
  public BaseSpringSystem(SpringClock clock, SpringLooper springLooper) {
    if (clock == null) {
      throw new IllegalArgumentException("clock is required");
    }
    if (springLooper == null) {
      throw new IllegalArgumentException("springLooper is required");
    }
    mClock = clock;
    mSpringLooper = springLooper;
    mSpringLooper.setSpringSystem(this);
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
   * get a spring by name
   * @param id id of the spring to retrieve
   * @return Spring with the specified key
   */
  public Spring getSpringById(String id) {
    if (id == null) {
      throw new IllegalArgumentException("id is required");
    }
    return mSpringRegistry.get(id);
  }

  /**
   * return all the springs in the simulator
   * @return all the springs
   */
  public List<Spring> getAllSprings() {
    Collection<Spring> collection = mSpringRegistry.values();
    List<Spring> list;
    if (collection instanceof List) {
      list = (List<Spring>)collection;
    } else {
      list = new ArrayList<Spring>(collection);
    }
    return Collections.unmodifiableList(list);
  }

  /**
   * Registers a Spring to this BaseSpringSystem so it can be iterated if active.
   * @param spring the Spring to register
   */
  void registerSpring(Spring spring) {
    if (spring == null) {
      throw new IllegalArgumentException("spring is required");
    }
    if (mSpringRegistry.containsKey(spring.getId())) {
      throw new IllegalArgumentException("spring is already registered");
    }
    mSpringRegistry.put(spring.getId(), spring);
  }

  /**
   * Deregisters a Spring from this BaseSpringSystem, so it won't be iterated anymore. The Spring should
   * not be used anymore after doing this.
   *
   * @param spring the Spring to deregister
   */
  void deregisterSpring(Spring spring) {
    if (spring == null) {
      throw new IllegalArgumentException("spring is required");
    }
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
  public void loop() {
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

    if (mIdle) {
      mSpringLooper.stop();
    }
  }

  /**
   * This is used internally by the {@link Spring}s created by this {@link BaseSpringSystem} to notify
   * it has reached a state where it needs to be iterated. This will add the spring to the list of
   * active springs on this system and start the iteration if the system was idle before this call.
   * @param springId the id of the Spring to be activated
   */
  void activateSpring(String springId) {
    Spring spring = mSpringRegistry.get(springId);
    if (spring == null) {
      throw new IllegalArgumentException("springId " + springId + " does not reference a registered spring");
    }
    synchronized (this) {
      mActiveSprings.add(spring);
      if (getIsIdle()) {
        mIdle = false;
        mSpringLooper.start();
      }
    }
  }

  /** listeners **/

  public void addListener(SpringSystemListener newListener) {
    if (newListener == null) {
      throw new IllegalArgumentException("newListener is required");
    }
    mListeners.addListener(newListener);
  }

  public void removeListener(SpringSystemListener listenerToRemove) {
    if (listenerToRemove == null) {
      throw new IllegalArgumentException("listenerToRemove is required");
    }
    mListeners.removeListener(listenerToRemove);
  }

  public void removeAllListeners() {
    mListeners.clear();
  }
}


