package com.facebook.rebound.androidplayground;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

public class PlaceholderView extends FrameLayout{

  private final TextView mTextView;

  public PlaceholderView(Context context) {
    this(context, null);
  }

  public PlaceholderView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public PlaceholderView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.placeholder_view, this, false);
    mTextView = (TextView) view.findViewById(R.id.text_view);
    addView(view);
    setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  public void setText(String text) {
    mTextView.setText(text);
  }

  public void setTextColor(int color) {
    mTextView.setTextColor(color);
  }
}
