package org.kobjects.krash.api;

public interface Tile<T extends Content> {
  void setContent(T content);

  void setBackgroundColor(int color);
}
