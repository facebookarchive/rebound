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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.facebook.rebound.BaseSpringSystem;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringSystemListener;
import com.facebook.rebound.playground.app.Util;

import java.util.ArrayList;
import java.util.List;

public class BallExample extends FrameLayout implements SpringListener, SpringSystemListener {

  private final Spring xSpring;
  private final Spring ySpring;
  private final SpringSystem springSystem;
  private final SpringConfig COASTING;
  private float x;
  private float y;
  private Paint paint;
  private boolean dragging;
  private float radius = 100;
  private float downX;
  private float downY;
  private float lastX;
  private float lastY;
  private VelocityTracker velocityTracker;
  private float centerX;
  private float centerY;
  private float attractionThreshold = 200;
  private SpringConfig CONVERGING = SpringConfig.fromOrigamiTensionAndFriction(20, 3);
  private List<PointF> points = new ArrayList<PointF>();
  private ArgbEvaluator colorEvaluator = new ArgbEvaluator();
  private Integer startColor = Color.argb(255, 0, 255, 48);
  private Integer endColor = Color.argb(255, 0, 228, 255);

  public BallExample(Context context) {
    this(context, null);
  }

  public BallExample(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public BallExample(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    COASTING = SpringConfig.fromOrigamiTensionAndFriction(0, 0.5);
    COASTING.tension = 0;
    setBackgroundColor(Color.WHITE);

    springSystem = SpringSystem.create();
    springSystem.addListener(this);
    xSpring = springSystem.createSpring();
    ySpring = springSystem.createSpring();
    xSpring.addListener(this);
    ySpring.addListener(this);
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);

    getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
      @Override
      public void onGlobalLayout() {
        centerX = getWidth() / 2f;
        centerY = getHeight() / 2f;

        xSpring.setCurrentValue(centerX).setAtRest();
        ySpring.setCurrentValue(centerY).setAtRest();
        getViewTreeObserver().removeOnGlobalLayoutListener(this);

        int offsetH = (int) ((getHeight() - (2 * radius)) % 800) / 2;
        int offsetW = (int) ((getWidth() - (2 * radius))  % 800) / 2;
        for (float i = offsetH + radius; i < getHeight() - offsetH; i += 400) {
          for (float j = offsetW + radius; j < getWidth() - offsetW; j += 400) {
            points.add(new PointF(j, i));
          }
        }
      }
    });
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    int bg = Color.argb(255, 240, 240, 240);
    canvas.drawColor(bg);

    int i = 0;
    for (PointF point : points) {
      paint.setColor(Color.argb(255, 255, 255, 255));
      paint.setStyle(Paint.Style.FILL);
      canvas.drawCircle(point.x, point.y, attractionThreshold - 80, paint);
      Integer color = (Integer) colorEvaluator.evaluate(
          (i + 1) / (float) points.size(), startColor, endColor);
      paint.setColor(color);
      paint.setStyle(Paint.Style.STROKE);
      paint.setStrokeWidth(20);
      canvas.drawCircle(point.x, point.y, attractionThreshold - 80, paint);
      i++;
    }

    paint.setColor(Color.argb(200, 255, 0, 0));
    paint.setStyle(Paint.Style.FILL);
    canvas.drawCircle(x, y, radius, paint);
    paint.setColor(Color.WHITE);
    paint.setTextSize(36);
    paint.setTextAlign(Paint.Align.CENTER);
    canvas.drawText("TOUCH", x, y + 10, paint);
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    float touchX = event.getRawX();
    float touchY = event.getRawY();
    boolean ret = false;

    switch (event.getAction()) {
      case MotionEvent.ACTION_DOWN:
        downX = touchX;
        downY = touchY;
        lastX = downX;
        lastY = downY;
        velocityTracker = VelocityTracker.obtain();
        velocityTracker.addMovement(event);
        if (downX > x - radius && downX < x + radius && downY > y - radius && downY < y + radius) {
          dragging = true;
          ret = true;
        }
        break;
      case MotionEvent.ACTION_MOVE:
        if (!dragging) {
          break;
        }
        velocityTracker.addMovement(event);
        float offsetX = lastX - touchX;
        float offsetY = lastY - touchY;
        xSpring.setCurrentValue(xSpring.getCurrentValue() - offsetX).setAtRest();
        ySpring.setCurrentValue(ySpring.getCurrentValue() - offsetY).setAtRest();
        checkConstraints();
        ret = true;
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        if (!dragging) {
          break;
        }
        velocityTracker.addMovement(event);
        velocityTracker.computeCurrentVelocity(1000);
        dragging = false;
        ySpring.setSpringConfig(COASTING);
        xSpring.setSpringConfig(COASTING);
        downX = 0;
        downY = 0;
        xSpring.setVelocity(velocityTracker.getXVelocity());
        ySpring.setVelocity(velocityTracker.getYVelocity());
        ret = true;
    }

    lastX = touchX;
    lastY = touchY;
    return ret;
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    x = (float) xSpring.getCurrentValue();
    y = (float) ySpring.getCurrentValue();
    invalidate();
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

  @Override
  public void onBeforeIntegrate(BaseSpringSystem springSystem) {
  }

  @Override
  public void onAfterIntegrate(BaseSpringSystem springSystem) {
    checkConstraints();
  }

  private void checkConstraints() {
    if (x + radius >= getWidth()) {
      xSpring.setVelocity(-xSpring.getVelocity());
      xSpring.setCurrentValue(xSpring.getCurrentValue() - (x + radius - getWidth()), false);
    }
    if (x - radius <= 0) {
      xSpring.setVelocity(-xSpring.getVelocity());
      xSpring.setCurrentValue(xSpring.getCurrentValue() - (x - radius), false);
    }
    if (y + radius >= getHeight()) {
      ySpring.setVelocity(-ySpring.getVelocity());
      ySpring.setCurrentValue(ySpring.getCurrentValue() - (y + radius - getHeight()), false);
    }
    if (y - radius <= 0) {
      ySpring.setVelocity(-ySpring.getVelocity());
      ySpring.setCurrentValue(ySpring.getCurrentValue() - (y - radius), false);
    }

    for (PointF point : points) {
      if (dist(x, y, point.x, point.y) < attractionThreshold &&
          Math.abs(xSpring.getVelocity()) < 900 &&
          Math.abs(ySpring.getVelocity()) < 900 &&
          !dragging) {
        xSpring.setSpringConfig(CONVERGING);
        xSpring.setEndValue(point.x);
        ySpring.setSpringConfig(CONVERGING);
        ySpring.setEndValue(point.y);
      }
    }
  }

  private float dist(double posX, double posY, double pos2X, double pos2Y) {
    return (float) Math.sqrt(Math.pow(pos2X - posX, 2) + Math.pow(pos2Y - posY, 2));
  }
}
