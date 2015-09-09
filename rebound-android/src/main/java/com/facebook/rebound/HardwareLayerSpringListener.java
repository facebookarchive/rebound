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
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Spring Listener with Hardware Layer implementation, it's useful if
 * you use with complex transition in your views.
 */
public class HardwareLayerSpringListener implements SpringListener {

  /**
   * WeakReference can avoid any memory leak, needs to be tested.
   */
  private List<WeakReference<View>> weakViews = new ArrayList<>();

  public HardwareLayerSpringListener(View... views) {
    for(View view : views) {
      weakViews.add(new WeakReference<>(view));
    }
  }

  @Override
  public void onSpringUpdate(Spring spring) {
  }

  /**
   * Removes the all views of the Hardware Layer.
   *
   * @param spring the spring that's now resting.
   */
  @Override
  public void onSpringAtRest(Spring spring) {
    for(WeakReference weakReference : weakViews) {
      ((View) weakReference.get()).setLayerType(View.LAYER_TYPE_NONE, null);
    }
  }

  /**
   * Set the layer type of the all views to Hardware Layer
   *
   * @param spring the spring that has left its resting state.
   */
  @Override
  public void onSpringActivate(Spring spring) {
    for(WeakReference weakReference : weakViews) {
      ((View) weakReference.get()).setLayerType(View.LAYER_TYPE_HARDWARE, null);
    }
  }

  @Override
  public void onSpringEndStateChange(Spring spring) {
  }

}
