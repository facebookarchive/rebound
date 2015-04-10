package com.facebook.rebound.playground.examples;

import android.animation.ArgbEvaluator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Shader;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.facebook.rebound.playground.R;

public class ToggleExample extends View implements SpringListener {
  private final Paint paint;
  private final Spring spring;
  private final BitmapShader bitmapShader;
  private final Paint strokePaint;

  int startBackgroundColor = Color.argb(255, 255, 230, 64);
  int endBackgroundColor = Color.argb(255, 255, 64, 230);
  private final int startDotColor = Color.argb(0, 255, 255, 255);
  private final int endDotColor = Color.argb(255, 255, 255, 255);
  private final ArgbEvaluator colorEvaluator = new ArgbEvaluator();
  private int dotColor;
  private int backgroundColor;
  private int radius;
  private int alpha;
  private float strokeWidth;
  private int distance;
  private int startDegrees;
  private Paint textPaint;

  public ToggleExample(Context context) {
    super(context);
    paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    spring = SpringSystem.create().createSpring().setSpringConfig(SpringConfig.fromBouncinessAndSpeed(10, 8)).addListener(this);
    BitmapFactory.Options options = new BitmapFactory.Options();
    Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.grumpy, options);
    bitmapShader = new BitmapShader(bmp, Shader.TileMode.REPEAT, Shader.TileMode.REPEAT);
    Matrix matrix = new Matrix();
    matrix.postTranslate(-300, -300);
    bitmapShader.setLocalMatrix(matrix);
    paint.setShader(bitmapShader);
    paint.setStyle(Paint.Style.FILL);
    paint.setAlpha(alpha);
    paint.setColor(Color.WHITE);

    strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    strokePaint.setStyle(Paint.Style.STROKE);
    strokePaint.setAlpha(alpha);
    strokePaint.setColor(Color.WHITE);

    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    textPaint.setTextSize(120);
    textPaint.setTextAlign(Paint.Align.CENTER);
    textPaint.setColor(startBackgroundColor);

    update((float) spring.getCurrentValue());
  }

  @Override
  public boolean onTouchEvent(MotionEvent event) {
    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        spring.setEndValue(1);
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        spring.setEndValue(0);
        break;
    }
    return true;
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    canvas.drawColor(backgroundColor);
    canvas.save();
    canvas.translate(getWidth() / 2f, getHeight() / 2f);

    int count = 10;
    for (int i = 0; i < count; i++) {
      canvas.save();
      canvas.rotate((i * (360 / count)) % 360);
      canvas.translate(0, -distance);
      strokePaint.setStrokeWidth(2);
      canvas.drawCircle(0, 0, radius / 9, strokePaint);
      canvas.restore();
    }

    // Center circle
    paint.setStrokeWidth(strokeWidth);
    canvas.drawCircle(0, 0, radius, paint);
    strokePaint.setStrokeWidth(strokeWidth);
    canvas.drawCircle(0, 0, radius, strokePaint);

    canvas.translate(0, -500);
    canvas.drawText("Material Design!", 0, 0, textPaint);

    canvas.restore();

  }
  private void update(float currentValue) {
    float clampedValue = (float) SpringUtil.clamp(currentValue, 0, 1);
    backgroundColor = (Integer) colorEvaluator.evaluate(
        clampedValue,
        startBackgroundColor,
        endBackgroundColor);

    radius = (int) Math.max(0, SpringUtil.mapValueFromRangeToRange(currentValue,  0,  1, 0, 280));
    alpha = (int) Math.max(currentValue * 255, 0);
    strokeWidth = (int) Math.max(currentValue * 10, 0);
    distance = (int) Math.max(currentValue * 400, 0);

    invalidate();
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    update((float) spring.getCurrentValue());
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
