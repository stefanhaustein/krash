package org.kobjects.graphics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

public class Screen extends ViewHolder<FrameLayout> implements LifecycleObserver {
  public final Activity activity;
  /**
   * Multiply with scale to get from virtual coordinates to px, divide to get from px to
   * virtual coordinates.
   */
  public float scale;
  ImageView imageView;
  Bitmap bitmap;
  float bitmapScale;
  public Dpad dpad;
  private Timer timer;

  /**
   * Contains all positioned view holders including children.
   */
  Set<PositionedViewHolder<?>> allWidgets = Collections.newSetFromMap(new WeakHashMap<>());

  public Screen(AppCompatActivity activity) {
    super(new FrameLayout(activity));
    activity.getLifecycle().addObserver(this);
    this.activity = activity;
    view.setClipChildren(false);

    int size = Dimensions.dpToPx(activity,200);

    bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
    bitmapScale = size / 200f;

    imageView = new ImageView(activity);
    imageView.setImageBitmap(bitmap);
    imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

    dpad = new Dpad(this);

    view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom) {
          scale = Math.min(right - left, bottom - top) / 200f;
          dpad.requestSync();
          synchronized (allWidgets) {
            for (PositionedViewHolder<?> widget : allWidgets) {
              widget.requestSync(true);
            }
          }
        }
      }
    });

    view.setFocusableInTouchMode(true);

    view.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));

    FrameLayout.LayoutParams dpadLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    dpadLayoutParams.gravity = Gravity.BOTTOM;
    view.addView(dpad.view, dpadLayoutParams);
  }

  public Pen createPen() {
    return new Pen(this);
  }


  public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
    System.out.println("KeyEvent: " + keyEvent);
    return false;
  }

  public void clearAll() {
    cls();
    synchronized (allWidgets) {
      for (PositionedViewHolder<?> widget : allWidgets) {
        widget.setVisible(false);
      }
    }
    allWidgets = Collections.newSetFromMap(new WeakHashMap<>());
  }

  public void cls() {
    bitmap.eraseColor(0);
    dpad.setVisible(false);
  }

  @Override
  public float getWidth() {
    return view.getWidth() / scale;
  }

  @Override
  public float getHeight() {
    return view.getHeight() / scale;
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  public synchronized void onResume() {
    timer = new Timer();
    timer.scheduleAtFixedRate(new TimerTask() {
      long lastCall = System.currentTimeMillis();
      @Override
      public void run() {
        long now = System.currentTimeMillis();
        long dt = now - lastCall;
        if (dt > 5) {
          animate(dt);
          lastCall = now;
        }
      }
    }, 0, 1000/60);
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  public synchronized void onPause() {
    timer.cancel();
    timer = null;
  }

  private void animate(float dt) {
    ArrayList<PositionedViewHolder<?>> copy = new ArrayList<>(allWidgets.size());
    synchronized (allWidgets) {
      copy.addAll(allWidgets);
    }
    for (PositionedViewHolder<?> widget : copy) {
      if (widget instanceof Sprite) {
        ((Sprite) widget).animate(dt);
      }
    }
  }
}
