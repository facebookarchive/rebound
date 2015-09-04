/*
 *  Copyright (c) 2015, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.facebook.rebound;

import android.view.View;
import java.util.ArrayList;
import java.util.WeakHashMap;

/**
 * Spring Listener with Hardware Layer implementation, it's useful if
 * you use with complex transition in your views
 */
public class HardwareLayerSpringListener implements SpringListener {

  /**
   * I think that WeakHashMap can avoid any memory leak, needs to be tested...
   */
  private WeakHashMap<Integer, View> weakViews = new WeakHashMap<>();

  public HardwareLayerSpringListener(View... views) {
    addViews(views);
  }

  public HardwareLayerSpringListener(WeakHashMap<Integer, View> weakViews) {
    this.weakViews = weakViews;
  }

  public void addViews(ArrayList<View> views) {
    int countViews = views.size();
    for(int i = 0; i < countViews; i++) {
      addView(i, views.get(i));
    }
  }

  public void addViews(WeakHashMap<Integer, View> weakViews) {
    int countViews = weakViews.size();
    for(int i = 0; i < countViews; i++) {
      addView(i, weakViews.get(i));
    }
  }

  public void addViews(View... views) {
    int countViews = views.length;
    for(int i = 0; i < countViews; i++) {
      addView(i, views[i]);
    }
  }

  public void addView(int index, View view) {
    weakViews.put(index, view);
  }

  @Override
  public void onSpringUpdate(Spring spring) {
  }

  /**
   * Removes the all views of the Hardware Layer
   *
   * @param spring the spring that's now resting
   */
  @Override
  public void onSpringAtRest(Spring spring) {
    for(View view : weakViews.values()) {
      view.setLayerType(View.LAYER_TYPE_NONE, null);
    }
  }

  /**
   * Set the layer type of the all views to View.LAYER_TYPE_HARDWARE
   *
   * @param spring the spring that has left its resting state
   */
  @Override
  public void onSpringActivate(Spring spring) {
    for(View view : weakViews.values()) {
      view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
  }

  @Override
  public void onSpringEndStateChange(Spring spring) {
  }

}
