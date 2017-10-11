/*
 * This file provided by Facebook is for non-commercial testing and evaluation purposes only.
 * Facebook reserves all rights not expressly granted.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 * FACEBOOK BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.facebook.rebound.playground.examples;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.TableLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringChain;
import com.facebook.rebound.playground.R;

import java.util.ArrayList;
import java.util.List;

public class SpringChainExample extends FrameLayout {

  private final SpringChain mSpringChain = SpringChain.create();

  private final List<View> mViews = new ArrayList<View>();
  private float mLastDownX;

  /** Touch handling **/
  private View mLastDraggingView;
  private float mLastDownXlat;
  private int mActivePointerId;
  private VelocityTracker mVelocityTracker;

  public SpringChainExample(Context context) {
    super(context);

    LayoutInflater inflater = LayoutInflater.from(context);
    ViewGroup container = (ViewGroup) inflater.inflate(R.layout.cascade_effect, this, false);
    addView(container);
    ViewGroup rootView = (ViewGroup) container.findViewById(R.id.root);
    int bgColor = Color.argb(255, 17, 148, 231);
    setBackgroundColor(bgColor);
    rootView.setBackgroundResource(R.drawable.rebound_tiles);

    int startColor = Color.argb(255, 255, 64, 230);
    int endColor = Color.argb(255, 255, 230, 64);
    ArgbEvaluator evaluator = new ArgbEvaluator();
    int viewCount = 10;
    for (int i = 0; i < viewCount; i++) {
      final View view = new View(context);
      view.setLayoutParams(
          new TableLayout.LayoutParams(
              ViewGroup.LayoutParams.MATCH_PARENT,
              ViewGroup.LayoutParams.WRAP_CONTENT,
              1f));
      mSpringChain.addSpring(new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
          float value = (float) spring.getCurrentValue();
          view.setTranslationX(value);
        }
      });
      int color = (Integer) evaluator.evaluate((float) i / (float) viewCount, startColor, endColor);
      view.setBackgroundColor(color);
      view.setOnTouchListener(new OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
          return handleRowTouch(v, event);
        }
      });
      mViews.add(view);
      rootView.addView(view);
    }

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);
        List<Spring> springs = mSpringChain.getAllSprings();
        for (int i = 0; i < springs.size(); i++) {
          springs.get(i).setCurrentValue(-mViews.get(i).getWidth());
        }
        postDelayed(new Runnable() {
          @Override
          public void run() {
            mSpringChain
                .setControlSpringIndex(0)
                .getControlSpring()
                .setEndValue(0);
          }
        }, 500);
      }
    });
  }

  private boolean handleRowTouch(View view, MotionEvent event) {
    int action = event.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:

        mActivePointerId = event.getPointerId(0);
        mLastDownXlat = view.getTranslationX();
        mLastDraggingView = view;
        mLastDownX = event.getRawX();

        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);

        int idx = mViews.indexOf(mLastDraggingView);
        mSpringChain
            .setControlSpringIndex(idx)
            .getControlSpring()
            .setCurrentValue(mLastDownXlat);
        break;
      case MotionEvent.ACTION_MOVE: {
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        if (pointerIndex != -1) {
          final int location[] = {0, 0};
          view.getLocationOnScreen(location);
          float x = event.getX(pointerIndex) + location[0];
          float offset = x - mLastDownX + mLastDownXlat;
          mSpringChain
              .getControlSpring()
              .setCurrentValue(offset);
          mVelocityTracker.addMovement(event);
        }
        break;
      }
      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
        final int pointerIndex = event.findPointerIndex(mActivePointerId);
        if (pointerIndex != -1) {
          mVelocityTracker.addMovement(event);
          mVelocityTracker.computeCurrentVelocity(1000);
          mSpringChain
              .getControlSpring()
              .setVelocity(mVelocityTracker.getXVelocity())
              .setEndValue(0);
        }
        break;
      }
    return true;
  }
}
