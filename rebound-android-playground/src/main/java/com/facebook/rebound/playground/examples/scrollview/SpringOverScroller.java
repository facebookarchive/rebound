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

package com.facebook.rebound.playground.examples.scrollview;

import android.content.Context;
import android.util.Log;
import android.view.animation.Interpolator;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

public class SpringOverScroller implements SpringListener {

  private final SpringSystem mSpringSystem;
  private final Spring mSpringX;
  private final Spring mSpringY;

  private static final SpringConfig COASTING_CONFIG =
      SpringConfig.fromOrigamiTensionAndFriction(0, 0.5);
  private static final SpringConfig RUBBERBANDING_CONFIG =
      SpringConfig.fromOrigamiTensionAndFriction(20, 9);

  public SpringOverScroller(Context context) {
    this(context, null);
  }

  public SpringOverScroller(Context context, Interpolator interpolator) {
    this(context, interpolator, true);
  }

  public SpringOverScroller(Context context, Interpolator interpolator,
                            float bounceCoefficientX, float bounceCoefficientY) {
    this(context, interpolator, true);
  }

  public SpringOverScroller(Context context, Interpolator interpolator,
                            float bounceCoefficientX, float bounceCoefficientY, boolean flywheel) {
    this(context, interpolator, flywheel);
  }

  public SpringOverScroller(Context context, Interpolator interpolator, boolean flywheel) {
    mSpringSystem = SpringSystem.create();
    mSpringX = mSpringSystem
        .createSpring()
        .addListener(this);
    mSpringY = mSpringSystem
        .createSpring()
        .addListener(this);
    SpringConfigRegistry
        .getInstance()
        .addSpringConfig(RUBBERBANDING_CONFIG, "rubber-banding");
    SpringConfigRegistry
        .getInstance()
        .addSpringConfig(COASTING_CONFIG, "coasting");
  }

  public final void setFriction(float friction) {
  }

  public final boolean isFinished() {
    return mSpringX.isAtRest() && mSpringY.isAtRest();
  }

  public final int getCurrX() {
    return (int) Math.round(mSpringX.getCurrentValue());
  }

  public final int getCurrY() {
    return (int) Math.round(mSpringY.getCurrentValue());
  }

  public float getCurrVelocity() {
    double velX = mSpringX.getVelocity();
    double velY = mSpringX.getVelocity();
    return (int) Math.sqrt(velX * velX + velY * velY);
  }

  public final int getStartX() {
    return (int) Math.round(mSpringX.getStartValue());
  }

  public final int getStartY() {
    return (int) Math.round(mSpringY.getStartValue());
  }

  public final int getFinalX() {
    return (int) Math.round(mSpringX.getEndValue());
  }

  public final int getFinalY() {
    return (int) Math.round(mSpringY.getEndValue());
  }

  public boolean computeScrollOffset() {
    return !(mSpringX.isAtRest() && mSpringY.isAtRest());
  }

  public void startScroll(int startX, int startY, int dx, int dy) {
    mSpringX.setCurrentValue(startX).setEndValue(dx);
    mSpringY.setCurrentValue(startY).setEndValue(dy);
  }

  public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
    mSpringX.setCurrentValue(startX, false);
    mSpringY.setCurrentValue(startY, false);
    if (startX > maxX || startX < minX) {
      if (startX > maxX) {
        mSpringX.setEndValue(maxX);
      } else if (startX < minX) {
        mSpringX.setEndValue(minX);
      }
      mSpringX.setSpringConfig(RUBBERBANDING_CONFIG);
      return true;
    }
    if (startY > maxY || startY < minY) {
      if (startY > maxY) {
        mSpringY.setEndValue(maxY);
      } else if (startY < minY) {
        mSpringY.setEndValue(minY);
      }
      mSpringY.setSpringConfig(RUBBERBANDING_CONFIG);
      return true;
    }
    return true;
  }

  public void fling(int startX, int startY, int velocityX, int velocityY,
                    int minX, int maxX, int minY, int maxY, int overX, int overY) {
    mSpringX
        .setSpringConfig(COASTING_CONFIG)
        .setCurrentValue(startX)
        .setVelocity(velocityX);
    mSpringY
        .setSpringConfig(COASTING_CONFIG)
        .setCurrentValue(startY)
        .setVelocity(velocityY);
  }

  public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
  }

  public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
  }

  public void abortAnimation() {
    mSpringX.setAtRest();
    mSpringY.setAtRest();
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    Log.d("WSB", "cv:" + spring.getCurrentValue());
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
