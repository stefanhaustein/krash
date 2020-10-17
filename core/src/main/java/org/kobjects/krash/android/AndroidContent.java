package org.kobjects.krash.android;

import org.kobjects.krash.api.Content;

public interface AndroidContent extends Content {
  void adjustSize(AndroidSprite sprite, AndroidSprite.SizeComponent sizeComponent);

  default void sync(AndroidSprite sprite) {}
}
