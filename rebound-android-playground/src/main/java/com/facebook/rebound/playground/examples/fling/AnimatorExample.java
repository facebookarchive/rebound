package com.facebook.rebound.playground.examples.fling;

import android.content.Context;
import android.view.MotionEvent;
import android.view.animation.LinearInterpolator;

public class AnimatorExample extends DartExample {
  public AnimatorExample(Context context) {
    super(context);
  }

  @Override
  public boolean onScroll(
      MotionEvent e1,
      MotionEvent e2,
      float distanceX,
      float distanceY) {
    dart.setTranslationY(dart.getTranslationY() - distanceY);
    return true;
  }

  @Override
  public boolean onFling(
      MotionEvent e1,
      MotionEvent e2,
      float velocityX,
      float velocityY) {
    float goal = velocityY > 0 ? 0 : getTargetCenter();
    dart.animate()
        .translationY(goal)
        .setDuration(durationToGoalWithVelocity(goal, velocityY))
        .setInterpolator(new LinearInterpolator());
    return true;
  }

  private long durationToGoalWithVelocity(float goal, float velocity) {
    float seconds = (goal - dart.getTranslationY()) / velocity;
    return (long) (Math.abs(seconds) * 1000);
  }
}
