package com.facebook.rebound;

import android.view.View;
import java.util.WeakHashMap;

public class HardwareLayerSpringListener implements SpringListener {

  private WeakHashMap<Integer, View> weakViews = new WeakHashMap<>();

  public HardwareLayerSpringListener(View... views) {
    int countViews = views.length;
    for(int i = 0; i < countViews; i++) {
      weakViews.put(i, views[i]);
    }
  }

  public HardwareLayerSpringListener(WeakHashMap<Integer, View> weakViews) {
    this.weakViews = weakViews;
  }

  @Override
  public void onSpringUpdate(Spring spring) {
  }

  @Override
  public void onSpringAtRest(Spring spring) {
    for(View view : weakViews.values()) {
      view.setLayerType(View.LAYER_TYPE_NONE, null);
    }
  }

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
