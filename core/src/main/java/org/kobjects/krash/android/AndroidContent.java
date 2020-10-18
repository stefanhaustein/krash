package org.kobjects.krash.android;

import android.view.View;

import org.kobjects.krash.api.Content;

interface AndroidContent extends Content {
  void adjustSize(AndroidSprite sprite, AndroidSprite.SizeComponent sizeComponent);

  View getView();

  default void sync(AndroidSprite sprite) {}
}
