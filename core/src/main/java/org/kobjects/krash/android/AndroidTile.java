package org.kobjects.krash.android;

import android.view.View;

import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.Tile;

public class AndroidTile<T extends Content> implements Tile<T> {



  final AndroidGrid<T> grid;
  final int x;
  final int y;
  T content;
  int changedAt;
  Orientation orientation = Orientation.ORIGINAL;

  AndroidTile(AndroidGrid<T> grid, int x, int y) {
    this.grid = grid;
    this.x = x;
    this.y = y;
  }

  @Override
  public void setContent(T content) {
    changedAt = grid.screen.stamp;
    this.content = content;
  }



  @Override
  public T getContent() {
    return content;
  }

  public void setBackgroundColor(int color) {
    grid.backgroundBitmap.setPixel(x, y, color);
  }

  @Override
  public int getBackgroundColor() {
    return grid.backgroundBitmap.getPixel(x, y);
  }

  @Override
  public void setOrientation(Orientation orientation) {
    this.orientation = orientation;
    this.changedAt = grid.screen.stamp;
  }

  @Override
  public Orientation getOrientation() {
    return orientation;
  }


  View sync(View oldView) {
    AndroidContent content = (AndroidContent) this.content;
    if (oldView != null && !grid.screen.needsSync(changedAt)) {
      if (content != null) {
        content.sync(oldView);
      }
      return oldView;
    }
    if (content == null) {
      return new View(grid.screen.activity);
    }
    View view = content.createView();
    switch (orientation) {
      case ROT_90:
        view.setRotation(90f);
        break;
      case ROT_180:
        view.setRotation(180f);
        break;
      case ROT_270:
        view.setRotation(270f);
        break;
    }
    return view;
  }
}
