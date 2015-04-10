package com.facebook.rebound.playground.examples.fling;

import android.content.Context;
import android.view.MotionEvent;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

public class ReboundExample extends DartExample implements SpringListener {

  private final Spring spring;
  private static final SpringConfig COASTING = SpringConfig.fromOrigamiTensionAndFriction(0, 0.5);
  private static final SpringConfig BOUNCING = SpringConfig.fromOrigamiTensionAndFriction(40, 8);

  public ReboundExample(Context context) {
    super(context);
    spring = SpringSystem.create().createSpring().addListener(this);
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    spring.setCurrentValue(spring.getCurrentValue() - distanceY);
    return true;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    float goal = velocityY > 0 || spring.getCurrentValue() < getTargetCenter() ? 0 : getTargetCenter();
    spring
        .setSpringConfig(goal != 0 ? COASTING : BOUNCING)
        .setOvershootClampingEnabled(goal != 0)
        .setVelocity(velocityY)
        .setEndValue(goal);
    return true;
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    dart.setTranslationY((float) spring.getCurrentValue());
  }

  @Override
  public void onSpringAtRest(Spring spring) {}

  @Override
  public void onSpringActivate(Spring spring) {}

  @Override
  public void onSpringEndStateChange(Spring spring) {}
}
