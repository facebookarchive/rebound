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


import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Because Java doesn't permit additions/removals to a list while iterating, a callback cannot
 * remove itself when it is being called.
 *
 * This class makes it safe for Callbacks to remove themselves. Also, it is thread-safe.
 *
 * Implementation detail: This class works by storing an inner set that will be modified by the
 * calls to add/removeListener. When getListeners() or iterator() is called, an immutable copy from
 * the inner set is returned. That copy will be reused if no changes have been done to the set since
 * last request for it.
 */
public class ReentrantCallback<CallbackClass> implements Iterable<CallbackClass> {
  private final Set<CallbackClass> mListeners;
  private Set<CallbackClass> mReturnSet;

  /**
   * Creates a reentrant set of callbacks.
   */
  public ReentrantCallback() {
    mListeners = new HashSet<CallbackClass>();
    mReturnSet = null;
  }

  /**
   * Returns an immutable set of Callbacks.
   */
  public synchronized Set<CallbackClass> getListeners() {
    if (mReturnSet == null) {
      mReturnSet = Collections.unmodifiableSet(mListeners);
    }
    return mReturnSet;
  }

  @Override
  public synchronized Iterator<CallbackClass> iterator() {
    if (mReturnSet == null) {
      mReturnSet = Collections.unmodifiableSet(mListeners);
    }
    return mReturnSet.iterator();
  }

  /**
   * Add a listener
   *
   * @param listener The listener
   */
  public synchronized void addListener(CallbackClass listener) {
    mListeners.add(listener);
    mReturnSet = null;
  }

  /**
   * Remove a listener
   *
   * @param listener The listener
   */
  public synchronized void removeListener(CallbackClass listener) {
    mListeners.remove(listener);
    mReturnSet = null;
  }

  /**
   * Gets a count of all listeners.
   */
  public synchronized int count() {
    return mListeners.size();
  }

  /**
   * Clears all listeners
   */
  public synchronized void clear() {
    mListeners.clear();
    mReturnSet = null;
  }
}
