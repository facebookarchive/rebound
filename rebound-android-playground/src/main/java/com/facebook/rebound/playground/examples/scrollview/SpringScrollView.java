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
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewDebug;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import java.util.List;

public class SpringScrollView extends FrameLayout {
  static final int ANIMATED_SCROLL_GAP = 250;

  static final float MAX_SCROLL_FACTOR = 0.5f;

  private static final String TAG = "ScrollView";

  private long mLastScroll;

  private final Rect mTempRect = new Rect();
  private SpringOverScroller mScroller;

  /**
   * Position of the last motion event.
   */
  private int mLastMotionY;

  /**
   * True when the layout has changed but the traversal has not come through yet.
   * Ideally the view hierarchy would keep track of this for us.
   */
  private boolean mIsLayoutDirty = true;

  /**
   * The child to give focus to in the event that a child has requested focus while the
   * layout is dirty. This prevents the scroll from being wrong if the child has not been
   * laid out before requesting focus.
   */
  private View mChildToScrollTo = null;

  /**
   * True if the user is currently dragging this ScrollView around. This is
   * not the same as 'is being flinged', which can be checked by
   * mScroller.isFinished() (flinging begins when the user lifts his finger).
   */
  private boolean mIsBeingDragged = false;

  /**
   * Determines speed during touch scrolling
   */
  private VelocityTracker mVelocityTracker;

  /**
   * When set to true, the scroll view measure its child to make it fill the currently
   * visible area.
   */
  @ViewDebug.ExportedProperty(category = "layout")
  private boolean mFillViewport;

  /**
   * Whether arrow scrolling is animated.
   */
  private boolean mSmoothScrollingEnabled = true;

  private int mTouchSlop;
  private int mMinimumVelocity;
  private int mMaximumVelocity;

  private int mOverscrollDistance;
  private int mOverflingDistance;

  /**
   * ID of the active pointer. This is used to retain consistency during
   * drags/flings if multiple pointers are used.
   */
  private int mActivePointerId = INVALID_POINTER;

  /**
   * Sentinel value for no current active pointer.
   * Used by {@link #mActivePointerId}.
   */
  private static final int INVALID_POINTER = -1;

  private SavedState mSavedState;

  public SpringScrollView(Context context) {
    this(context, null);
  }

  public SpringScrollView(Context context, AttributeSet attrs) {
    this(context, attrs, 0); //TODO: com.android.internal.R.attr.scrollViewStyle);
  }

  public SpringScrollView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    initScrollView();

