package org.kobjects.krash.android;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

public class AndroidBitmapContent extends AndroidDrawableContent {

  Bitmap bitmap;

  AndroidBitmapContent(AndroidScreen screen, Bitmap bitmap) {
    super(screen);
    this.bitmap = bitmap;
  }


  @Override
  public Drawable getDrawable() {
    BitmapDrawable bitmapDrawable = new BitmapDrawable(screen.getView().getResources(), bitmap);
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
