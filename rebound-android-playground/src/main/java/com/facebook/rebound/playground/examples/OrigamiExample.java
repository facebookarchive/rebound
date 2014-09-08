package com.facebook.rebound.playground.examples;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;

import com.facebook.rebound.SimpleSpringListener;
import com.facebook.rebound.Spring;
import com.facebook.rebound.SpringConfig;
import com.facebook.rebound.SpringConfigRegistry;
import com.facebook.rebound.SpringSystem;
import com.facebook.rebound.SpringUtil;
import com.facebook.rebound.playground.R;
import com.facebook.rebound.ui.SpringConfiguratorView;
import com.facebook.rebound.ui.Util;

public class OrigamiExample extends FrameLayout {

  // Create a spring configuration based on Origami values from the Photo Grid example.
  private static final SpringConfig ORIGAMI_SPRING_CONFIG = SpringConfig.fromOrigamiTensionAndFriction(40, 7);

  private final Spring mSpring;
  private final View mSelectedPhoto;
  private final View mPhotoGrid;
  private final View mFeedbackBar;
  private final SpringConfiguratorView mSpringConfiguratorView;

  public OrigamiExample(Context context) {
    this(context, null);
  }

  public OrigamiExample(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public OrigamiExample(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    // Inflate the layout.
    LayoutInflater inflater = LayoutInflater.from(context);
    ViewGroup root = (ViewGroup) inflater.inflate(R.layout.origami_example, this, false);
    addView(root);

    // Listen for clicks on the root view.
    root.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        handleClick(v);
      }
    });

    // Get references to our views.
    mPhotoGrid = root.findViewById(R.id.grid);
    mSelectedPhoto = root.findViewById(R.id.selection);
    mFeedbackBar = root.findViewById(R.id.feedback);
    mSpringConfiguratorView = (SpringConfiguratorView) root.findViewById(R.id.spring_configurator);

    // Setup the Spring by creating a SpringSystem adding a SimpleListener that renders the
    // animation whenever the spring is updated.
    mSpring = SpringSystem
        .create()
        .createSpring()
        .setSpringConfig(ORIGAMI_SPRING_CONFIG)
        .addListener(new SimpleSpringListener() {
          @Override
          public void onSpringUpdate(Spring spring) {
            // Just tell the UI to update based on the springs current state.
            render();
          }
        });


    // Here we just wait until the first layout pass finishes and call our render method to update
    // the animation to the initial resting state of the spring.
    mPhotoGrid.getViewTreeObserver().addOnGlobalLayoutListener(
        new ViewTreeObserver.OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            render();
            mPhotoGrid.getViewTreeObserver().removeOnGlobalLayoutListener(this);
          }
        });

    /** Optional - Live Spring Tuning **/

    // Put our config into a registry. This is optional, but it gives you the ability to live tune
    // the spring using the SpringConfiguratorView which will show up at the bottom of the screen.
    SpringConfigRegistry.getInstance().addSpringConfig(ORIGAMI_SPRING_CONFIG, "origami animation spring");
    // Tell the SpringConfiguratorView that we've updated the registry to allow you to live tune the animation spring.
    mSpringConfiguratorView.refreshSpringConfigurations();

    // Uncomment this line to actually show the SpringConfiguratorView allowing you to live tune
    // the Spring constants as you manipulate the UI.
    mSpringConfiguratorView.setVisibility(View.VISIBLE);
  }

  /**
   * On click we just move the springs end state from 0 to 1. This allows the Spring to act much
   * like an Origami switch.
   */
  public void handleClick(View view) {
    if (mSpring.getEndValue() == 0) {
      mSpring.setEndValue(1);
    } else {
      mSpring.setEndValue(0);
    }
  }

  /**
   * This method takes the current state of the spring and maps it to all the values for each UI
   * element that is animated on this spring. This allows the Spring to act as a common timing
   * function for the animation ensuring that all element transitions are synchronized.
   *
   * You can think of these mappings as similiar to Origami transitions.
   * SpringUtil#mapValueFromRangeToRange converts the spring's 0 to 1 transition and maps it to the
   * range of animation for a property on a view such as translation, scale, rotation, and alpha.
   */
  private void render() {
    Resources resources = getResources();
    // Get the current spring value.
    double value = mSpring.getCurrentValue();

    // Map the spring to the feedback bar position so that its hidden off screen and bounces in on tap.
    float barPosition =
        (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, mFeedbackBar.getHeight(), 0);
    mFeedbackBar.setTranslationY(barPosition);

    // Map the spring to the selected photo scale as it moves into and out of the grid.
    float selectedPhotoScale =
        (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, 0.33, 1);
    selectedPhotoScale = Math.max(selectedPhotoScale, 0); // Clamp the value so we don't go below 0.
    mSelectedPhoto.setScaleX(selectedPhotoScale);
    mSelectedPhoto.setScaleY(selectedPhotoScale);

    // Map the spring to the selected photo translation from its position in the grid
    float selectedPhotoTranslateX = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, Util.dpToPx(-106.667f, resources), 0);
    float selectedPhotoTranslateY = (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, Util.dpToPx(46.667f, resources), 0);
    mSelectedPhoto.setTranslationX(selectedPhotoTranslateX);
    mSelectedPhoto.setTranslationY(selectedPhotoTranslateY);

    // Map the spring to the photo grid alpha as it fades to black when the photo is selected.
    float gridAlpha =
        (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, 1, 0);
    mPhotoGrid.setAlpha(gridAlpha);


    // Map the spring to the photo grid scale so that it scales down slightly as the selected photo // zooms in.
    float gridScale =
        (float) SpringUtil.mapValueFromRangeToRange(value, 0, 1, 1, 0.95);
    gridScale = Math.max(gridScale, 0); // Clamp the value so we don't go below 0.
    mPhotoGrid.setScaleX(gridScale);
    mPhotoGrid.setScaleY(gridScale);
  }

}
