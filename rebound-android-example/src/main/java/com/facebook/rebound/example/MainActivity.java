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

package com.facebook.rebound.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;

/**
 * This Activity presents an ImageView that scales down when pressed and returns to full size when
 * released. This demonstrates a very simple integrates a very Simple integration of a Rebound
 * Spring model to drive a bouncy animation as the photo scales up and down. You can control the
 * Spring configuration by tapping on the blue nub at the bottom of the screen to reveal the
 * SpringConfiguratorView. From this view you can adjust the tension and friction of the animation
 * spring and observe the effect these values have on the animation.
 */
public class MainActivity extends Activity {

  private final BaseSpringSystem mSpringSystem = SpringSystem.create();
  private final ExampleSpringListener mSpringListener = new ExampleSpringListener();
  private FrameLayout mRootView;
  private Spring mScaleSpring;
  private View mImageView;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);
    mRootView = (FrameLayout) findViewById(R.id.root_view);
    mImageView = mRootView.findViewById(R.id.image_view);

    // Create the animation spring.
    mScaleSpring = mSpringSystem.createSpring();

    // Add an OnTouchListener to the root view.
    mRootView.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
          case MotionEvent.ACTION_DOWN:
            // When pressed start solving the spring to 1.
            mScaleSpring.setEndValue(1);
            break;
          case MotionEvent.ACTION_UP:
          case MotionEvent.ACTION_CANCEL:
            // When released start solving the spring to 0.
            mScaleSpring.setEndValue(0);
            break;
        }
        return true;
      }
    });
  }

  @Override
  public void onResume() {
    super.onResume();
    // Add a listener to the spring when the Activity resumes.
    mScaleSpring.addListener(mSpringListener);
  }

  @Override
  public void onPause() {
    super.onPause();
    // Remove the listener to the spring when the Activity pauses.
    mScaleSpring.removeListener(mSpringListener);
  }

  private class ExampleSpringListener extends SimpleSpringListener {
    @Override
    public void onSpringUpdate(Spring spring) {
      // On each update of the spring value, we adjust the scale of the image view to match the
      // springs new value. We use the SpringUtil linear interpolation function mapValueFromRangeToRange
      // to translate the spring's 0 to 1 scale to a 100% to 50% scale range and apply that to the View
      // with setScaleX/Y. Note that rendering is an implementation detail of the application and not
      // Rebound itself. If you need Gingerbread compatibility consider using NineOldAndroids to update
      // your view properties in a backwards compatible manner.
      float mappedValue = (float) SpringUtil.mapValueFromRangeToRange(spring.getCurrentValue(), 0, 1, 1, 0.5);
      mImageView.setScaleX(mappedValue);
      mImageView.setScaleY(mappedValue);
    }
  }

}