    // TODO: enable attributes
    // TypedArray a =
    //        context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.ScrollView, defStyle, 0);
    // setFillViewport(a.getBoolean(R.styleable.ScrollView_fillViewport, false));
    // a.recycle();
  }

  @Override
  public int getOverScrollMode() {
    return OVER_SCROLL_ALWAYS;
  }

  @Override
  public boolean shouldDelayChildPressedState() {
    return true;
  }

  @Override
  protected float getTopFadingEdgeStrength() {
    return 0;
  }

  @Override
  protected float getBottomFadingEdgeStrength() {
    return 0;
  }

  /**
   * @return The maximum amount this scroll view will scroll in response to
   *   an arrow event.
   */
  public int getMaxScrollAmount() {
    return (int) (MAX_SCROLL_FACTOR * (getBottom() - getTop()));
  }


  private void initScrollView() {
    mScroller = new SpringOverScroller(getContext());
    setFocusable(true);
    setDescendantFocusability(FOCUS_AFTER_DESCENDANTS);
    setWillNotDraw(false);
    final ViewConfiguration configuration = ViewConfiguration.get(getContext());
    mTouchSlop = configuration.getScaledTouchSlop();
    mMinimumVelocity = configuration.getScaledMinimumFlingVelocity();
    mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
    mOverscrollDistance = configuration.getScaledOverscrollDistance();
    mOverflingDistance = configuration.getScaledOverflingDistance();
  }

  @Override
  public void addView(View child) {
    if (getChildCount() > 0) {
      throw new IllegalStateException("ScrollView can host only one direct child");
    }

    super.addView(child);
  }

  @Override
  public void addView(View child, int index) {
    if (getChildCount() > 0) {
      throw new IllegalStateException("ScrollView can host only one direct child");
    }

    super.addView(child, index);
  }

  @Override
  public void addView(View child, ViewGroup.LayoutParams params) {
    if (getChildCount() > 0) {
      throw new IllegalStateException("ScrollView can host only one direct child");
    }

    super.addView(child, params);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (getChildCount() > 0) {
      throw new IllegalStateException("ScrollView can host only one direct child");
    }

    super.addView(child, index, params);
  }

  /**
   * @return Returns true this ScrollView can be scrolled
   */
  private boolean canScroll() {
    View child = getChildAt(0);
    if (child != null) {
      int childHeight = child.getHeight();
      return getHeight() < childHeight + getPaddingTop() + getPaddingBottom();
    }
    return false;
  }

  /**
   * Indicates whether this ScrollView's content is stretched to fill the viewport.
   *
   * @return True if the content fills the viewport, false otherwise.
   *
   * @attr ref android.R.styleable#ScrollView_fillViewport
   */
  public boolean isFillViewport() {
    return mFillViewport;
  }

  /**
   * Indicates this ScrollView whether it should stretch its content height to fill
   * the viewport or not.
   *
   * @param fillViewport True to stretch the content's height to the viewport's
   *        boundaries, false otherwise.
   *
   * @attr ref android.R.styleable#ScrollView_fillViewport
   */
  public void setFillViewport(boolean fillViewport) {
    if (fillViewport != mFillViewport) {
      mFillViewport = fillViewport;
      requestLayout();
    }
  }

  /**
   * @return Whether arrow scrolling will animate its transition.
   */
  public boolean isSmoothScrollingEnabled() {
    return mSmoothScrollingEnabled;
  }

  /**
   * Set whether arrow scrolling will animate its transition.
   * @param smoothScrollingEnabled whether arrow scrolling will animate its transition
   */
  public void setSmoothScrollingEnabled(boolean smoothScrollingEnabled) {
    mSmoothScrollingEnabled = smoothScrollingEnabled;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (!mFillViewport) {
      return;
    }

    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightMode == MeasureSpec.UNSPECIFIED) {
      return;
    }

    if (getChildCount() > 0) {
      final View child = getChildAt(0);
      int height = getMeasuredHeight();
      if (child.getMeasuredHeight() < height) {
        final FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();

        int childWidthMeasureSpec = getChildMeasureSpec(widthMeasureSpec,
            getPaddingLeft() + getPaddingRight(), lp.width);
        height -= getPaddingTop();
        height -= getPaddingBottom();
        int childHeightMeasureSpec =
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);

        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
      }
    }
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent event) {
    // Let the focused view and/or our descendants get the key first
    return super.dispatchKeyEvent(event) || executeKeyEvent(event);
  }

  /**
   * You can call this function yourself to have the scroll view perform
   * scrolling from a key event, just as if the event had been dispatched to
   * it by the view hierarchy.
   *
   * @param event The key event to execute.
   * @return Return true if the event was handled, else false.
   */
  public boolean executeKeyEvent(KeyEvent event) {
    mTempRect.setEmpty();

    if (!canScroll()) {
      if (isFocused() && event.getKeyCode() != KeyEvent.KEYCODE_BACK) {
        View currentFocused = findFocus();
        if (currentFocused == this) currentFocused = null;
        View nextFocused = FocusFinder.getInstance().findNextFocus(this,
            currentFocused, View.FOCUS_DOWN);
        return nextFocused != null
            && nextFocused != this
            && nextFocused.requestFocus(View.FOCUS_DOWN);
      }
      return false;
    }

    boolean handled = false;
    if (event.getAction() == KeyEvent.ACTION_DOWN) {
      switch (event.getKeyCode()) {
        case KeyEvent.KEYCODE_DPAD_UP:
          if (!event.isAltPressed()) {
            handled = arrowScroll(View.FOCUS_UP);
          } else {
            handled = fullScroll(View.FOCUS_UP);
          }
          break;
        case KeyEvent.KEYCODE_DPAD_DOWN:
          if (!event.isAltPressed()) {
            handled = arrowScroll(View.FOCUS_DOWN);
          } else {
            handled = fullScroll(View.FOCUS_DOWN);
          }
          break;
        case KeyEvent.KEYCODE_SPACE:
          pageScroll(event.isShiftPressed() ? View.FOCUS_UP : View.FOCUS_DOWN);
          break;
      }
    }

    return handled;
  }

  private boolean inChild(int x, int y) {
    if (getChildCount() > 0) {
      final int scrollY = getScrollY();
      final View child = getChildAt(0);
      return !(y < child.getTop() - scrollY
          || y >= child.getBottom() - scrollY
          || x < child.getLeft()
          || x >= child.getRight());
    }
    return false;
  }

  private void initOrResetVelocityTracker() {
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    } else {
      mVelocityTracker.clear();
    }
  }

  private void initVelocityTrackerIfNotExists() {
    if (mVelocityTracker == null) {
      mVelocityTracker = VelocityTracker.obtain();
    }
  }

  private void recycleVelocityTracker() {
    if (mVelocityTracker != null) {
      mVelocityTracker.recycle();
      mVelocityTracker = null;
    }
  }

  @Override
  public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
    if (disallowIntercept) {
      recycleVelocityTracker();
    }
    super.requestDisallowInterceptTouchEvent(disallowIntercept);
  }


  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
        /*
         * This method JUST determines whether we want to intercept the motion.
         * If we return true, onMotionEvent will be called and we do the actual
         * scrolling there.
         */

        /*
        * Shortcut the most recurring case: the user is in the dragging
        * state and he is moving his finger.  We want to intercept this
        * motion.
        */
    final int action = ev.getAction();
    if ((action == MotionEvent.ACTION_MOVE) && (mIsBeingDragged)) {
      return true;
    }

        /*
         * Don't try to intercept touch if we can't scroll anyway.
         */
    if (getScrollY() == 0 && !canScrollVertically(1)) {
      return false;
    }

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_MOVE: {
                /*
                 * mIsBeingDragged == false, otherwise the shortcut would have caught it. Check
                 * whether the user has moved far enough from his original down touch.
                 */

                /*
                * Locally do absolute value. mLastMotionY is set to the y value
                * of the down event.
                */
        final int activePointerId = mActivePointerId;
        if (activePointerId == INVALID_POINTER) {
          // If we don't have a valid id, the touch down wasn't on content.
          break;
        }

        final int pointerIndex = ev.findPointerIndex(activePointerId);
        if (pointerIndex == -1) {
          Log.e(TAG, "Invalid pointerId=" + activePointerId
              + " in onInterceptTouchEvent");
          break;
        }

        final int y = (int) ev.getY(pointerIndex);
        final int yDiff = Math.abs(y - mLastMotionY);
        if (yDiff > mTouchSlop) {
          mIsBeingDragged = true;
          mLastMotionY = y;
          initVelocityTrackerIfNotExists();
          mVelocityTracker.addMovement(ev);
          final ViewParent parent = getParent();
          if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
          }
        }
        break;
      }

      case MotionEvent.ACTION_DOWN: {
        final int y = (int) ev.getY();
        if (!inChild((int) ev.getX(), (int) y)) {
          mIsBeingDragged = false;
          recycleVelocityTracker();
          break;
        }

                /*
                 * Remember location of down touch.
                 * ACTION_DOWN always refers to pointer index 0.
                 */
        mLastMotionY = y;
        mActivePointerId = ev.getPointerId(0);

        initOrResetVelocityTracker();
        mVelocityTracker.addMovement(ev);
                /*
                * If being flinged and user touches the screen, initiate drag;
                * otherwise don't.  mScroller.isFinished should be false when
                * being flinged.
                */
        mIsBeingDragged = !mScroller.isFinished();
        break;
      }

      case MotionEvent.ACTION_CANCEL:
      case MotionEvent.ACTION_UP:
                /* Release the drag */
        mIsBeingDragged = false;
        mActivePointerId = INVALID_POINTER;
        recycleVelocityTracker();
        if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange())) {
          postInvalidateOnAnimation();
        }
        break;
      case MotionEvent.ACTION_POINTER_UP:
        onSecondaryPointerUp(ev);
        break;
    }

        /*
        * The only time we want to intercept motion events is if we are in the
        * drag mode.
        */
    return mIsBeingDragged;
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    initVelocityTrackerIfNotExists();
    mVelocityTracker.addMovement(ev);

    final int action = ev.getAction();

    switch (action & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN: {
        if (getChildCount() == 0) {
          return false;
        }
        if ((mIsBeingDragged = !mScroller.isFinished())) {
          final ViewParent parent = getParent();
          if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
          }
        }

                /*
                 * If being flinged and user touches, stop the fling. isFinished
                 * will be false if being flinged.
                 */
        if (!mScroller.isFinished()) {
          mScroller.abortAnimation();
        }

        // Remember where the motion event started
        mLastMotionY = (int) ev.getY();
        mActivePointerId = ev.getPointerId(0);
        break;
      }
      case MotionEvent.ACTION_MOVE:
        final int activePointerIndex = ev.findPointerIndex(mActivePointerId);
        if (activePointerIndex == -1) {
          Log.e(TAG, "Invalid pointerId=" + mActivePointerId + " in onTouchEvent");
          break;
        }

        final int y = (int) ev.getY(activePointerIndex);
        int deltaY = mLastMotionY - y;
        if (!mIsBeingDragged && Math.abs(deltaY) > mTouchSlop) {
          final ViewParent parent = getParent();
          if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
          }
          mIsBeingDragged = true;
          if (deltaY > 0) {
            deltaY -= mTouchSlop;
          } else {
            deltaY += mTouchSlop;
          }
        }
        if (mIsBeingDragged) {
          // Scroll to follow the motion event
          mLastMotionY = y;

          final int oldX = getScrollX();
          final int oldY = getScrollY();
          final int range = getScrollRange();
          final int overscrollMode = getOverScrollMode();
          final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
              (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

          // Calling overScrollBy will call onOverScrolled, which
          // calls onScrollChanged if applicable.
          if (overScrollBy(0, deltaY, 0, getScrollY(),
              0, range, 0, mOverscrollDistance, true)) {
            // Break our velocity if we hit a scroll barrier.
            mVelocityTracker.clear();
          }
        }
        break;
      case MotionEvent.ACTION_UP:
        if (mIsBeingDragged) {
          final VelocityTracker velocityTracker = mVelocityTracker;
          velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
          int initialVelocity = (int) velocityTracker.getYVelocity(mActivePointerId);

          if (getChildCount() > 0) {
            if ((Math.abs(initialVelocity) > mMinimumVelocity)) {
              fling(-initialVelocity);
            } else {
              if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0,
                  getScrollRange())) {
                postInvalidateOnAnimation();
              }
            }
          }

          mActivePointerId = INVALID_POINTER;
          endDrag();
        }
        break;
      case MotionEvent.ACTION_CANCEL:
        if (mIsBeingDragged && getChildCount() > 0) {
          if (mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange())) {
            postInvalidateOnAnimation();
          }
          mActivePointerId = INVALID_POINTER;
          endDrag();
        }
        break;
      case MotionEvent.ACTION_POINTER_DOWN: {
        final int index = ev.getActionIndex();
        mLastMotionY = (int) ev.getY(index);
        mActivePointerId = ev.getPointerId(index);
        break;
      }
      case MotionEvent.ACTION_POINTER_UP:
        onSecondaryPointerUp(ev);
        mLastMotionY = (int) ev.getY(ev.findPointerIndex(mActivePointerId));
        break;
    }
    return true;
  }

  private void onSecondaryPointerUp(MotionEvent ev) {
    final int pointerIndex = (ev.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK) >>
        MotionEvent.ACTION_POINTER_INDEX_SHIFT;
    final int pointerId = ev.getPointerId(pointerIndex);
    if (pointerId == mActivePointerId) {
      // This was our active pointer going up. Choose a new
      // active pointer and adjust accordingly.
      // TODO: Make this decision more intelligent.
      final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
      mLastMotionY = (int) ev.getY(newPointerIndex);
      mActivePointerId = ev.getPointerId(newPointerIndex);
      if (mVelocityTracker != null) {
        mVelocityTracker.clear();
      }
    }
  }

