package org.kobjects.krash;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AndroidBitmapContent implements AndroidDrawableContent {

  Bitmap bitmap;
  Screen screen;

  AndroidBitmapContent(Screen screen, Bitmap bitmap) {
    this.screen = screen;
    this.bitmap = bitmap;
  }


  @Override
  public Drawable getDrawable() {
    BitmapDrawable bitmapDrawable = new BitmapDrawable(screen.view.getResources(), bitmap);
    bitmapDrawable.setFilterBitmap(false);
    // bitmapDrawable.setAntiAlias(false);
    return bitmapDrawable;
  }

  @Override
  public float getIntrinsicWidth() {
    return bitmap.getWidth();
  }

  @Override
  public float getIntrinsicHeight() {
    return bitmap.getHeight();
  }
}
