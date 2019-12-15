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

  private final static boolean DEBUG = false; // true;

  public final Activity activity;
  /**
   * Multiply with scale to get from virtual coordinates to px, divide to get from px to
   * virtual coordinates.
   */
  public float scale;

  private ImageView imageView;
  private Bitmap bitmap;
  float bitmapScale;
  public Dpad dpad;
  private Timer timer;
  private int logicalViewportHeight = 200;
  private int logicalViewportWidth = 200;
  private boolean physicalPixels;

  /**
   * Contains all positioned view holders including children.
   */
  Set<PositionedViewHolder<?>> allWidgets = Collections.newSetFromMap(new WeakHashMap<>());


  public Screen(AppCompatActivity activity) {
    super(new FrameLayout(activity));
    this.activity = activity;
    view.setClipChildren(false);
    activity.getLifecycle().addObserver(this);


    dpad = new Dpad(this);

    view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
      @Override
      public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom) {
          scale = Math.min(right - left, bottom - top) / Math.max(logicalViewportHeight, logicalViewportWidth);
          dpad.requestSync();
          synchronized (allWidgets) {
            for (PositionedViewHolder<?> widget : allWidgets) {
              widget.requestSync(true);
            }
          }
        }
      }
    });

    view.setFocusableInTouchMode(true);  // Why?

    FrameLayout.LayoutParams dpadLayoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    dpadLayoutParams.gravity = Gravity.BOTTOM;
    view.addView(dpad.view, dpadLayoutParams);

    imageView = new AppCompatImageView(activity) {
      @Override
      protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (bitmap == null) {
          return;
        }
        float availableWidth = right - left;
        float availableHeight = bottom - top;
        float physicalInnerWidth = bitmap.getHeight() / 2; // Width / height swap is intentional!
        float physicalInnerHeight = bitmap.getWidth() / 2;
        float scaleX = availableWidth / physicalInnerWidth;
        float scaleY = availableHeight / physicalInnerHeight;
        float scale = Math.min(scaleX, scaleY);
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate((availableWidth - bitmap.getWidth() * scale) / 2, (availableHeight - bitmap.getHeight() * scale) / 2);
        setImageMatrix(matrix);
      }
    };
    imageView.setScaleType(ImageView.ScaleType.MATRIX);

    view.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  public Pen createPen() {
    return new Pen(this);
  }


  public void setViewport(int logicalViewportWidth, int logicalViewportHeight, boolean physicalPixels) {
    if (logicalViewportWidth == this.logicalViewportWidth
        && logicalViewportHeight == this.logicalViewportHeight
        && physicalPixels == this.physicalPixels) {
      return;
    }
    this.physicalPixels = physicalPixels;
    this.logicalViewportHeight = logicalViewportHeight;
    this.logicalViewportWidth = logicalViewportWidth;

    if (bitmap != null) {
      bitmap = null;
    }
  }


  Bitmap getBitmap() {
    if (bitmap != null) {
      return bitmap;
    }

    int physicalInnerWidth;
    int physicalInnerHeight;
    if (physicalPixels) {
      physicalInnerWidth = logicalViewportWidth;
      physicalInnerHeight = logicalViewportHeight;
      bitmapScale = 1;
    } else {
      // This is a bit arbitrary at the moment...
      physicalInnerWidth = Dimensions.dpToPx(activity, logicalViewportWidth);
      physicalInnerHeight = Dimensions.dpToPx(activity, logicalViewportHeight);
      bitmapScale = ((float) physicalInnerWidth) / logicalViewportWidth;
    }
    bitmap = Bitmap.createBitmap(2 * physicalInnerHeight, 2 * physicalInnerWidth, Bitmap.Config.ARGB_8888);

    if (DEBUG) {
      Canvas canvas = new Canvas(bitmap);
      Paint debugPaint = new Paint();
      debugPaint.setStyle(Paint.Style.STROKE);
      debugPaint.setColor(Color.RED);

      canvas.drawLine(0, 0, bitmap.getWidth(), bitmap.getHeight(), debugPaint);
      canvas.drawLine(bitmap.getWidth(), 0, 0, bitmap.getHeight(), debugPaint);
      canvas.drawLine(bitmap.getWidth() / 2, 0, bitmap.getWidth() / 2, 10000, debugPaint);
      canvas.drawLine(0, bitmap.getHeight() / 2, 10000, bitmap.getHeight() / 2, debugPaint);
      canvas.drawRect(
          bitmap.getWidth() / 2 - physicalInnerWidth / 2,
          bitmap.getHeight() / 2 - physicalInnerHeight / 2,
          bitmap.getWidth() / 2 + physicalInnerWidth / 2,
          bitmap.getHeight() / 2 + physicalInnerHeight /2,
          debugPaint);
    }

    if (imageView == null) {
      throw new NullPointerException();
    }

    activity.runOnUiThread(() -> {
      imageView.setImageBitmap(bitmap);
      imageView.requestLayout();
    });

    return bitmap;
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

  public int getLogicalViewportHeight() {
    return logicalViewportHeight;
  }

  public int getLogicalViewportWidth() {
    return logicalViewportWidth;
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
