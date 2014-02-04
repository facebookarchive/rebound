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

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Build;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;

import com.facebook.rebound.OrigamiValueConverter;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringListener;
import com.facebook.rebound.SpringSystem;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.facebook.rebound.ui.Util.*;

/**
 * The SpringConfiguratorView provides a reusable view for live-editing all registered springs
 * within an Application. Each registered Spring can be accessed by its id and its tension and
 * friction properties can be edited while the user tests the effected UI live.
 */
public class SpringConfiguratorView extends FrameLayout {

  private static final int MAX_SEEKBAR_VAL = 100000;
  private static final float MIN_TENSION = 0;
  private static final float MAX_TENSION = 200;
  private static final float MIN_FRICTION = 0;
  private static final float MAX_FRICTION = 50;
  private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.#");

  private final SpinnerAdapter spinnerAdapter;
  private final List<SpringConfig> mSpringConfigs = new ArrayList<SpringConfig>();
  private final Spring mRevealerSpring;
  private final float mStashPx;
  private final float mRevealPx;
  private final SpringConfigRegistry springConfigRegistry;
  private final int mTextColor = Color.argb(255, 225, 225, 225);
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

  @TargetApi(Build.VERSION_CODES.HONEYCOMB)
  public SpringConfiguratorView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    SpringSystem springSystem = SpringSystem.create();
    springConfigRegistry = SpringConfigRegistry.getInstance();
    spinnerAdapter = new SpinnerAdapter(context);

    Resources resources = getResources();
    mRevealPx = dpToPx(40, resources);
    mStashPx = dpToPx(280, resources);

    mRevealerSpring = springSystem.createSpring();
    SpringListener revealerSpringListener = new RevealerSpringListener();
    mRevealerSpring
        .setCurrentValue(1)
        .setEndValue(1)
        .addListener(revealerSpringListener);

    addView(generateHierarchy(context));

    SeekbarListener seekbarListener = new SeekbarListener();
    mTensionSeekBar.setMax(MAX_SEEKBAR_VAL);
    mTensionSeekBar.setOnSeekBarChangeListener(seekbarListener);

    mFrictionSeekBar.setMax(MAX_SEEKBAR_VAL);
    mFrictionSeekBar.setOnSeekBarChangeListener(seekbarListener);

    mSpringSelectorSpinner.setAdapter(spinnerAdapter);
    mSpringSelectorSpinner.setOnItemSelectedListener(new SpringSelectedListener());
    refreshSpringConfigurations();

