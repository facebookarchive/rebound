/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.facebook.rebound.example;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

public class ZoomFragment extends Fragment {

  private final BaseSpringSystem mSpringSystem = SpringSystem.create();
  private final ExampleSpringListener mSpringListener = new ExampleSpringListener();
  private FrameLayout mRootView;
  private Spring mScaleSpring;
  private View mImageView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mScaleSpring = mSpringSystem.createSpring();
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mRootView = (FrameLayout) inflater.inflate(R.layout.zoom_fragment, container, false);
    mImageView = mRootView.findViewById(R.id.image_view);
    mRootView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            mScaleSpring.setEndValue(1);
            break;
          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_CANCEL:
            mScaleSpring.setEndValue(0);
            break;
        }
        return true;
      }
    });
    return mRootView;
  }

  @Override
  public void onResume() {
    super.onResume();
    mScaleSpring.addListener(mSpringListener);
  }

  @Override
  public void onPause() {
    super.onPause();
    mScaleSpring.removeListener(mSpringListener);
  }

  private class ExampleSpringListener extends SimpleSpringListener {
    @Override
    public void onSpringUpdate(Spring spring) {
      float mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
      mImageView.setScaleX(mappedValue);
      mImageView.setScaleY(mappedValue);
    }
  }
}
