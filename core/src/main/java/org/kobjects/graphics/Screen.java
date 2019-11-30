package org.kobjects.graphics;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
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

  private final static boolean DEBUG = false;

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

    bitmap = Bitmap.createBitmap(2 * size, 2 * size, Bitmap.Config.ARGB_8888);
    bitmapScale = size / 200f;

    if (DEBUG) {
      Canvas canvas = new Canvas(bitmap);
      Paint debugPaint = new Paint();
      debugPaint.setStyle(Paint.Style.STROKE);
      debugPaint.setColor(Color.RED);

      canvas.drawLine(0, 0, 10000, 10000, debugPaint);
      canvas.drawLine(bitmap.getWidth(), 0, 0, bitmap.getHeight(), debugPaint);
      canvas.drawLine(bitmap.getWidth() / 2, 0, bitmap.getWidth() / 2, 10000, debugPaint);
      canvas.drawLine(0, bitmap.getHeight() / 2, 10000, bitmap.getHeight() / 2, debugPaint);
      canvas.drawRect(bitmap.getWidth() / 4, bitmap.getHeight() / 4, bitmap.getWidth() * 3 / 4, 3 * bitmap.getHeight() / 4, debugPaint);
    }
    imageView = new AppCompatImageView(activity) {
      @Override
      protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        int w = r-l;
        int h = b-t;
        System.out.println("************** onLayout"+w + " x "+ h);
        Matrix matrix = new Matrix();
        float targetSize = 2 * Math.min(w, h);
        float scale = targetSize / bitmap.getWidth();
        matrix.setScale(scale, scale);
        matrix.postTranslate((w - targetSize) / 2, (h - targetSize) / 2);
        setImageMatrix(matrix);
      }
    };
    imageView.setImageBitmap(bitmap);
    imageView.setScaleType(ImageView.ScaleType.MATRIX);

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