//  @Override
//  public boolean onGenericMotionEvent(MotionEvent event) {
//    if ((event.getSource() & InputDevice.SOURCE_CLASS_POINTER) != 0) {
//      switch (event.getAction()) {
//        case MotionEvent.ACTION_SCROLL: {
//          if (!mIsBeingDragged) {
//            final float vscroll = event.getAxisValue(MotionEvent.AXIS_VSCROLL);
//            if (vscroll != 0) {
//              final int delta = (int) (vscroll * getVerticalScrollFactor());
//              final int range = getScrollRange();
//              int oldScrollY = getScrollY();
//              int newScrollY = oldScrollY - delta;
//              if (newScrollY < 0) {
//                newScrollY = 0;
//              } else if (newScrollY > range) {
//                newScrollY = range;
//              }
//              if (newScrollY != oldScrollY) {
//                super.scrollTo(getScrollX(), newScrollY);
//                return true;
//              }
//            }
//          }
//        }
//      }
//    }
//    return super.onGenericMotionEvent(event);
//  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY,
                                boolean clampedX, boolean clampedY) {
    // Treat animating scrolls differently; see #computeScroll() for why.
    if (!mScroller.isFinished()) {
      final int oldX = getScrollX();
      final int oldY = getScrollY();
      setScrollX(scrollX);
      setScrollY(scrollY);
      invalidateParentIfNeeded();
      onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
      if (clampedY) {
        mScroller.springBack(getScrollX(), getScrollY(), 0, 0, 0, getScrollRange());
      }
    } else {
      super.scrollTo(scrollX, scrollY);
    }

    awakenScrollBars();
  }

  private void invalidateParentIfNeeded() {

    if (isHardwareAccelerated() && getParent() instanceof View) {
      ((View) getParent()).invalidate();
    }
  }

  @Override
  public boolean performAccessibilityAction(int action, Bundle arguments) {
    if (super.performAccessibilityAction(action, arguments)) {
      return true;
    }
    if (!isEnabled()) {
      return false;
    }
    switch (action) {
      case AccessibilityNodeInfo.ACTION_SCROLL_FORWARD: {
        final int viewportHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        final int targetScrollY = Math.min(getScrollY() + viewportHeight, getScrollRange());
        if (targetScrollY != getScrollY()) {
          smoothScrollTo(0, targetScrollY);
          return true;
        }
      } return false;
      case AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD: {
        final int viewportHeight = getHeight() - getPaddingBottom() - getPaddingTop();
        final int targetScrollY = Math.max(getScrollY() - viewportHeight, 0);
        if (targetScrollY != getScrollY()) {
          smoothScrollTo(0, targetScrollY);
          return true;
        }
      } return false;
    }
    return false;
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(SpringScrollView.class.getName());
    if (isEnabled()) {
      final int scrollRange = getScrollRange();
      if (scrollRange > 0) {
        info.setScrollable(true);
        if (getScrollY() > 0) {
          info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_BACKWARD);
        }
        if (getScrollY() < scrollRange) {
          info.addAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
        }
      }
    }
  }

  @Override
  public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    event.setClassName(SpringScrollView.class.getName());
    final boolean scrollable = getScrollRange() > 0;
    event.setScrollable(scrollable);
    event.setScrollX(getScrollX());
    event.setScrollY(getScrollY());
    event.setMaxScrollX(getScrollX());
    event.setMaxScrollY(getScrollRange());
  }

  private int getScrollRange() {
    int scrollRange = 0;
    if (getChildCount() > 0) {
      View child = getChildAt(0);
      scrollRange = Math.max(0,
          child.getHeight() - (getHeight() - getPaddingBottom() - getPaddingTop()));
    }
    return scrollRange;
  }

  /**
   * <p>
   * Finds the next focusable component that fits in the specified bounds.
   * </p>
   *
   * @param topFocus look for a candidate is the one at the top of the bounds
   *                 if topFocus is true, or at the bottom of the bounds if topFocus is
   *                 false
   * @param top      the top offset of the bounds in which a focusable must be
   *                 found
   * @param bottom   the bottom offset of the bounds in which a focusable must
   *                 be found
   * @return the next focusable component in the bounds or null if none can
   *         be found
   */
  private View findFocusableViewInBounds(boolean topFocus, int top, int bottom) {

    List<View> focusables = getFocusables(View.FOCUS_FORWARD);
    View focusCandidate = null;

        /*
         * A fully contained focusable is one where its top is below the bound's
         * top, and its bottom is above the bound's bottom. A partially
         * contained focusable is one where some part of it is within the
         * bounds, but it also has some part that is not within bounds.  A fully contained
         * focusable is preferred to a partially contained focusable.
         */
    boolean foundFullyContainedFocusable = false;

    int count = focusables.size();
    for (int i = 0; i < count; i++) {
      View view = focusables.get(i);
      int viewTop = view.getTop();
      int viewBottom = view.getBottom();

      if (top < viewBottom && viewTop < bottom) {
                /*
                 * the focusable is in the target area, it is a candidate for
                 * focusing
                 */

        final boolean viewIsFullyContained = (top < viewTop) &&
            (viewBottom < bottom);

        if (focusCandidate == null) {
                    /* No candidate, take this one */
          focusCandidate = view;
          foundFullyContainedFocusable = viewIsFullyContained;
        } else {
          final boolean viewIsCloserToBoundary =
              (topFocus && viewTop < focusCandidate.getTop()) ||
                  (!topFocus && viewBottom > focusCandidate
                      .getBottom());

          if (foundFullyContainedFocusable) {
            if (viewIsFullyContained && viewIsCloserToBoundary) {
                            /*
                             * We're dealing with only fully contained views, so
                             * it has to be closer to the boundary to beat our
                             * candidate
                             */
              focusCandidate = view;
            }
          } else {
            if (viewIsFullyContained) {
                            /* Any fully contained view beats a partially contained view */
              focusCandidate = view;
              foundFullyContainedFocusable = true;
            } else if (viewIsCloserToBoundary) {
                            /*
                             * Partially contained view beats another partially
                             * contained view if it's closer
                             */
              focusCandidate = view;
            }
          }
        }
      }
    }

    return focusCandidate;
  }

  /**
   * <p>Handles scrolling in response to a "page up/down" shortcut press. This
   * method will scroll the view by one page up or down and give the focus
   * to the topmost/bottommost component in the new visible area. If no
   * component is a good candidate for focus, this scrollview reclaims the
   * focus.</p>
   *
   * @param direction the scroll direction: {@link android.view.View#FOCUS_UP}
   *                  to go one page up or
   *                  {@link android.view.View#FOCUS_DOWN} to go one page down
   * @return true if the key event is consumed by this method, false otherwise
   */
  public boolean pageScroll(int direction) {
    boolean down = direction == View.FOCUS_DOWN;
    int height = getHeight();

    if (down) {
      mTempRect.top = getScrollY() + height;
      int count = getChildCount();
      if (count > 0) {
        View view = getChildAt(count - 1);
        if (mTempRect.top + height > view.getBottom()) {
          mTempRect.top = view.getBottom() - height;
        }
      }
    } else {
      mTempRect.top = getScrollY() - height;
      if (mTempRect.top < 0) {
        mTempRect.top = 0;
      }
    }
    mTempRect.bottom = mTempRect.top + height;

    return scrollAndFocus(direction, mTempRect.top, mTempRect.bottom);
  }

  /**
   * <p>Handles scrolling in response to a "home/end" shortcut press. This
   * method will scroll the view to the top or bottom and give the focus
   * to the topmost/bottommost component in the new visible area. If no
   * component is a good candidate for focus, this scrollview reclaims the
   * focus.</p>
   *
   * @param direction the scroll direction: {@link android.view.View#FOCUS_UP}
   *                  to go the top of the view or
   *                  {@link android.view.View#FOCUS_DOWN} to go the bottom
   * @return true if the key event is consumed by this method, false otherwise
   */
  public boolean fullScroll(int direction) {
    boolean down = direction == View.FOCUS_DOWN;
    int height = getHeight();

    mTempRect.top = 0;
    mTempRect.bottom = height;

    if (down) {
      int count = getChildCount();
      if (count > 0) {
        View view = getChildAt(count - 1);
        mTempRect.bottom = view.getBottom() + getPaddingBottom();
        mTempRect.top = mTempRect.bottom - height;
      }
    }

    return scrollAndFocus(direction, mTempRect.top, mTempRect.bottom);
  }

  /**
   * <p>Scrolls the view to make the area defined by <code>top</code> and
   * <code>bottom</code> visible. This method attempts to give the focus
   * to a component visible in this area. If no component can be focused in
   * the new visible area, the focus is reclaimed by this ScrollView.</p>
   *
   * @param direction the scroll direction: {@link android.view.View#FOCUS_UP}
   *                  to go upward, {@link android.view.View#FOCUS_DOWN} to downward
   * @param top       the top offset of the new area to be made visible
   * @param bottom    the bottom offset of the new area to be made visible
   * @return true if the key event is consumed by this method, false otherwise
   */
  private boolean scrollAndFocus(int direction, int top, int bottom) {
    boolean handled = true;

    int height = getHeight();
    int containerTop = getScrollY();
    int containerBottom = containerTop + height;
    boolean up = direction == View.FOCUS_UP;

    View newFocused = findFocusableViewInBounds(up, top, bottom);
    if (newFocused == null) {
      newFocused = this;
    }

    if (top >= containerTop && bottom <= containerBottom) {
      handled = false;
    } else {
      int delta = up ? (top - containerTop) : (bottom - containerBottom);
      doScrollY(delta);
    }

    if (newFocused != findFocus()) newFocused.requestFocus(direction);

    return handled;
  }

  /**
   * Handle scrolling in response to an up or down arrow click.
   *
   * @param direction The direction corresponding to the arrow key that was
   *                  pressed
   * @return True if we consumed the event, false otherwise
   */
  public boolean arrowScroll(int direction) {

    View currentFocused = findFocus();
    if (currentFocused == this) currentFocused = null;

    View nextFocused = FocusFinder.getInstance().findNextFocus(this, currentFocused, direction);

    final int maxJump = getMaxScrollAmount();

    if (nextFocused != null && isWithinDeltaOfScreen(nextFocused, maxJump, getHeight())) {
      nextFocused.getDrawingRect(mTempRect);
      offsetDescendantRectToMyCoords(nextFocused, mTempRect);
      int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
      doScrollY(scrollDelta);
      nextFocused.requestFocus(direction);
    } else {
      // no new focus
      int scrollDelta = maxJump;

      if (direction == View.FOCUS_UP && getScrollY() < scrollDelta) {
        scrollDelta = getScrollY();
      } else if (direction == View.FOCUS_DOWN) {
        if (getChildCount() > 0) {
          int daBottom = getChildAt(0).getBottom();
          int screenBottom = getScrollY() + getHeight() - getPaddingBottom();
          if (daBottom - screenBottom < maxJump) {
            scrollDelta = daBottom - screenBottom;
          }
        }
      }
      if (scrollDelta == 0) {
        return false;
      }
      doScrollY(direction == View.FOCUS_DOWN ? scrollDelta : -scrollDelta);
    }

    if (currentFocused != null && currentFocused.isFocused()
        && isOffScreen(currentFocused)) {
      // previously focused item still has focus and is off screen, give
      // it up (take it back to ourselves)
      // (also, need to temporarily force FOCUS_BEFORE_DESCENDANTS so we are
      // sure to
      // get it)
      final int descendantFocusability = getDescendantFocusability();  // save
      setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
      requestFocus();
      setDescendantFocusability(descendantFocusability);  // restore
    }
    return true;
  }

  /**
   * @return whether the descendant of this scroll view is scrolled off
   *  screen.
   */
  private boolean isOffScreen(View descendant) {
    return !isWithinDeltaOfScreen(descendant, 0, getHeight());
  }

  /**
   * @return whether the descendant of this scroll view is within delta
   *  pixels of being on the screen.
   */
  private boolean isWithinDeltaOfScreen(View descendant, int delta, int height) {
    descendant.getDrawingRect(mTempRect);
    offsetDescendantRectToMyCoords(descendant, mTempRect);

    return (mTempRect.bottom + delta) >= getScrollY()
        && (mTempRect.top - delta) <= (getScrollY() + height);
  }

  /**
   * Smooth scroll by a Y delta
   *
   * @param delta the number of pixels to scroll by on the Y axis
   */
  private void doScrollY(int delta) {
    if (delta != 0) {
      if (mSmoothScrollingEnabled) {
        smoothScrollBy(0, delta);
      } else {
        scrollBy(0, delta);
      }
    }
  }

  /**
   * Like {@link View#scrollBy}, but scroll smoothly instead of immediately.
   *
   * @param dx the number of pixels to scroll by on the X axis
   * @param dy the number of pixels to scroll by on the Y axis
   */
  public final void smoothScrollBy(int dx, int dy) {
    if (getChildCount() == 0) {
      // Nothing to do.
      return;
    }
    long duration = AnimationUtils.currentAnimationTimeMillis() - mLastScroll;
    if (duration > ANIMATED_SCROLL_GAP) {
      final int height = getHeight() - getPaddingBottom() - getPaddingTop();
      final int bottom = getChildAt(0).getHeight();
      final int maxY = Math.max(0, bottom - height);
      final int scrollY = getScrollY();
      dy = Math.max(0, Math.min(scrollY + dy, maxY)) - scrollY;

      mScroller.startScroll(getScrollX(), scrollY, 0, dy);
      postInvalidateOnAnimation();
    } else {
      if (!mScroller.isFinished()) {
        mScroller.abortAnimation();
      }
      scrollBy(dx, dy);
    }
    mLastScroll = AnimationUtils.currentAnimationTimeMillis();
  }

  /**
   * Like {@link #scrollTo}, but scroll smoothly instead of immediately.
   *
   * @param x the position where to scroll on the X axis
   * @param y the position where to scroll on the Y axis
   */
  public final void smoothScrollTo(int x, int y) {
    smoothScrollBy(x - getScrollX(), y - getScrollY());
  }

  /**
   * <p>The scroll range of a scroll view is the overall height of all of its
   * children.</p>
   */
  @Override
  protected int computeVerticalScrollRange() {
    final int count = getChildCount();
    final int contentHeight = getHeight() - getPaddingBottom() - getPaddingTop();
    if (count == 0) {
      return contentHeight;
    }

    int scrollRange = getChildAt(0).getBottom();
    final int scrollY = getScrollY();
    final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
    if (scrollY < 0) {
      scrollRange -= scrollY;
    } else if (scrollY > overscrollBottom) {
      scrollRange += scrollY - overscrollBottom;
    }

    return scrollRange;
  }

  @Override
  protected int computeVerticalScrollOffset() {
    return Math.max(0, super.computeVerticalScrollOffset());
  }

  @Override
  protected void measureChild(View child, int parentWidthMeasureSpec, int parentHeightMeasureSpec) {
    ViewGroup.LayoutParams lp = child.getLayoutParams();

    int childWidthMeasureSpec;
    int childHeightMeasureSpec;

    childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec, getPaddingLeft()
        + getPaddingRight(), lp.width);

    childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }

  @Override
  protected void measureChildWithMargins(View child, int parentWidthMeasureSpec, int widthUsed,
                                         int parentHeightMeasureSpec, int heightUsed) {
    final MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();

    final int childWidthMeasureSpec = getChildMeasureSpec(parentWidthMeasureSpec,
        getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin
            + widthUsed, lp.width);
    final int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(
        lp.topMargin + lp.bottomMargin, MeasureSpec.UNSPECIFIED);

    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
  }

  @Override
  public void computeScroll() {
    if (mScroller.computeScrollOffset()) {
      // This is called at drawing time by ViewGroup.  We don't want to
      // re-show the scrollbars at this point, which scrollTo will do,
      // so we replicate most of scrollTo here.
      //
      //         It's a little odd to call onScrollChanged from inside the drawing.
      //
      //         It is, except when you remember that computeScroll() is used to
      //         animate scrolling. So unless we want to defer the onScrollChanged()
      //         until the end of the animated scrolling, we don't really have a
      //         choice here.
      //
      //         I agree.  The alternative, which I think would be worse, is to post
      //         something and tell the subclasses later.  This is bad because there
      //         will be a window where getScrollX()/Y is different from what the app
      //         thinks it is.
      //
      int oldX = getScrollX();
      int oldY = getScrollY();
      int x = mScroller.getCurrX();
      int y = mScroller.getCurrY();

      if (oldX != x || oldY != y) {
        final int range = getScrollRange();
        final int overscrollMode = getOverScrollMode();
        final boolean canOverscroll = overscrollMode == OVER_SCROLL_ALWAYS ||
            (overscrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && range > 0);

        overScrollBy(x - oldX, y - oldY, oldX, oldY, 0, range,
            0, mOverflingDistance, false);
        onScrollChanged(getScrollX(), getScrollY(), oldX, oldY);
      }

      if (!awakenScrollBars()) {
        // Keep on drawing until the animation has finished.
        postInvalidateOnAnimation();
      }
    }
  }

  /**
   * Scrolls the view to the given child.
   *
   * @param child the View to scroll to
   */
  private void scrollToChild(View child) {
    child.getDrawingRect(mTempRect);

        /* Offset from child's local coordinates to ScrollView coordinates */
    offsetDescendantRectToMyCoords(child, mTempRect);

    int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);

    if (scrollDelta != 0) {
      scrollBy(0, scrollDelta);
    }
  }

  /**
   * If rect is off screen, scroll just enough to get it (or at least the
   * first screen size chunk of it) on screen.
   *
   * @param rect      The rectangle.
   * @param immediate True to scroll immediately without animation
   * @return true if scrolling was performed
   */
  private boolean scrollToChildRect(Rect rect, boolean immediate) {
    final int delta = computeScrollDeltaToGetChildRectOnScreen(rect);
    final boolean scroll = delta != 0;
    if (scroll) {
      if (immediate) {
        scrollBy(0, delta);
      } else {
        smoothScrollBy(0, delta);
      }
    }
    return scroll;
  }

  /**
   * Compute the amount to scroll in the Y direction in order to get
   * a rectangle completely on the screen (or, if taller than the screen,
   * at least the first screen size chunk of it).
   *
   * @param rect The rect.
   * @return The scroll delta.
   */
  protected int computeScrollDeltaToGetChildRectOnScreen(Rect rect) {
    if (getChildCount() == 0) return 0;

    int height = getHeight();
    int screenTop = getScrollY();
    int screenBottom = screenTop + height;

    int fadingEdge = getVerticalFadingEdgeLength();

    // leave room for top fading edge as long as rect isn't at very top
    if (rect.top > 0) {
      screenTop += fadingEdge;
    }

    // leave room for bottom fading edge as long as rect isn't at very bottom
    if (rect.bottom < getChildAt(0).getHeight()) {
      screenBottom -= fadingEdge;
    }

    int scrollYDelta = 0;

    if (rect.bottom > screenBottom && rect.top > screenTop) {
      // need to move down to get it in view: move down just enough so
      // that the entire rectangle is in view (or at least the first
      // screen size chunk).

      if (rect.height() > height) {
        // just enough to get screen size chunk on
        scrollYDelta += (rect.top - screenTop);
      } else {
        // get entire rect at bottom of screen
        scrollYDelta += (rect.bottom - screenBottom);
      }

      // make sure we aren't scrolling beyond the end of our content
      int bottom = getChildAt(0).getBottom();
      int distanceToBottom = bottom - screenBottom;
      scrollYDelta = Math.min(scrollYDelta, distanceToBottom);

    } else if (rect.top < screenTop && rect.bottom < screenBottom) {
      // need to move up to get it in view: move up just enough so that
      // entire rectangle is in view (or at least the first screen
      // size chunk of it).

      if (rect.height() > height) {
        // screen size chunk
        scrollYDelta -= (screenBottom - rect.bottom);
      } else {
        // entire rect at top
        scrollYDelta -= (screenTop - rect.top);
      }

      // make sure we aren't scrolling any further than the top our content
      scrollYDelta = Math.max(scrollYDelta, -getScrollY());
    }
    return scrollYDelta;
  }

  @Override
  public void requestChildFocus(View child, View focused) {
    if (!mIsLayoutDirty) {
      scrollToChild(focused);
    } else {
      // The child may not be laid out yet, we can't compute the scroll yet
      mChildToScrollTo = focused;
    }
    super.requestChildFocus(child, focused);
  }


  /**
   * When looking for focus in children of a scroll view, need to be a little
   * more careful not to give focus to something that is scrolled off screen.
   *
   * This is more expensive than the default {@link android.view.ViewGroup}
   * implementation, otherwise this behavior might have been made the default.
   */
  @Override
  protected boolean onRequestFocusInDescendants(int direction,
                                                Rect previouslyFocusedRect) {

    // convert from forward / backward notation to up / down / left / right
    // (ugh).
    if (direction == View.FOCUS_FORWARD) {
      direction = View.FOCUS_DOWN;
    } else if (direction == View.FOCUS_BACKWARD) {
      direction = View.FOCUS_UP;
    }

    final View nextFocus = previouslyFocusedRect == null ?
        FocusFinder.getInstance().findNextFocus(this, null, direction) :
        FocusFinder.getInstance().findNextFocusFromRect(this,
            previouslyFocusedRect, direction);

    if (nextFocus == null) {
      return false;
    }

    if (isOffScreen(nextFocus)) {
      return false;
    }

    return nextFocus.requestFocus(direction, previouslyFocusedRect);
  }

  @Override
  public boolean requestChildRectangleOnScreen(View child, Rect rectangle,
                                               boolean immediate) {
    // offset into coordinate space of this scroll view
    rectangle.offset(child.getLeft() - child.getScrollX(),
        child.getTop() - child.getScrollY());

    return scrollToChildRect(rectangle, immediate);
  }

  @Override
  public void requestLayout() {
    mIsLayoutDirty = true;
    super.requestLayout();
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    mIsLayoutDirty = false;
    // Give a child focus if it needs it
    if (mChildToScrollTo != null && isViewDescendantOf(mChildToScrollTo, this)) {
      scrollToChild(mChildToScrollTo);
    }
    mChildToScrollTo = null;

    if (!isLaidOut()) {
      if (mSavedState != null) {
        setScrollY(mSavedState.scrollPosition);
        mSavedState = null;
      } // getScrollY() default value is "0"

      final int childHeight = (getChildCount() > 0) ? getChildAt(0).getMeasuredHeight() : 0;
      final int scrollRange = Math.max(0,
          childHeight - (b - t - getPaddingBottom() - getPaddingTop()));

      // Don't forget to clamp
      if (getScrollY() > scrollRange) {
        setScrollY(scrollRange);
      } else if (getScrollY() < 0) {
        setScaleY(0);
      }
    }

    // Calling this with the present values causes it to re-claim them
    scrollTo(getScrollX(), getScrollY());
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);

    View currentFocused = findFocus();
    if (null == currentFocused || this == currentFocused)
      return;

    // If the currently-focused view was visible on the screen when the
    // screen was at the old height, then scroll the screen to make that
    // view visible with the new screen height.
    if (isWithinDeltaOfScreen(currentFocused, 0, oldh)) {
      currentFocused.getDrawingRect(mTempRect);
      offsetDescendantRectToMyCoords(currentFocused, mTempRect);
      int scrollDelta = computeScrollDeltaToGetChildRectOnScreen(mTempRect);
      doScrollY(scrollDelta);
    }
  }

  /**
   * Return true if child is a descendant of parent, (or equal to the parent).
   */
  private static boolean isViewDescendantOf(View child, View parent) {
    if (child == parent) {
      return true;
    }

    final ViewParent theParent = child.getParent();
    return (theParent instanceof ViewGroup) && isViewDescendantOf((View) theParent, parent);
  }

  /**
   * Fling the scroll view
   *
   * @param velocityY The initial velocity in the Y direction. Positive
   *                  numbers mean that the finger/cursor is moving down the screen,
   *                  which means we want to scroll towards the top.
   */
  public void fling(int velocityY) {
    if (getChildCount() > 0) {
      int height = getHeight() - getPaddingBottom() - getPaddingTop();
      int bottom = getChildAt(0).getHeight();

      mScroller.fling(getScrollX(), getScrollY(), 0, velocityY, 0, 0, 0,
          Math.max(0, bottom - height), 0, height/2);

      postInvalidateOnAnimation();
    }
  }

  private void endDrag() {
    mIsBeingDragged = false;
    recycleVelocityTracker();
  }

  /**
   * {@inheritDoc}
   *
   * <p>This version also clamps the scrolling to the bounds of our child.
   */
  @Override
  public void scrollTo(int x, int y) {
    // we rely on the fact the View.scrollBy calls scrollTo.
    if (getChildCount() > 0) {
      if (x != getScrollX() || y != getScrollY()) {
        super.scrollTo(x, y);
      }
    }
  }

  @Override
  public void setOverScrollMode(int mode) {
  }

  @Override
  public void draw(Canvas canvas) {
    super.draw(canvas);
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (getContext().getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      // Some old apps reused IDs in ways they shouldn't have.
      // Don't break them, but they don't get scroll state restoration.
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(ss.getSuperState());
    mSavedState = ss;
    requestLayout();
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    if (getContext().getApplicationInfo().targetSdkVersion <= Build.VERSION_CODES.JELLY_BEAN_MR2) {
      // Some old apps reused IDs in ways they shouldn't have.
      // Don't break them, but they don't get scroll state restoration.
      return super.onSaveInstanceState();
    }
    Parcelable superState = super.onSaveInstanceState();
    SavedState ss = new SavedState(superState);
    ss.scrollPosition = getScrollY();
    return ss;
  }

  static class SavedState extends BaseSavedState {
    public int scrollPosition;

    SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(Parcel source) {
      super(source);
      scrollPosition = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(scrollPosition);
    }

    @Override
    public String toString() {
      return "HorizontalScrollView.SavedState{"
          + Integer.toHexString(System.identityHashCode(this))
          + " scrollPosition=" + scrollPosition + "}";
    }

    public static final Parcelable.Creator<SavedState> CREATOR
        = new Parcelable.Creator<SavedState>() {
      public SavedState createFromParcel(Parcel in) {
        return new SavedState(in);
      }

      public SavedState[] newArray(int size) {
        return new SavedState[size];
      }
    };
  }

  protected boolean overScrollBy(int deltaX, int deltaY,
                                 int scrollX, int scrollY,
                                 int scrollRangeX, int scrollRangeY,
                                 int maxOverScrollX, int maxOverScrollY,
                                 boolean isTouchEvent) {
    final int overScrollMode = OVER_SCROLL_ALWAYS;
    final boolean canScrollHorizontal =
        computeHorizontalScrollRange() > computeHorizontalScrollExtent();
    final boolean canScrollVertical =
        computeVerticalScrollRange() > computeVerticalScrollExtent();
    final boolean overScrollHorizontal = overScrollMode == OVER_SCROLL_ALWAYS ||
        (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollHorizontal);
    final boolean overScrollVertical = overScrollMode == OVER_SCROLL_ALWAYS ||
        (overScrollMode == OVER_SCROLL_IF_CONTENT_SCROLLS && canScrollVertical);

    int newScrollX = scrollX + deltaX;
//    if (!overScrollHorizontal) {
//      maxOverScrollX = 0;
//    }

    int newScrollY = scrollY + deltaY;
    if (!overScrollVertical) {
      maxOverScrollY = 0;
    }

    // Clamp values if at the limits and record
    int padding = 0;
    final int left = -maxOverScrollX - padding;
    final int right = maxOverScrollX + scrollRangeX + padding;
    final int top = -maxOverScrollY - padding;
    final int bottom = maxOverScrollY + scrollRangeY + padding;

    boolean clampedX = false;
    if (newScrollX > right) {
//      newScrollX = right;
      clampedX = true;
    } else if (newScrollX < left) {
//      newScrollX = left;
      clampedX = true;
    }

    boolean clampedY = false;
    if (newScrollY > bottom) {
//      newScrollY = bottom;
      clampedY = true;
    } else if (newScrollY < top) {
//      newScrollY = top;
      clampedY = true;
    }

    if (mIsBeingDragged && clampedX) {
      newScrollX = (int)(scrollX + deltaX * 0.25);
    }
    if (mIsBeingDragged && clampedY) {
      newScrollY = (int)(scrollY + deltaY * 0.25);
    }

    onOverScrolled(newScrollX, newScrollY, clampedX, clampedY);

    return clampedX || clampedY;
  }

}
