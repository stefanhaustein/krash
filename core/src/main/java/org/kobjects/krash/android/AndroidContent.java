package org.kobjects.krash.android;

import android.view.View;

import org.kobjects.krash.api.Content;

interface AndroidContent extends Content {
  float[] adjustSize(float width, float height, AndroidSprite.SizeComponent sizeComponent);

  View createView();

  default void sync(View view) {}
}
