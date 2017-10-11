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
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.facebook.rebound.playground.R;

public class ExampleRowView extends FrameLayout {
  private final TextView mTextView;

  public ExampleRowView(Context context) {
    super(context);
    LayoutInflater inflater = LayoutInflater.from(context);
    ViewGroup view = (ViewGroup) inflater.inflate(R.layout.example_row_view, this, false);
    mTextView = (TextView) view.findViewById(R.id.text_view);
    addView(view);
  }

  public void setText(String text) {
    mTextView.setText(text);
  }

}
