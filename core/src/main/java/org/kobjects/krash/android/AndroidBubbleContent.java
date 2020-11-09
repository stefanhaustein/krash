package org.kobjects.krash.android;

import android.view.View;
import android.widget.FrameLayout;

import org.jetbrains.annotations.NotNull;
import org.kobjects.krash.api.Bubble;
import org.kobjects.krash.api.Content;

public class AndroidBubbleContent implements Bubble, AndroidContent {

  private final Object lock = new Object();
  private final AndroidScreen screen;
  int lineColor = 0xff000000;
  int fillColor = 0xffffffff;
  float lineWidth = 1;
  float cornerRadius = 5;
  float padding = 5;
  float dX;
  float dY;

  boolean syncNeeded;
  private AndroidContent content;

  AndroidBubbleContent(AndroidScreen screen) {
    this.screen = screen;
  }

  public int getLineColor() {
    return lineColor;
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public int getFillColor() {
    return fillColor;
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  public float getPadding() {
    return padding;
  }


  public void setFillColor(int fillColor) {
    synchronized (lock) {
      if (fillColor != this.fillColor) {
        this.fillColor = fillColor;
        syncNeeded = true;
      }
    }
  }

  public void setLineColor(int lineColor) {
    if (lineColor != this.lineColor) {
      this.lineColor = lineColor;
      syncNeeded = true;
    }
  }

  public void setLineWidth(float lineWidth) {
    if (lineWidth == this.lineWidth) {
      this.lineWidth = lineWidth;
      syncNeeded = true;
    }
  }

  public void setCornerRadius(float cornerRadius) {
    if (cornerRadius == this.cornerRadius) {
      this.cornerRadius = cornerRadius;
      syncNeeded = true;
    }
  }

  public void setPadding(float padding) {
    if (padding == this.padding) {
      this.padding = padding;
      syncNeeded = true;
    }
  }

  @Override
  public float[] adjustSize(float width, float height, AndroidSprite.SizeComponent sizeComponent) {
    float[] result = content.adjustSize(width - 2*padding, height-2*padding, sizeComponent);
    result[0] += 2 * padding;
    result[1] += 2 * padding;
    return result;
  }

  @Override
  public void sync(View view) {
    if (getCornerRadius() == 0 && (getLineColor() == 0 || getLineWidth() == 0) && dX == 0 && dY == 0) {
      view.setBackground(null);
      view.setBackgroundColor(getFillColor());
    } else {
      BubbleDrawable bubbleDrawable = new BubbleDrawable();
        view.setBackground(bubbleDrawable);
        view.setClipToOutline(false);
        //      view.setClipChildren(false);
      bubbleDrawable.cornerBox = getCornerRadius() * 2 * screen.scale;
      bubbleDrawable.strokePaint.setColor(getLineColor());
      bubbleDrawable.strokePaint.setStrokeWidth(getLineWidth() * screen.scale);
      bubbleDrawable.backgroundPaint.setColor(getFillColor());

    /*  if (yAlign == YAlign.BOTTOM && anchor != screen && getY() > 0) {
        bubbleDrawable.arrowDy = screen.scale * y;
        bubbleDrawable.arrowDx = screen.scale * -x / 2;
      } else { */

        bubbleDrawable.arrowDy = screen.scale * dY;
        bubbleDrawable.arrowDx = screen.scale * -dX / 2;
      //}
      bubbleDrawable.invalidateSelf();
//            view.wrapped.invalidate();


    }

    FrameLayout frameLayout = (FrameLayout) view;

    int scaledPadding = (int) (padding * screen.scale);
    View child = frameLayout.getChildAt(0);
    FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) child.getLayoutParams();
    layoutParams.leftMargin = scaledPadding;
    layoutParams.rightMargin = scaledPadding;
    layoutParams.topMargin = scaledPadding;
    layoutParams.bottomMargin = scaledPadding;

    content.sync(frameLayout.getChildAt(0));

  }

  @Override
  public View createView() {
    FrameLayout result = new FrameLayout(screen.activity);
    result.addView(content.createView());
    return result;
  }

  @NotNull
  @Override
  public Content getContent() {
    return content;
  }

  @Override
  public void setContent(Content content) {
    this.content = (AndroidContent) content;
    syncNeeded = true;
  }

  @Override
  public float getDX() {
    return dX;
  }

  @Override
  public void setDX(float dX) {
    if (dX != this.dX) {
      this.dX = dX;
      syncNeeded = true;
    }
  }

  @Override
  public float getDY() {
    return dY;
  }

  @Override
  public void setDY(float dY) {
    if (dY != this.dY) {
      this.dY = dY;
      syncNeeded = true;
    }
  }
}
