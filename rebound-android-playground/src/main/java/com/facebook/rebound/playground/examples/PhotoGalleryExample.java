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
import android.content.res.Resources;
import android.graphics.Point;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringChain;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.facebook.rebound.playground.app.Util;

import java.util.ArrayList;
import java.util.List;

public class PhotoGalleryExample extends FrameLayout implements SpringListener {

  private static final int ROWS = 5;
  private static final int COLS = 4;

  private final List<ImageView> mImageViews = new ArrayList<ImageView>();
  private final List<Point> mPositions = new ArrayList<Point>();
  private final SpringChain mSpringChain = SpringChain.create();
  private final Spring mSpring = SpringSystem
      .create()
      .createSpring()
      .addListener(this)
      .setSpringConfig(SpringConfig.fromOrigamiTensionAndFriction(40, 6));

  private int mActiveIndex;
  private int mPadding;

  public PhotoGalleryExample(Context context) {
    super(context);

    int viewCount = ROWS * COLS;

    for (int i = 0; i < viewCount; i++) {
      final int j = i;

      // Create the View.
      final ImageView imageView = new ImageView(context);
      mImageViews.add(imageView);
      addView(imageView);
      imageView.setAlpha(0f);
      imageView.setBackgroundColor(Util.randomColor());
      imageView.setLayerType(LAYER_TYPE_HARDWARE, null);

      // Add an image for each view.
      int res = getResources().getIdentifier("d" + (i % 11 + 1), "drawable", context.getPackageName());
      imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
      imageView.setImageResource(res);

      // Add a click listener to handle scaling up the view.
      imageView.setOnClickListener(new OnClickListener() {
        @Override
        public void onClick(View v) {
          int endValue = mSpring.getEndValue() == 0 ? 1 : 0;
          imageView.bringToFront();
          mActiveIndex = j;
          mSpring.setEndValue(endValue);
        }
      });

      // Add a spring to the SpringChain to do an entry animation.
      mSpringChain.addSpring(new SimpleSpringListener() {
        @Override
        public void onSpringUpdate(Spring spring) {
          render();
        }
      });
    }

    // Wait for layout.
    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        layout();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);

        postOnAnimationDelayed(new Runnable() {
          @Override
          public void run() {
            mSpringChain.setControlSpringIndex(0).getControlSpring().setEndValue(1);
          }
        }, 500);
      }
    });

  }

  private void render() {
    for (int i = 0; i < mImageViews.size(); i++) {
      ImageView imageView = mImageViews.get(i);
      if (mSpring.isAtRest() && mSpring.getCurrentValue() == 0) {
        // Performing the initial entry transition animation.
        Spring spring = mSpringChain.getAllSprings().get(i);
        float val = (float) spring.getCurrentValue();
        imageView.setScaleX(val);
        imageView.setScaleY(val);
        imageView.setAlpha(val);
        Point pos = mPositions.get(i);
        imageView.setTranslationX(pos.x);
        imageView.setTranslationY(pos.y);
      } else {
        // Scaling up a photo to fullscreen size.
        Point pos = mPositions.get(i);
        if (i == mActiveIndex) {
          float ww = imageView.getWidth();
          float hh = imageView.getHeight();
          float sx = getWidth() / ww;
          float sy = getHeight() / hh;
          float s = sx > sy ? sx : sy;
          float xlatX = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, pos.x, 0);
          float xlatY = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, pos.y, 0);
          imageView.setPivotX(0);
          imageView.setPivotY(0);
          imageView.setTranslationX(xlatX);
          imageView.setTranslationY(xlatY);

          float ss = (float) SpringUtil.mapValueFromRangeToRange(mSpring.getCurrentValue(), 0, 1, 1, s);
          imageView.setScaleX(ss);
          imageView.setScaleY(ss);
        } else {
          float val = (float) Math.max(0, 1 - mSpring.getCurrentValue());
          imageView.setAlpha(val);
        }
      }
    }
  }

  private void layout() {
    float width = getWidth();
    float height = getHeight();

    // Determine the size for each image given the screen dimensions.
    Resources res = getResources();
    mPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3, res.getDisplayMetrics());
    int colWidth = (int) Math.ceil((width - 2 * mPadding) / COLS) - 2 * mPadding;
    int rowHeight = (int) Math.ceil((height - 2 * mPadding) / ROWS) - 2 * mPadding;

    // Determine the resting position for each view.
    int k = 0;
    int py = 0;
    for (int i = 0; i < ROWS; i++) {
      int px = 0;
      py += mPadding * 2;
      for (int j = 0; j < COLS; j++) {
        px += mPadding * 2;
        ImageView imageView = mImageViews.get(k);
        imageView.setLayoutParams(new LayoutParams(colWidth, rowHeight));
        mPositions.add(new Point(px, py));
        px += colWidth;
        k++;
      }
      py += rowHeight;
    }
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    render();
  }

  @Override
  public void onSpringAtRest(Spring spring) {

  }

  @Override
  public void onSpringActivate(Spring spring) {

  }

  @Override
  public void onSpringEndStateChange(Spring spring) {

  }
}
