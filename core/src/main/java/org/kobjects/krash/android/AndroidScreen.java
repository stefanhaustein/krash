package org.kobjects.krash.android;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.jetbrains.annotations.NotNull;
import org.kobjects.krash.api.Bubble;
import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.GamepadKey;
import org.kobjects.krash.api.Grid;
import org.kobjects.krash.api.Screen;
import org.kobjects.krash.api.Sprite;
import org.kobjects.krash.api.Svg;
import org.kobjects.krash.api.Text;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.WeakHashMap;

public class AndroidScreen implements LifecycleObserver, Screen {

  private final static boolean DEBUG = false;
  public int stamp;

  private ImageView imageView;
  private Bitmap bitmap;
  private Timer timer;
  private int logicalViewportHeight = 200;
  private int logicalViewportWidth = 200;
  private boolean physicalPixels;

  private HashMap<Runnable, SchedulerRecord> scheduled = new HashMap<>();
  private HashMap<Runnable, EnumSet<GamepadKey>> gamepadListeners = new HashMap<>();

  private ArrayList<Sprite<?>> children = new ArrayList<>();




  final Activity activity;
  /**
   * Multiply with scale to get from virtual coordinates to px, divide to get from px to
   * virtual coordinates.
   */

  float bitmapScale;
  final Object lock = new Object();

  public final float scale = Resources.getSystem().getDisplayMetrics().density;
  public Dpad dpad;
  private ViewGroup view;
  private View.OnTouchListener internalGamepadListener = new View.OnTouchListener() {
    @Override
    public boolean onTouch(View v, MotionEvent event) {
      if (event.getAction() != MotionEvent.ACTION_DOWN) {
        return false;
      }
      GamepadKey target;
      if (v == dpad.up) {
        target = GamepadKey.UP;
      } else if (v == dpad.down) {
        target = GamepadKey.DOWN;
      } else if (v == dpad.left) {
        target = GamepadKey.LEFT;
      } else if (v == dpad.right) {
        target = GamepadKey.RIGHT;
      } else {
        return false;
      }
      for (Map.Entry<Runnable, EnumSet<GamepadKey>> entry : gamepadListeners.entrySet()) {
        if (entry.getValue().contains(target)) {
          entry.getKey().run();
        }
      }
      return true;
    }
  };


  /**
   * Contains all positioned view holders including children.
   */
  Set<AndroidSprite> allWidgets = Collections.newSetFromMap(new WeakHashMap<>());


