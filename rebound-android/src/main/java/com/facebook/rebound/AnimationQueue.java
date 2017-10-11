/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 */

package com.facebook.rebound;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * AnimationQueue provides a way to trigger a delayed stream of animations off of a stream of
 * values. Each callback that is added the AnimationQueue will be process the stream delayed by
 * the number of animation frames equal to its position in the callback list. This makes it easy
 * to build cascading animations.
 *
 * TODO: Add options for changing the delay after which a callback receives a value from the
 *       animation queue value stream.
 */
public class AnimationQueue {

  /**
   * AnimationQueue.Callback receives the value from the stream that it should use in its onFrame
   * method.
   */
  public interface Callback {
    void onFrame(Double value);
  }

  private final ChoreographerCompat mChoreographer;
  private final Queue<Double> mPendingQueue = new LinkedList<Double>();
  private final Queue<Double> mAnimationQueue = new LinkedList<Double>();
  private final List<Callback> mCallbacks = new ArrayList<Callback>();
  private final ArrayList<Double> mTempValues = new ArrayList<Double>();
  private final ChoreographerCompat.FrameCallback mChoreographerCallback;
  private boolean mRunning;

  public AnimationQueue() {
    mChoreographer = ChoreographerCompat.getInstance();
    mChoreographerCallback = new ChoreographerCompat.FrameCallback() {
      @Override
      public void doFrame(long frameTimeNanos) {
        onFrame(frameTimeNanos);
      }
    };
  }

  /* Values */

  /**
   * Add a single value to the pending animation queue.
   * @param value the single value to add
   */
  public void addValue(Double value) {
    mPendingQueue.add(value);
    runIfIdle();
  }

  /**
   * Add a collection of values to the pending animation value queue
   * @param values the collection of values to add
   */
  public void addAllValues(Collection<Double> values) {
    mPendingQueue.addAll(values);
    runIfIdle();
  }

  /**
   * Clear all pending animation values.
   */
  public void clearValues() {
    mPendingQueue.clear();
  }

  /* Callbacks */

  /**
   * Add a callback to the AnimationQueue.
   * @param callback the callback to add
   */
  public void addCallback(Callback callback) {
    mCallbacks.add(callback);
  }

  /**
   * Remove the specified callback from the AnimationQueue.
   * @param callback the callback to remove
   */
  public void removeCallback(Callback callback) {
    mCallbacks.remove(callback);
  }

  /**
   * Remove any callbacks from the AnimationQueue.
   */
  public void clearCallbacks() {
    mCallbacks.clear();
  }

  /**
   * Start the animation loop if it is not currently running.
   */
  private void runIfIdle() {
    if (!mRunning) {
      mRunning = true;
      mChoreographer.postFrameCallback(mChoreographerCallback);
    }
  }

  /**
   * Called every time a new frame is ready to be rendered.
   *
   * Values are processed FIFO and each callback is given a chance to handle each value when its
   * turn comes before a value is poll'd off the AnimationQueue.
   *
   * @param frameTimeNanos The time in nanoseconds when the frame started being rendered, in the
   *                       nanoTime() timebase. Divide this value by 1000000 to convert it to the
   *                       uptimeMillis() time base.
   */
  private void onFrame(long frameTimeNanos) {
    Double nextPendingValue = mPendingQueue.poll();

    int drainingOffset;
    if (nextPendingValue != null) {
      mAnimationQueue.offer(nextPendingValue);
      drainingOffset = 0;
    } else {
      drainingOffset = Math.max(mCallbacks.size() - mAnimationQueue.size(), 0);
    }

    // Copy the values into a temporary ArrayList for processing.
    mTempValues.addAll(mAnimationQueue);
    for (int i = mTempValues.size() - 1; i > -1; i--) {
      Double val = mTempValues.get(i);
      int cbIdx = mTempValues.size() - 1 - i + drainingOffset;
      if (mCallbacks.size() > cbIdx) {
        mCallbacks.get(cbIdx).onFrame(val);
      }
    }
    mTempValues.clear();

    while (mAnimationQueue.size() + drainingOffset >= mCallbacks.size()) {
      mAnimationQueue.poll();
    }

    if (mAnimationQueue.isEmpty() && mPendingQueue.isEmpty()) {
      mRunning = false;
    } else {
      mChoreographer.postFrameCallback(mChoreographerCallback);
    }
  }

}

