package org.kobjects.krash;

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
        sprite.height = getIntrinsicHeight();
        sprite.width = getIntrinsicWidth();
        break;
      case SIZE: {
        float intrinsicWidth = getIntrinsicWidth();
        float intrinsicHeight = getIntrinsicHeight();
        float intrinsicSize = (intrinsicWidth + intrinsicHeight) / 2;
        float scale = sprite.size / intrinsicSize;

        sprite.width = intrinsicWidth * scale;
        sprite.height = intrinsicHeight * scale;
        return;
      }
      case WIDTH: {
        float scale = sprite.width / getIntrinsicWidth();
        sprite.height = getIntrinsicHeight() * scale;
        break;
      }
      case HEIGHT: {
        float scale = sprite.height / getIntrinsicHeight();
        sprite.height = getIntrinsicHeight() * scale;
        break;
      }
    }
    sprite.size = (sprite.width + sprite.height) / 2;

  }
}
