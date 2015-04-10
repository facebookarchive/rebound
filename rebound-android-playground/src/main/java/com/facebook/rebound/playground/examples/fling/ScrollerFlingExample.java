package com.facebook.rebound.playground.examples.fling;

import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.widget.OverScroller;

public class ScrollerFlingExample extends DartExample implements ViewTreeObserver.OnPreDrawListener {
  private final OverScroller scroller;

  public ScrollerFlingExample(Context context) {
    super(context);
    scroller = new OverScroller(context);
    getViewTreeObserver().addOnPreDrawListener(this);
  }

  @Override
  public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
    dart.setTranslationY(dart.getTranslationY() - distanceY);
    scroller.forceFinished(true);
    return true;
  }

  @Override
  public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    scroller.forceFinished(true);
    scroller.fling(0, (int) dart.getTranslationY(), 0, (int) velocityY, 0, 0, (int) getTargetCenter(), 0);
    postInvalidateOnAnimation();
    return true;
  }

  @Override
  public boolean onPreDraw() {
    if (!scroller.isFinished()) {
      scroller.computeScrollOffset();
      dart.setTranslationY(Math.min(Math.max(scroller.getCurrY(), getTargetCenter()), 0));
      postInvalidateOnAnimation();
    }
    return true;
  }
}
