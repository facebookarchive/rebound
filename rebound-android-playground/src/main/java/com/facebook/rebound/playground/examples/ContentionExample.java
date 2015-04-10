package com.facebook.rebound.playground.examples;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.playground.R;

public class ContentionExample extends FrameLayout implements SpringListener {

  private final View rightButton;
  private final View leftButton;
  private final Spring spring;
  private final View droid;

  public ContentionExample(Context context) {
    super(context);
    LayoutInflater.from(context).inflate(R.layout.contention_example, this);
    leftButton = findViewById(R.id.left);
    rightButton = findViewById(R.id.right);
    droid = findViewById(R.id.droid);

    spring = SpringSystem.create().createSpring().addListener(this);

    leftButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        translateAnimated(-getWidth() / 2f + droid.getWidth() / 2f, true);
      }
    });
    rightButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        translateAnimated(getWidth() / 2f - droid.getWidth() / 2f, true);
      }
    });
  }

  public void translateAnimated(float xlat, boolean animated) {
    if (animated) {
      spring.setEndValue(xlat);
    } else {
      spring.setCurrentValue(xlat);
    }
  }

  @Override
  public void onSpringUpdate(Spring spring) {
    droid.setTranslationX((float) spring.getCurrentValue());
  }

  @Override
  public void onSpringAtRest(Spring spring) {}

  @Override
  public void onSpringActivate(Spring spring) {}

  @Override
  public void onSpringEndStateChange(Spring spring) {}
}
