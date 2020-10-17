package org.kobjects.krash;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import org.kobjects.krash.api.Content;

import java.util.Objects;


public class AndroidSprite extends Sprite<View> {

  private static Canvas testCanvas;
  private static Bitmap testBitmap;
  private BubbleDrawable bubbleDrawable;
  private AndroidContent content;

  AndroidSprite(Screen screen) {
    super(screen, new AppCompatImageView(screen.activity));


    setContent(new AndroidEmojiContent(screen, DEFAULT_FACE));
  }

  @Override
  boolean shouldBeAttached() {
    // Top level sprites without children will get checked for physical removal
    if (view.getChildCount() == 1 && anchor instanceof Screen) {
      // width / height swap is intended here: ranges go up to the double of the opposite dimension
      float size = Math.max(width, height);
      return opacity > MIN_OPACITY
          && x - size / 2 < screen.getLogicalViewportHeight() && x + size / 2 > -screen.getLogicalViewportHeight()
          && y - size / 2 < screen.getLogicalViewportWidth() && y + size / 2 > -screen.getLogicalViewportWidth();
    }
    return super.shouldBeAttached();
  }

  @Override
  protected Content getContent() {
    return content;
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


  public boolean setContent(Content content) {
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
  public void syncUi(int changedProperties) {
    synchronized (lock) {

      if ((changedProperties & CONTENT_CHANGED) != 0) {
        if (content instanceof AndroidDrawableContent) {
          ImageView imageView = new ImageView(screen.activity);
          Drawable drawable = ((AndroidDrawableContent) content).getDrawable();
          imageView.setLayerType(drawable instanceof SvgDrawable ? View.LAYER_TYPE_SOFTWARE : View.LAYER_TYPE_HARDWARE, null);

          fillDistanceArray(drawable);
          imageView.setImageDrawable(drawable);

          imageView.setAdjustViewBounds(true);
          imageView.setScaleType(ImageView.ScaleType.FIT_XY);

          view.setWrapped(imageView);
        } else if (content instanceof AndroidViewContent) {
          view.setWrapped(((AndroidViewContent) content).getView());
          adjustSize(SizeComponent.SIZE);
        } else {
          throw new IllegalStateException();
        }

        content.sync(this);
      }

      if ((changedProperties & SIZE_CHANGED) != 0) {
        content.sync(this);

        int pixelWidth = Math.round(screen.scale * width);
        int pixelHeight = Math.round(screen.scale * height);

   /*   if (face != null && !face.isEmpty()) {
        synchronized (svgCache) {
          SVG svg = svgCache.get(face);
          if (svg != null) {
            view.wrapped.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            view.wrapped.setImageDrawable(new PictureDrawable(svg.renderToPicture(pixelSize, pixelSize)));
          }
        }
      }*/


        if (getCornerRadius() == 0 && (getLineColor() == 0 || getLineWidth() == 0)) {
          if (bubbleDrawable != null) {
            view.wrapped.setBackground(null);
            //     view.wrapped.setClipToOutline(true);
            bubbleDrawable = null;
          }
          view.wrapped.setBackgroundColor(getFillColor());
        } else {
          if (bubbleDrawable == null) {
            bubbleDrawable = new BubbleDrawable();
            view.wrapped.setBackground(bubbleDrawable);
            view.wrapped.setClipToOutline(false);
            //      view.setClipChildren(false);
          }
          bubbleDrawable.cornerBox = getCornerRadius() * 2 * screen.scale;
          bubbleDrawable.strokePaint.setColor(getLineColor());
          bubbleDrawable.strokePaint.setStrokeWidth(getLineWidth() * screen.scale);
          bubbleDrawable.backgroundPaint.setColor(getFillColor());

          if (yAlign == YAlign.BOTTOM && anchor != screen && getY() > 0) {
            bubbleDrawable.arrowDy = screen.scale * y;
            bubbleDrawable.arrowDx = screen.scale * -x / 2;
          } else {
            bubbleDrawable.arrowDy = 0;
            bubbleDrawable.arrowDx = 0;
          }
          bubbleDrawable.invalidateSelf();
//            view.wrapped.invalidate();
        }


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
    content.adjustSize(this, sizeComponent);
    requestSync(SIZE_CHANGED);
  }


}
