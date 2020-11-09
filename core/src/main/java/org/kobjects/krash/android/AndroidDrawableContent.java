package org.kobjects.krash.android;

import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

abstract class AndroidDrawableContent implements AndroidContent {
  final AndroidScreen screen;
  abstract Drawable getDrawable();

  AndroidDrawableContent(AndroidScreen screen) {
    this.screen = screen;
  }


  public View createView() {
    ImageView result = new ImageView(screen.activity);
    Drawable drawable = getDrawable();
    result.setLayerType(drawable instanceof SvgDrawable ? View.LAYER_TYPE_SOFTWARE : View.LAYER_TYPE_HARDWARE, null);

    // fillDistanceArray(drawable);
    result.setImageDrawable(drawable);

    result.setAdjustViewBounds(true);
    result.setScaleType(ImageView.ScaleType.FIT_XY);

    return result;
  }


  float getIntrinsicWidth() {
    return getDrawable().getIntrinsicWidth();
  }

  float getIntrinsicHeight() {
    return getDrawable().getIntrinsicHeight();
  }

  public float[] adjustSize(float width, float height, AndroidSprite.SizeComponent sizeComponent) {
    switch (sizeComponent) {
      case NONE:
        return new float[]{getIntrinsicWidth(), getIntrinsicHeight()};

      case WIDTH: {
        float scale = width / getIntrinsicWidth();
        return new float[]{width, getIntrinsicHeight() * scale};

      }
      case HEIGHT: {
        float scale = height / getIntrinsicHeight();
        return new float[]{getIntrinsicWidth() * scale, height};

      }
      default:
        throw new IllegalArgumentException();
    }
  }
}