  public AndroidScreen(AppCompatActivity activity) {

    EmojiManager.install(new TwitterEmojiProvider());

    view = new FrameLayout(activity);
    this.activity = activity;
    view.setClipChildren(false);
    activity.getLifecycle().addObserver(this);

    dpad = new Dpad(this);

    view.addOnLayoutChangeListener((viw, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
        if (left != oldLeft || right != oldRight || top != oldTop || bottom != oldBottom) {

          dpad.requestSync();
          synchronized (lock) {
            for (AndroidSprite widget : allWidgets) {
              widget.requestSync(Sprite.SIZE_CHANGED);
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
        Bitmap bitmap = AndroidScreen.this.bitmap;
        if (bitmap != null) {
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
      }
    };
    imageView.setScaleType(ImageView.ScaleType.MATRIX);

    view.addView(imageView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
  }

  public Pen createPen() {
    return new Pen(this);
  }

  public void resetViewport() {
    setViewport(200, 200, false);
  }

  public void setViewport(int logicalViewportWidth, int logicalViewportHeight, boolean physicalPixels) {
    synchronized (lock) {
      if (logicalViewportWidth != this.logicalViewportWidth
          || logicalViewportHeight != this.logicalViewportHeight
          || physicalPixels != this.physicalPixels) {
        this.physicalPixels = physicalPixels;
        this.logicalViewportHeight = logicalViewportHeight;
        this.logicalViewportWidth = logicalViewportWidth;
        bitmap = null;
      }
    }
  }


  public Bitmap getBitmap() {
    synchronized (lock) {
      if (bitmap == null) {
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
              bitmap.getHeight() / 2 + physicalInnerHeight / 2,
              debugPaint);
        }

        if (imageView == null) {
          throw new NullPointerException();
        }

        activity.runOnUiThread(() -> {
          BitmapDrawable bitmapDrawable = new BitmapDrawable(view.getResources(), bitmap);
          bitmapDrawable.setFilterBitmap(!physicalPixels);
          imageView.setImageDrawable(bitmapDrawable);
          imageView.requestLayout();
        });
      }
      imageView.postInvalidate();
      return bitmap;
    }
  }



  public boolean dispatchKeyEvent(android.view.KeyEvent keyEvent) {
    System.out.println("KeyEvent: " + keyEvent);
    return false;
  }

  public void clearAll() {
    synchronized (lock) {
      cls();
      for (Sprite widget : allWidgets) {
        widget.setVisible(false);
      }
      allWidgets.clear();
    }
  }

  public void cls() {
    synchronized (lock) {
      if (bitmap != null) {
        bitmap.eraseColor(0);
      }
    }
    dpad.setVisible(false);
  }

  public <T extends Content> AndroidSprite createSprite(T content) {
    synchronized (lock) {
      AndroidSprite sprite = new AndroidSprite(this, content);
      allWidgets.add(sprite);
      return sprite;
    }
  }

  @Override
  public float getWidth() {
    return view.getWidth() / scale;
  }

  @Override
  public float getHeight() {
    return view.getHeight() / scale;
  }

  @Override
  public Object getTag() {
    return view.getTag();
  }

  public int getLogicalViewportHeight() {
    return logicalViewportHeight;
  }

  public int getLogicalViewportWidth() {
    return logicalViewportWidth;
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
  public void onResume() {
    synchronized (lock) {
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
      }, 0, 1000 / 60);
    }
  }

  @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
  public void onPause() {
    synchronized (lock) {
      timer.cancel();
      timer = null;
    }
  }

  private void animate(float dt) {
    ArrayList<Runnable> toRun = new ArrayList<>();
    synchronized (lock) {
      for (SchedulerRecord schedulerRecord : scheduled.values()) {
        schedulerRecord.passed += dt/1000f;
        while (schedulerRecord.passed > schedulerRecord.interval) {
          toRun.add(schedulerRecord.task);
          schedulerRecord.passed -= schedulerRecord.interval;
        }
      }
    }

    for (Runnable runnable : toRun) {
      runnable.run();
    }

    ArrayList<Sprite> copy = new ArrayList<>(children.size());
    synchronized (lock) {
      copy.addAll(children);
    }
    activity.runOnUiThread(() -> {
      stamp++;
      Matrix transformation = new Matrix();
      transformation.setScale(scale, scale);
      for (Sprite widget : copy) {
        if (widget instanceof AndroidSprite) {
          widget.sync(dt, transformation);
        }
      }
    });
  }

  float rawXToScreen(float rawX) {
    int[] loc = new int[2];
    view.getLocationOnScreen(loc);
    return ((rawX - loc[0]) - view.getWidth() / 2) / scale;
  }

  float rawYToScreen(float rawX) {
    int[] loc = new int[2];
    view.getLocationOnScreen(loc);
    return (view.getHeight() / 2 - (rawX - loc[0])) / scale;
  }

  @Override
  public AndroidEmojiContent createEmoji(String codepoint) {
    return new AndroidEmojiContent(this, codepoint);
  }

  @Override
  public Object getLock() {
    return lock;
  }

  @Override
  public Iterable<Sprite> allSprites() {
    ArrayList<Sprite> copy = new ArrayList<>(allWidgets.size());
    synchronized (lock) {
      copy.addAll(allWidgets);
    }
    return copy;
  }

  @Override
  public Text createText(String text) {
    return new AndrodTextContent(this, text);
  }

  @Override
  public Grid createGrid(int width, int height) {
    return new AndroidGrid(this, width, height);
  }

  @Override
  public Svg createSvg(String svg) {
    return new AndroidSvgContent(this, svg);
  }

  @Override
  public Bubble createBubble() {
    return new AndroidBubbleContent(this);
  }

  @Override
  public void schedule(Float interval, Runnable task) {
    synchronized (lock) {
      scheduled.put(task, new SchedulerRecord(interval, task));
    }
  }

  @Override
  public void unSchedule(Runnable task) {
    synchronized (lock) {
      scheduled.remove(task);
    }
  }

  @Override
  public void addKeyDownListener(GamepadKey key, Runnable listener) {
    if (gamepadListeners.size() == 0) {
      dpad.setVisible(true);
      dpad.up.setOnTouchListener(internalGamepadListener);
      dpad.down.setOnTouchListener(internalGamepadListener);
      dpad.left.setOnTouchListener(internalGamepadListener);
      dpad.right.setOnTouchListener(internalGamepadListener);
      dpad.fire.setOnTouchListener(internalGamepadListener);
    }

    EnumSet<GamepadKey> old = gamepadListeners.get(listener);
    if (old == null) {
      gamepadListeners.put(listener, EnumSet.of(key));
    } else {
      old.add(key);
    }
  }


  public ViewGroup getView() {
    return view;
  }

  boolean needsSync(int changedAt) {
    return changedAt >= stamp - 1;

  }

  @Override
  public void addChild(@NotNull Sprite<? extends Content> child) {
    children.add(child);
  }

  @Override
  public void removeChild(@NotNull Sprite<? extends Content> child) {
    children.remove(child);
  }


  static class SchedulerRecord {
    final Runnable task;
    final float interval;
    float passed;
    SchedulerRecord(float interval, Runnable task) {
      this.task = task;
      this.interval = interval;
    }
  }

}
