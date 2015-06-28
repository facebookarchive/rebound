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

  private final List<View> mViews = new ArrayList<>();
  private float mLastDownX;

  /** Touch handling **/
  private float mLastDownXlat;
  private int mActivePointerId;
  private VelocityTracker mVelocityTracker;

  private Integer startColor = Color.argb(255, 255, 64, 230);
  private Integer endColor = Color.argb(255, 255, 230, 64);

  public SpringChainExample(Context context) {
    super(context);
    setBackgroundColor(Color.argb(255, 17, 148, 231));
    LayoutInflater inflater = LayoutInflater.from(context);
    ViewGroup container = (ViewGroup) inflater.inflate(R.layout.cascade_effect, this, false);
    addView(container);
    ViewGroup rootView = (ViewGroup) container.findViewById(R.id.root);
    rootView.setBackgroundResource(R.drawable.rebound_tiles);
    initializeViews(context, rootView, new ArgbEvaluator());
  }

  @Override protected void onFinishInflate() {
    super.onFinishInflate();
    List<Spring> springs = mSpringChain.getAllSprings();
    for (int i = 0; i < springs.size(); i++) {
      springs.get(i).setCurrentValue(-mViews.get(i).getWidth());
    }
    postDelayed(new Runnable() {
      @Override public void run() {
        mSpringChain.setControlSpringIndex(0).getControlSpring().setEndValue(0);
      }
    }, 500);
  }

  private void initializeViews(Context context, ViewGroup rootView, ArgbEvaluator evaluator) {
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
      view.setBackgroundColor((Integer)
          evaluator.evaluate((float) i / (float) viewCount, startColor, endColor));
      view.setOnTouchListener(onTouchListener);
      mViews.add(view);
      rootView.addView(view);
    }
  }

  private boolean handleRowTouch(View view, MotionEvent event) {
    int action = event.getAction();
    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:

        mActivePointerId = event.getPointerId(0);
        mLastDownXlat = view.getTranslationX();
        mLastDownX = event.getRawX();

        mVelocityTracker = VelocityTracker.obtain();
        mVelocityTracker.addMovement(event);

        int idx = mViews.indexOf(view);
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

  private View.OnTouchListener onTouchListener = new OnTouchListener() {
    @Override public boolean onTouch(View v, MotionEvent event) {
      return handleRowTouch(v, event);
    }
  };

}
