package com.facebook.rebound.androidplayground;

import android.app.Activity;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.facebook.rebound.SpringUtil;
import com.facebook.rebound.androidplayground.examples.BallExample;

import java.util.ArrayList;
import java.util.List;

public class PlaygroundActivity extends Activity implements AdapterView.OnItemClickListener {

  private static final List<String> PLACEHOLDER_TEXT = new ArrayList<String>();

  static {
    PLACEHOLDER_TEXT.add("HELLO!");
    PLACEHOLDER_TEXT.add("NOTHING");
    PLACEHOLDER_TEXT.add("TO");
    PLACEHOLDER_TEXT.add("SEE");
    PLACEHOLDER_TEXT.add("HERE");
    PLACEHOLDER_TEXT.add("YET...");
    PLACEHOLDER_TEXT.add("BUT");
    PLACEHOLDER_TEXT.add("CHECK");
    PLACEHOLDER_TEXT.add("BACK");
    PLACEHOLDER_TEXT.add("SOON :D");
  }

  private ListView mListView;
  private ExampleListAdapter mAdapter;
  private FrameLayout mRootView;
  private ExampleContainerView mCurrentExample;
  private boolean mAnimating;
  private LayoutInflater mInflater;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_playground);
    mInflater = LayoutInflater.from(this);
    mListView = (ListView) findViewById(R.id.list_view);
    mRootView = (FrameLayout) findViewById(R.id.root);
    mAdapter = new ExampleListAdapter();
    mListView.setAdapter(mAdapter);

    mListView.setOnItemClickListener(this);
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    getMenuInflater().inflate(R.menu.playground, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
    if (mAnimating) {
      return;
    }
    mAnimating = true;

//    PlaceholderView placeholderView = new PlaceholderView(this);
//    placeholderView.setBackgroundColor(Util.COLORS.get(position));
//    placeholderView.setTextColor(Color.WHITE);
//    placeholderView.setText(PLACEHOLDER_TEXT.get(position));

    BallExample ballExample = new BallExample(this);

    mCurrentExample = new ExampleContainerView(this);
    mCurrentExample.addView(ballExample);
    mRootView.addView(mCurrentExample);

    mCurrentExample.postDelayed(new Runnable() {
      @Override
      public void run() {
        mCurrentExample.reveal(true, new ExampleContainerView.Callback() {
          @Override
          public void onProgress(double progress) {
            float scale = (float) SpringUtil.mapValueFromRangeToRange(progress, 0, 1, 0.8, 1);
            mListView.setScaleX(scale);
            mListView.setScaleY(scale);
            mListView.setAlpha((float) progress);
          }

          @Override
          public void onEnd() {
            mAnimating = false;
          }
        });
      }
    }, 100);
  }

  @Override
  public void onBackPressed() {
    if (mAnimating || mCurrentExample == null) {
      return;
    }
    mAnimating = true;
    mCurrentExample.hide(true, new ExampleContainerView.Callback() {
      @Override
      public void onProgress(double progress) {
        float scale = (float) SpringUtil.mapValueFromRangeToRange(progress, 0, 1, 0.8, 1);
        mListView.setScaleX(scale);
        mListView.setScaleY(scale);
        mListView.setAlpha((float) progress);
      }

      @Override
      public void onEnd() {
        mAnimating = false;
        mCurrentExample.clearCallback();
        mRootView.removeView(mCurrentExample);
        mCurrentExample = null;
      }
    });
  }

  private class ExampleListAdapter implements ListAdapter {

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {}

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {
    }

    @Override
    public int getCount() {
      return PLACEHOLDER_TEXT.size();
    }

    @Override
    public Object getItem(int position) {
      return PLACEHOLDER_TEXT.get(position);
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public boolean hasStableIds() {
      return true;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
      RowView rowView;
      if (convertView != null) {
        rowView = (RowView) convertView;
      } else {
        rowView = new RowView(PlaygroundActivity.this);
      }
      rowView.setText(PLACEHOLDER_TEXT.get(position));
      rowView.setBackgroundColor(Util.COLORS.get(position));
      return rowView;
    }

    @Override
    public int getItemViewType(int position) {
      return 0;
    }

    @Override
    public int getViewTypeCount() {
      return 1;
    }

    @Override
    public boolean isEmpty() {
      return PLACEHOLDER_TEXT.isEmpty();
    }

    @Override
    public boolean areAllItemsEnabled() {
      return true;
    }

    @Override
    public boolean isEnabled(int position) {
      return true;
    }
  }
}
