package org.kobjects.krash.android;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.DragListener;
import org.kobjects.krash.api.Sprite;

import java.util.Objects;


public class AndroidSprite<T> extends Sprite {

  private static Canvas testCanvas;
  private static Bitmap testBitmap;
  private BubbleDrawable bubbleDrawable;
  private AndroidContent content;
  private AndroidScreen screen;

  public View view;

  AndroidSprite(AndroidScreen screen, Content content) {
    super(screen, content);
    this.screen = screen;
  }


  @Override
  public Content getContent() {
    return content;
  }

  void fillDistanceArrayForRect() {
    for (int i = 0; i < 64; i++) {
      distances[i] = 0;
      float deg = i * 360 / 64;
      float dx = clockwiseDegToDx(deg);
      float dy = clockwiseDegToDy(deg);
      for (int distance = (int) Math.sqrt(2 * 32 * 32); distance > 0; distance -= 2) {
        int x = 32 + (int) (dx * distance);
        int y = 32 + (int) (dy * distance);
        if (x >= 0 && y >= 0 && x < 64 && y < 64) {
          distances[i] = distance/32f;
          break;
        }
      }
    }
  }

  void fillDistanceArray(Drawable drawable) {
    if (testCanvas == null) {
      testBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
      testCanvas = new Canvas(testBitmap);
    }

    Paint clearPaint = new Paint();
    clearPaint.setColor(0);
    clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

    testCanvas.drawRect(0, 0, 64, 64, clearPaint);

    ScaleDrawable scaleDrawable = new ScaleDrawable(drawable, Gravity.CENTER, 64, 64);
    scaleDrawable.setLevel(10000);
    scaleDrawable.setBounds(0, 0, 64, 64);
    scaleDrawable.draw(testCanvas);

    for (int i = 0; i < 64; i++) {
      distances[i] = 0;
      float deg = i * 360 / 64;
      float dx = clockwiseDegToDx(deg);
      float dy = clockwiseDegToDy(deg);
      for (int distance = (int) Math.sqrt(2 * 32 * 32); distance > 0; distance -= 2) {
        int x = 32 + (int) (dx * distance);
        int y = 32 + (int) (dy * distance);
        if (x >= 0 && y >= 0 && x < 64 && y < 64) {
          if (testBitmap.getPixel(x, y) != 0) {
            distances[i] = distance/32f;
            break;
          } else {
            testBitmap.setPixel(x, y, i < 8 ? 0xff00ff00 : i < 16 ? 0xff0000ff : 0xffff0000);
          }
        }
      }
    }
  }

  public void setBitmap(Bitmap bitmap) {
    setContent(new AndroidBitmapContent(screen, bitmap));
  }


  public boolean setContentImpl(Content content) {
    synchronized (lock) {
      if (Objects.equals(content, this.content)) {
        return false;
      }

      this.content = (AndroidContent) content;
      requestSync(CONTENT_CHANGED);

      // Order is important here currently, as a size change without content change notification
      // will error
      adjustSize(SizeComponent.NONE);
      return true;
    }
  }

  @Override
  protected void adjustSize(SizeComponent sizeComponent) {
    manualSizeComponents.add(sizeComponent);
    this.setAdjustedSize(content.adjustSize(getWidth(), getHeight(), sizeComponent));
    requestSync(SIZE_CHANGED);
  }
  

  protected void syncNative(Matrix matrix) {
    synchronized (lock) {
      int changedProperties = this.changedProperties;
      this.changedProperties = 0;

      if (view == null || (changedProperties & CONTENT_CHANGED) != 0) {
        if (view != null) {
          screen.getView().removeView(view);
        }
        view = content.createView();

        if (content instanceof AndroidDrawableContent) {
          fillDistanceArray(((AndroidDrawableContent) content).getDrawable());
        } else {
          fillDistanceArrayForRect();
        }
      }
      content.sync(view);

      view.setVisibility(visible ? View.VISIBLE : View.GONE);
      view.setAlpha(opacity);
      // visible is used internally to handle bubble visibility and to remove everything on clear, so it
      // gets special treatment here.


      boolean shouldBeAttached = this.visible;
      boolean attached = view.getParent() != null;
      if (shouldBeAttached != attached) {
        if (shouldBeAttached) {
          screen.getView().addView(view);
        } else {
          screen.getView().removeView(view);
        }
      }


      if ((changedProperties & (CONTENT_CHANGED | SIZE_CHANGED)) != 0) {
          int pixelWidth = Math.round(screen.scale * getWidth());
          int pixelHeight = Math.round(screen.scale * getHeight());

          // view.wrapped.setBackgroundColor((int) (Math.random() * 0xffffff) | 0xff000000);
          view.setLayoutParams(new FrameLayout.LayoutParams(pixelWidth, pixelHeight));
          view.requestLayout();
      }

      if ((changedProperties & (CONTENT_CHANGED | LISTENERS_CHANGED)) != 0 && dragListeners != null && !dragListeners.isEmpty()) {
        view.setOnTouchListener(new View.OnTouchListener() {
          @Override
          public boolean onTouch(View v, MotionEvent event) {
            final float x = event.getX() / screen.scale;
            final float y = event.getY() / screen.scale;
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
              case MotionEvent.ACTION_DOWN:
                return notifyDragged(DragListener.DragState.START, x, y);
              case MotionEvent.ACTION_UP:
                return notifyDragged(DragListener.DragState.END, x, y);
              case MotionEvent.ACTION_MOVE:
                return notifyDragged(DragListener.DragState.MOVE, x, y);
              case MotionEvent.ACTION_CANCEL:
                return notifyDragged(DragListener.DragState.CANCEL, x, y);
            }
            return false;
          }
        });
      }


      float[] values = new float[9];
      matrix.getValues(values);

      view.setTranslationX(values[Matrix.MTRANS_X]);
      view.setTranslationY(values[Matrix.MTRANS_Y]);
      view.setTranslationZ(z);
      view.setRotation(getAngle());

      if (changeListeners != null) {
        synchronized (changeListeners) {
          for (Runnable changeListener : (Iterable<Runnable>) changeListeners) {
            changeListener.run();
          }
        }
      }
      this.changedProperties = 0;
    }
  }


  @Override
  public void requestSync(int newChangedProperties) {
    synchronized (lock) {
      this.changedProperties |= newChangedProperties;
    }
  }


  public View getView() {
    return view;
  }

}
