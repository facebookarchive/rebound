/*
 *  Copyright (c) 2013, Facebook, Inc.
 *  All rights reserved.
 *
 *  This source code is licensed under the BSD-style license found in the
 *  LICENSE file in the root directory of this source tree. An additional grant
 *  of patent rights can be found in the PATENTS file in the same directory.
 *
 */

package com.facebook.rebound.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.facebook.rebound.R;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The SpringConfiguratorView provides a reusable view for live-editing all registered springs
 * within an Application. Each registered Spring can be accessed by its id and its tension and
 * friction properties can be edited while the user tests the effected UI live.
 */
public class SpringConfiguratorView extends FrameLayout {

  private static final String SPRING_CONFIG_NAME = "spring configurator revealer";
  private static final int MAX_SEEKBAR_VAL = 100000;
  private static final float MIN_TENSION = 0;
  private static final float MAX_TENSION = 500;
  private static final float MIN_FRICTION = 0;
  private static final float MAX_FRICTION = 100;
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

  private final SpringSystem springSystem;
  private final ArrayAdapter<String> springArrayAdapter;
  private final List<SpringConfig> mSpringConfigs = new ArrayList<SpringConfig>();
  private final Spring mRevealerSpring;
  private final float mStashPx;
  private final float mRevealPx;
  private final RevealerSpringListener mRevealerSpringListener;
  private final SpringConfigRegistry springConfigRegistry;
  private SeekBar mTensionSeekBar;
  private SeekBar mFrictionSeekBar;
  private Spinner mSpringSelectorSpinner;
  private TextView mFrictionLabel;
  private TextView mTensionLabel;
  private SpringConfig mSelectedSpringConfig;

  public SpringConfiguratorView(Context context) {
    this(context, null);
  }

  public SpringConfiguratorView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SpringConfiguratorView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    springSystem = SpringSystem.create();
    springConfigRegistry = SpringConfigRegistry.getInstance();
    springArrayAdapter = new ArrayAdapter<String>(getContext(), R.layout.spring_text);

    mRevealPx = getResources().getDimensionPixelOffset(R.dimen.spring_configurator_reveal_px);
    mStashPx = getResources().getDimensionPixelOffset(R.dimen.spring_configurator_stash_px);

    mRevealerSpring = springSystem.createSpring();
    mRevealerSpringListener = new RevealerSpringListener();
    mRevealerSpring
        .setCurrentValue(1)
        .setEndValue(1)
        .addListener(mRevealerSpringListener);

    LayoutInflater inflater = LayoutInflater.from(context);
    RelativeLayout content =
        (RelativeLayout) inflater.inflate(R.layout.spring_configurator_view, this, false);
    addView(content);

    View nub = findViewById(R.id.nub);
    nub.setOnTouchListener(new OnNubTouchListener());

    SeekbarListener seekbarListener = new SeekbarListener();

    mTensionSeekBar = (SeekBar) findViewById(R.id.tension_seekbar);
    mTensionSeekBar.setMax(MAX_SEEKBAR_VAL);
    mTensionSeekBar.setOnSeekBarChangeListener(seekbarListener);
    mTensionLabel = (TextView) findViewById(R.id.tension_label);

    mFrictionSeekBar = (SeekBar) findViewById(R.id.friction_seekbar);
    mFrictionSeekBar.setMax(MAX_SEEKBAR_VAL);
    mFrictionSeekBar.setOnSeekBarChangeListener(seekbarListener);
    mFrictionLabel = (TextView) findViewById(R.id.friction_label);

    mSpringSelectorSpinner = (Spinner) findViewById(R.id.spring_selector_spinner);
    mSpringSelectorSpinner.setAdapter(springArrayAdapter);
    mSpringSelectorSpinner.setOnItemSelectedListener(new SpringSelectedListener());
    refreshSpringConfigurations();

