package org.kobjects.krash.api;

public interface Tile<T extends Content> {

  enum Orientation {
    ORIGINAL,
    ROT_90,
    ROT_180,
    ROT_270
  }

  void setContent(T content);
  T getContent();

  void setBackgroundColor(int color);
  int getBackgroundColor();

  void setOrientation(Orientation orientation);
  Orientation getOrientation();
}
