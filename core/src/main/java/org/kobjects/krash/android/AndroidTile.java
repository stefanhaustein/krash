package org.kobjects.krash.android;

import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.Tile;

public class AndroidTile<T extends Content> implements Tile<T> {

  enum Orientation {
    ORIGINAL,
    ROT_90,
    ROT_180,
    ROT_270
  }


  final AndroidGrid<T> grid;
  final int x;
  final int y;
  T content;

  AndroidTile(AndroidGrid<T> grid, int x, int y) {
    this.grid = grid;
    this.x = x;
    this.y = y;
  }

  @Override
  public void setContent(T content) {
    this.content = content;
  }

  public void setBackgroundColor(int color) {
    grid.backgroundBitmap.setPixel(x, y, color);
  }
}
