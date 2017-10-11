package com.facebook.rebound.ui;

import android.content.res.Resources;
import android.util.TypedValue;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Utilities for generating view hierarchies without using resources.
 */
public abstract class Util {

  public static int dpToPx(float dp, Resources res) {
    return (int) TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_DIP,
        dp,
        res.getDisplayMetrics());
  }

  public static FrameLayout.LayoutParams createLayoutParams(int width, int height) {
    return new FrameLayout.LayoutParams(width, height);
  }

  public static FrameLayout.LayoutParams createMatchParams() {
    return createLayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
  }

  public static FrameLayout.LayoutParams createWrapParams() {
    return createLayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  public static FrameLayout.LayoutParams createWrapMatchParams() {
    return createLayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT,
        ViewGroup.LayoutParams.MATCH_PARENT);
  }

  public static FrameLayout.LayoutParams createMatchWrapParams() {
    return createLayoutParams(
        ViewGroup.LayoutParams.MATCH_PARENT,
        ViewGroup.LayoutParams.WRAP_CONTENT);
  }

}
