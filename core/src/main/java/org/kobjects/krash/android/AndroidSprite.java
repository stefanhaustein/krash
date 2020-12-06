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
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.appcompat.widget.AppCompatImageView;

import org.jetbrains.annotations.NotNull;
import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.DragListener;
import org.kobjects.krash.api.Sprite;

import java.util.Objects;


public class AndroidSprite<T> extends Sprite implements AndroidAnchor {

  private static Canvas testCanvas;
  private static Bitmap testBitmap;
  private BubbleDrawable bubbleDrawable;
  private AndroidContent content;
  private AndroidScreen screen;

  public AnchorLayout<View> view;

  AndroidSprite(AndroidScreen screen, Content content) {
    super(screen, content);
    this.screen = screen;

    view = new AnchorLayout<>(new AppCompatImageView(screen.activity));
    view.setTag(this);
  }

  @Override
  protected boolean shouldBeAttached() {
    // Top level sprites without children will get checked for physical removal
    if (view.getChildCount() == 1 && getAnchor() instanceof AndroidScreen) {
      // width / height swap is intended here: ranges go up to the double of the opposite dimension
      float size = Math.max(getWidth(), getHeight());
      return opacity > MIN_OPACITY
          && x - size / 2 < screen.getLogicalViewportHeight() && x + size / 2 > -screen.getLogicalViewportHeight()
          && y - size / 2 < screen.getLogicalViewportWidth() && y + size / 2 > -screen.getLogicalViewportWidth();
    }
    return super.shouldBeAttached();
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

  void syncUi(int changedProperties) {
    synchronized (lock) {

      if ((changedProperties & CONTENT_CHANGED) != 0) {
      /*  if (content instanceof AndroidDrawableContent) {
          ImageView imageView = new ImageView(screen.activity);
          Drawable drawable = ((AndroidDrawableContent) content).getDrawable();
          imageView.setLayerType(drawable instanceof SvgDrawable ? View.LAYER_TYPE_SOFTWARE : View.LAYER_TYPE_HARDWARE, null);

          fillDistanceArray(drawable);
          imageView.setImageDrawable(drawable);

          imageView.setAdjustViewBounds(true);
          imageView.setScaleType(ImageView.ScaleType.FIT_XY);

          view.setWrapped(imageView);
        } else if (content instanceof AndroidViewContent) { */
          view.setWrapped(content.createView());
      //    adjustSize(SizeComponent.SIZE);
/*        } else {
          throw new IllegalStateException();
        }*/

        content.sync(view.wrapped);

        if (content instanceof AndroidDrawableContent) {
          fillDistanceArray(((AndroidDrawableContent) content).getDrawable());
        } else {
          fillDistanceArrayForRect();
        }
      } else {
        content.sync(view.wrapped);
      }

      if ((changedProperties & SIZE_CHANGED) != 0) {
        int pixelWidth = Math.round(screen.scale * getWidth());
        int pixelHeight = Math.round(screen.scale * getHeight());

        // view.wrapped.setBackgroundColor((int) (Math.random() * 0xffffff) | 0xff000000);
        view.wrapped.setLayoutParams(new FrameLayout.LayoutParams(pixelWidth, pixelHeight));
        view.wrapped.requestLayout();
        view.requestLayout();


      }
      view.wrapped.setRotation(getAngle());
    }
  }

  @Override
  protected void adjustSize(SizeComponent sizeComponent) {
    manualSizeComponents.add(sizeComponent);
    this.setAdjustedSize(content.adjustSize(getWidth(), getHeight(), sizeComponent));
    requestSync(SIZE_CHANGED);
  }


  @Override
  protected void addDragListenerImpl() {
    view.setOnTouchListener(new View.OnTouchListener() {
      @Override
      public boolean onTouch(View v, MotionEvent event) {
        final float x = (event.getX() - view.getWidth() / 2) / screen.scale;
        final float y = (view.getHeight() / 2 - event.getY()) / screen.scale;
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

  protected void syncNative(Matrix matrix) {
    synchronized (lock) {
      int changedProperties = this.changedProperties;
     this.changedProperties = 0;
      view.setVisibility(visible ? View.VISIBLE : View.GONE);
      view.wrapped.setAlpha(opacity);
      // visible is used internally to handle bubble visibility and to remove everything on clear, so it
      // gets special treatment here.
      boolean shouldBeAttached = visible && shouldBeAttached();
      ViewGroup expectedParent = shouldBeAttached ? screen.getView() : null;
      if (view.getParent() != expectedParent) {
        if (view.getParent() != null) {
          ((ViewGroup) view.getParent()).removeView(view);
        }
        if (expectedParent == null) {
          return;
        }
        expectedParent.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
      }
      syncUi(changedProperties);

      float[] values = new float[9];
      matrix.getValues(values);
      view.setTranslationX(values[Matrix.MTRANS_X]);
      view.setTranslationY(values[Matrix.MTRANS_Y]);

      view.setTranslationZ(z);



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


  @Override
  public ViewGroup getView() {
    return view;
  }

}
