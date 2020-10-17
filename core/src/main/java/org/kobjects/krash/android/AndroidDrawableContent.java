package org.kobjects.krash.android;

import android.graphics.drawable.Drawable;

public interface AndroidDrawableContent extends AndroidContent {
  Drawable getDrawable();


  default float getIntrinsicWidth() {
    return getDrawable().getIntrinsicWidth();
  }

  default float getIntrinsicHeight() {
    return getDrawable().getIntrinsicHeight();
  }

  default void adjustSize(AndroidSprite sprite, AndroidSprite.SizeComponent sizeComponent) {
    switch (sizeComponent) {
      case NONE:
        sprite.setAdjustedSize(getIntrinsicWidth(), getIntrinsicHeight());
        break;
      case SIZE: {
        float intrinsicWidth = getIntrinsicWidth();
        float intrinsicHeight = getIntrinsicHeight();
        float intrinsicSize = (intrinsicWidth + intrinsicHeight) / 2;
        float scale = sprite.getSize() / intrinsicSize;

        sprite.setAdjustedSize(intrinsicWidth * scale, intrinsicHeight * scale, sprite.getSize());
        break;
      }
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
