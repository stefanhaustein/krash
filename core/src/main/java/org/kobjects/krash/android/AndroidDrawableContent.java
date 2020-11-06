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


  public View getView() {
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

  public void adjustSize(AndroidSprite sprite, AndroidSprite.SizeComponent sizeComponent) {
    switch (sizeComponent) {
      case NONE:
        sprite.setAdjustedSize(getIntrinsicWidth(), getIntrinsicHeight());
        break;
       case WIDTH: {
        float scale = sprite.getWidth() / getIntrinsicWidth();
        sprite.setAdjustedSize(sprite.getWidth(), getIntrinsicHeight() * scale);
        break;
      }
      case HEIGHT: {
        float scale = sprite.getHeight() / getIntrinsicHeight();
        sprite.setAdjustedSize(getIntrinsicWidth() * scale, sprite.getHeight());
        break;
      }
    }
  }
}
