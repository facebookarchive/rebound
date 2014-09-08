package com.facebook.rebound.androidplayground;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

public class RowView extends FrameLayout{
  private final TextView mTextView;

  public RowView(Context context) {
    this(context, null);
  }

  public RowView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RowView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    LayoutInflater inflater = LayoutInflater.from(context);
    View view = inflater.inflate(R.layout.row_view, this, false);
    mTextView = (TextView) view.findViewById(R.id.text_view);
    addView(view);
  }

  public void setText(String text) {
    mTextView.setText(text);
  }
}