    this.setTranslationY(mStashPx);
  }

  /**
   * Programmatically build up the view hierarchy to avoid the need for resources.
   * @return View hierarchy
   */
  private View generateHierarchy(Context context) {
    Resources resources = getResources();

    FrameLayout.LayoutParams params;
    int fivePx = dpToPx(5, resources);
    int tenPx = dpToPx(10, resources);
    int twentyPx = dpToPx(20, resources);
    TableLayout.LayoutParams tableLayoutParams = new TableLayout.LayoutParams(
        0,
        ViewGroup.LayoutParams.WRAP_CONTENT,
        1f);
    tableLayoutParams.setMargins(0, 0, fivePx, 0);
    LinearLayout seekWrapper;

    FrameLayout root = new FrameLayout(context);
    params = createLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, dpToPx(300, resources));
    root.setLayoutParams(params);

      FrameLayout container = new FrameLayout(context);
      params = createMatchParams();
      params.setMargins(0, twentyPx, 0, 0);
      container.setLayoutParams(params);
      container.setBackgroundColor(Color.argb(100, 0, 0, 0));
      root.addView(container);

        mSpringSelectorSpinner = new Spinner(context, Spinner.MODE_DIALOG);
        params = createMatchWrapParams();
        params.gravity = Gravity.TOP;
        params.setMargins(tenPx, tenPx, tenPx, 0);
        mSpringSelectorSpinner.setLayoutParams(params);
        container.addView(mSpringSelectorSpinner);

        LinearLayout linearLayout = new LinearLayout(context);
        params = createMatchWrapParams();
        params.setMargins(0, 0, 0, dpToPx(80, resources));
        params.gravity = Gravity.BOTTOM;
        linearLayout.setLayoutParams(params);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        container.addView(linearLayout);

          seekWrapper = new LinearLayout(context);
          params = createMatchWrapParams();
          params.setMargins(tenPx, tenPx, tenPx, twentyPx);
          seekWrapper.setPadding(tenPx, tenPx, tenPx, tenPx);
          seekWrapper.setLayoutParams(params);
          seekWrapper.setOrientation(LinearLayout.HORIZONTAL);
          linearLayout.addView(seekWrapper);

            mTensionSeekBar = new SeekBar(context);
            mTensionSeekBar.setLayoutParams(tableLayoutParams);
            seekWrapper.addView(mTensionSeekBar);

            mTensionLabel = new TextView(getContext());
            mTensionLabel.setTextColor(mTextColor);
            params = createLayoutParams(
                dpToPx(50, resources),
                ViewGroup.LayoutParams.MATCH_PARENT);
            mTensionLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            mTensionLabel.setLayoutParams(params);
            mTensionLabel.setMaxLines(1);
            seekWrapper.addView(mTensionLabel);

          seekWrapper = new LinearLayout(context);
          params = createMatchWrapParams();
          params.setMargins(tenPx, tenPx, tenPx, twentyPx);
          seekWrapper.setPadding(tenPx, tenPx, tenPx, tenPx);
          seekWrapper.setLayoutParams(params);
          seekWrapper.setOrientation(LinearLayout.HORIZONTAL);
          linearLayout.addView(seekWrapper);

            mFrictionSeekBar = new SeekBar(context);
            mFrictionSeekBar.setLayoutParams(tableLayoutParams);
            seekWrapper.addView(mFrictionSeekBar);

            mFrictionLabel = new TextView(getContext());
            mFrictionLabel.setTextColor(mTextColor);
            params = createLayoutParams(dpToPx(50, resources), ViewGroup.LayoutParams.MATCH_PARENT);
            mFrictionLabel.setGravity(Gravity.CENTER_VERTICAL | Gravity.LEFT);
            mFrictionLabel.setLayoutParams(params);
            mFrictionLabel.setMaxLines(1);
            seekWrapper.addView(mFrictionLabel);

      View nub = new View(context);
      params = createLayoutParams(dpToPx(60, resources), dpToPx(40, resources));
      params.gravity = Gravity.TOP | Gravity.CENTER;
      nub.setLayoutParams(params);
      nub.setOnTouchListener(new OnNubTouchListener());
      nub.setBackgroundColor(Color.argb(255, 0, 164, 209));
      root.addView(nub);

    return root;
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

    spinnerAdapter.clear();
    mSpringConfigs.clear();

    for (Map.Entry<SpringConfig, String> entry : springConfigMap.entrySet()) {
      if (entry.getKey() == SpringConfig.defaultConfig) {
        continue;
      }
      mSpringConfigs.add(entry.getKey());
      spinnerAdapter.add(entry.getValue());
    }
    // Add the default config in last.
    mSpringConfigs.add(SpringConfig.defaultConfig);
    spinnerAdapter.add(springConfigMap.get(SpringConfig.defaultConfig));
    spinnerAdapter.notifyDataSetChanged();
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
        mSelectedSpringConfig.tension =
            OrigamiValueConverter.tensionFromOrigamiValue(scaledTension);
        String roundedTensionLabel = DECIMAL_FORMAT.format(scaledTension);
        mTensionLabel.setText("T:" + roundedTensionLabel);
      }

      if (seekBar == mFrictionSeekBar) {
        float scaledFriction = ((val) * frictionRange) / MAX_SEEKBAR_VAL + MIN_FRICTION;
        mSelectedSpringConfig.friction =
            OrigamiValueConverter.frictionFromOrigamiValue(scaledFriction);
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
    float tension = (float) OrigamiValueConverter.origamiValueFromTension(springConfig.tension);
    float tensionRange = MAX_TENSION - MIN_TENSION;
    int scaledTension = Math.round(((tension - MIN_TENSION) * MAX_SEEKBAR_VAL) / tensionRange);

    float friction = (float) OrigamiValueConverter.origamiValueFromFriction(springConfig.friction);
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

  private class SpinnerAdapter extends BaseAdapter {

    private final Context mContext;
    private final List<String> mStrings;

    public SpinnerAdapter(Context context) {
      mContext = context;
      mStrings = new ArrayList<String>();
    }

    @Override
    public int getCount() {
      return mStrings.size();
    }

    @Override
    public Object getItem(int position) {
      return mStrings.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    public void add(String string) {
      mStrings.add(string);
      notifyDataSetChanged();
    }

    /**
     * Remove all elements from the list.
     */
    public void clear() {
      mStrings.clear();
      notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      TextView textView;
      if (convertView == null) {
        textView = new TextView(mContext);
        AbsListView.LayoutParams params = new AbsListView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT);
        textView.setLayoutParams(params);
        int twelvePx = dpToPx(12, getResources());
        textView.setPadding(twelvePx, twelvePx, twelvePx, twelvePx);
        textView.setTextColor(mTextColor);
      } else {
        textView = (TextView) convertView;
      }
      textView.setText(mStrings.get(position));
      return textView;
    }
  }
}