    this.setTranslationY(mStashPx);
  }

  /**
   * remove the configurator from its parent and clean up springs and listeners
   */
  public void destroy() {
    ViewGroup parent = (ViewGroup) getParent();
    if (parent != null) {
      parent.removeView(this);
    }
    mRevealerSpring.destroy();
  }

  /**
   * reload the springs from the registry and update the UI
   */
  public void refreshSpringConfigurations() {
    Map<SpringConfig, String> springConfigMap = springConfigRegistry.getAllSpringConfig();

    springArrayAdapter.clear();
    mSpringConfigs.clear();

    for (Map.Entry<SpringConfig, String> entry : springConfigMap.entrySet()) {
      mSpringConfigs.add(entry.getKey());
      springArrayAdapter.add(entry.getValue());
    }
    springArrayAdapter.notifyDataSetChanged();
    if (mSpringConfigs.size() > 0) {
      mSpringSelectorSpinner.setSelection(0);
    }
  }

  private class SpringSelectedListener implements AdapterView.OnItemSelectedListener {

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
      mSelectedSpringConfig = mSpringConfigs.get(i);
      updateSeekBarsForSpringConfig(mSelectedSpringConfig);
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
    }
  }

  /**
   * listen to events on seekbars and update registered springs accordingly
   */
  private class SeekbarListener implements SeekBar.OnSeekBarChangeListener {

    @Override
    public void onProgressChanged(SeekBar seekBar, int val, boolean b) {
      float tensionRange = MAX_TENSION - MIN_TENSION;
      float frictionRange = MAX_FRICTION - MIN_FRICTION;

      if (seekBar == mTensionSeekBar) {
        float scaledTension = ((val) * tensionRange) / MAX_SEEKBAR_VAL + MIN_TENSION;
        mSelectedSpringConfig.tension = scaledTension;
        String roundedTensionLabel = DECIMAL_FORMAT.format(scaledTension);
        mTensionLabel.setText("T:" + roundedTensionLabel);
      }

      if (seekBar == mFrictionSeekBar) {
        float scaledFriction = ((val) * frictionRange) / MAX_SEEKBAR_VAL + MIN_FRICTION;
        mSelectedSpringConfig.friction = scaledFriction;
        String roundedFrictionLabel = DECIMAL_FORMAT.format(scaledFriction);
        mFrictionLabel.setText("F:" + roundedFrictionLabel);
      }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }
  }

  /**
   * update the position of the seekbars based on the spring value;
   * @param springConfig current editing spring
   */
  private void updateSeekBarsForSpringConfig(SpringConfig springConfig) {
    float tension = (float) springConfig.tension;
    float tensionRange = MAX_TENSION - MIN_TENSION;
    int scaledTension = Math.round(((tension - MIN_TENSION) * MAX_SEEKBAR_VAL) / tensionRange);

    float friction = (float) springConfig.friction;
    float frictionRange = MAX_FRICTION - MIN_FRICTION;
    int scaledFriction = Math.round(((friction - MIN_FRICTION) * MAX_SEEKBAR_VAL) / frictionRange);

    mTensionSeekBar.setProgress(scaledTension);
    mFrictionSeekBar.setProgress(scaledFriction);
  }

  /**
   * toggle visibility when the nub is tapped.
   */
  private class OnNubTouchListener implements View.OnTouchListener {
    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
      if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
        togglePosition();
      }
      return true;
    }
  }

  private void togglePosition() {
    double currentValue = mRevealerSpring.getEndValue();
    mRevealerSpring
        .setEndValue(currentValue == 1 ? 0 : 1);
  }

  private class RevealerSpringListener implements SpringListener {

    @Override
    public void onSpringUpdate(Spring spring) {
      float val = (float) spring.getCurrentValue();
      float minTranslate = mRevealPx;
      float maxTranslate = mStashPx;
      float range = maxTranslate - minTranslate;
      float yTranslate = (val * range) + minTranslate;
      SpringConfiguratorView.this.setTranslationY(yTranslate);
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
}

