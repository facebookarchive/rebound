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

import com.facebook.rebound.android.AndroidSpringChoreographer;

import java.util.*;
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
      Collections.newSetFromMap(new ConcurrentHashMap<Spring, Boolean>());
  private final SpringChoreographer mSpringChoreographer;
  private final SpringClock mClock;
  private long mLastTimeMillis = -1;
  private ReentrantCallback<SpringSystemListener> mListeners = new ReentrantCallback<SpringSystemListener>();
  private boolean mIdle = true;

  /**
   * Create a SpringSystem with dependencies
   * @return a new SpringSystem
   */
  public static SpringSystem create() {
    return new SpringSystem(new SpringClock(), new AndroidSpringChoreographer());
  }

  /**
   * create a new SpringSystem
   * @param clock parameterized Clock to allow testability of the physics loop
   * @param springChoreographer parameterized springChoreographer to allow testability of the
   *        physics loop
   */
  public SpringSystem(
      SpringClock clock,
      SpringChoreographer springChoreographer) {
    if (clock == null) {
      throw new IllegalArgumentException("clock is required");
    }
    if (springChoreographer == null) {
      throw new IllegalArgumentException("springChoreographer is required");
    }
    mClock = clock;
    mSpringChoreographer = springChoreographer;
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
   * Registers a Spring to this SpringSystem so it can be iterated if active.
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
   * Deregisters a Spring from this SpringSystem, so it won't be iterated anymore. The Spring should
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

    if (mIdle) {
      mSpringChoreographer.stop();
    }
  }

  /**
   * This is used internally by the {@link Spring}s created by this {@link SpringSystem} to notify
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
        mSpringChoreographer.start(new Runnable() {
          @Override
          public void run() {
            loop();
          }
        });
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


