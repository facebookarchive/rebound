package com.facebook.rebound.androidplayground;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Util {

  public static final List<Integer> COLORS = new ArrayList<Integer>();
  static {
    for (int i = 0; i < 10; i++) {
      COLORS.add(randomColor());
    }
  }

  public static int randomColor() {
    Random random = new Random();
    return Color.argb(
        255,
        random.nextInt(255),
        random.nextInt(255),
        random.nextInt(255));
  }
}
