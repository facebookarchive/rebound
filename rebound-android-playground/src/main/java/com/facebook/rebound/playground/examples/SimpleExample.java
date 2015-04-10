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

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.facebook.rebound.playground.R;

public class SimpleExample extends FrameLayout {

  private final BaseSpringSystem mSpringSystem = SpringSystem.create();
  private final ExampleSpringListener mSpringListener = new ExampleSpringListener();
  private final FrameLayout mRootView;
  private final Spring mScaleSpring;
  private final View mImageView;

  public SimpleExample(Context context) {
    this(context, null);
  }

  public SimpleExample(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SimpleExample(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    mScaleSpring = mSpringSystem.createSpring();
    LayoutInflater inflater = LayoutInflater.from(context);
    mRootView = (FrameLayout) inflater.inflate(R.layout.photo_scale_example, this, false);
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
    addView(mRootView);
  }

  @Override
  protected void onAttachedToWindow() {
    mScaleSpring.addListener(mSpringListener);
  }

  @Override
  protected void onDetachedFromWindow() {
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
