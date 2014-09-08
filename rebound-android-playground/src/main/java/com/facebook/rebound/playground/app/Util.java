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

package com.facebook.rebound.playground.app;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class Util {

  public static final List<Integer> COLORS = new ArrayList<Integer>();
  static {
    for (int i = 0; i < 10; i++) {
      COLORS.add(randomColor());
    }
  }

  public static int randomColor() {
    Random random = new Random();
    return Color.argb(
        255,
        random.nextInt(255),
        random.nextInt(255),
        random.nextInt(255));
  }
}
