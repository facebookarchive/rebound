package com.facebook.rebound.playground.examples.fling;

import android.content.Context;
import android.graphics.Rect;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.rebound.playground.R;

public class DartExample extends FrameLayout implements GestureDetector.OnGestureListener {

  private final GestureDetector gestureDetector;
  protected final View dart;
  protected final View target;

  public DartExample(Context context) {
    super(context);
    LayoutInflater.from(context).inflate(R.layout.dart_example, this);
    dart = findViewById(R.id.dart);
    target = findViewById(R.id.target);
    gestureDetector = new GestureDetector(context, this);
  }

  protected float getTargetCenter() {
    return target.getTop() + target.getHeight() / 2f - getHeight() + dart.getHeight() / 2f;
  }

  @Override
  public boolean onTouchEvent(MotionEvent e) {
    boolean res = gestureDetector.onTouchEvent(e);
    return res || super.onTouchEvent(e);
  }

  @Override
  public boolean onDown(MotionEvent e) {
    Rect hitRect = new Rect();
    dart.getHitRect(hitRect);
    return hitRect.contains((int) e.getX(), (int) e.getY());
  }

  @Override
  public void onShowPress(MotionEvent e) {}

  @Override
  public boolean onSingleTapUp(MotionEvent e) { return false; }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) { return false; }

  @Override
  public void onLongPress(MotionEvent e) { }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) { return false; }
}
