package org.kobjects.krash.android;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.Grid;
import org.kobjects.krash.api.Tile;

public class AndroidGrid<T extends Content> implements Grid<T>, AndroidContent {

  private int columnCount;
  private int rowCount;
  private AndroidTile[][] tiles;
  final AndroidScreen screen;
  Bitmap backgroundBitmap;

  public AndroidGrid(AndroidScreen screen, int columnCount, int rowCount) {
    this.screen = screen;
    this.columnCount = columnCount;
    this.rowCount = rowCount;
    tiles = new AndroidTile[rowCount][];
    for (int y = 0; y < rowCount; y++) {
      tiles[y] = new AndroidTile[columnCount];
      for (int x = 0; x < columnCount; x++) {
        tiles[y][x] = new AndroidTile<T>(this, x, y);
      }
    }
    backgroundBitmap = Bitmap.createBitmap(columnCount, rowCount, Bitmap.Config.ARGB_8888);
  }

  @Override
  public Tile<T> tile(int x, int y) {
    return tiles[y][x];
  }

  LinearLayout.LayoutParams createCellLayoutParams() {
    LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(0, TableRow.LayoutParams.MATCH_PARENT);
    layoutParams.weight = 1;
    return layoutParams;
  }

  @Override
  public View createView() {
    LinearLayout tableLayout = new LinearLayout(screen.activity);
    tableLayout.setOrientation(LinearLayout.VERTICAL);
//    tableLayout.setStretchAllColumns(true);

    BitmapDrawable bitmapDrawable = new BitmapDrawable(backgroundBitmap);
    bitmapDrawable.setAntiAlias(false);
    bitmapDrawable.setFilterBitmap(false);
    tableLayout.setBackground(bitmapDrawable);

    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      AndroidTile[] row = tiles[rowIndex];
      LinearLayout tableRow = new LinearLayout(screen.activity);
      if (row != null) {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
          AndroidTile tile = row[columnIndex];
          View view = tile.sync(null);
          tableRow.addView(view, createCellLayoutParams());
        }
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        layoutParams.weight = 1;
        tableLayout.addView(tableRow, layoutParams);
      }
    }
    return tableLayout;
  }

  float getIntrinsicWidth() {
    return 10 * columnCount;
  }

  float getIntrinsicHeight() {
    return 10 * rowCount;
  }

  public void sync(View gridView) {
    LinearLayout tableLayout = (LinearLayout) gridView;
    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      AndroidTile[] row = tiles[rowIndex];
      LinearLayout tableRow = (LinearLayout) tableLayout.getChildAt(rowIndex);
      if (row != null) {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
          AndroidTile tile = row[columnIndex];
          View oldView = tableRow.getChildAt(columnIndex);
          View newView = tile.sync(oldView);
          if (oldView != newView) {
            tableRow.removeViewAt(columnIndex);
            tableRow.addView(newView, columnIndex, createCellLayoutParams());
          }
        }
      }
    }
  }

  @Override
  public float[] adjustSize(float width, float height, AndroidSprite.SizeComponent sizeComponent) {
    switch (sizeComponent) {
      case NONE:
        return new float[] {getIntrinsicWidth(), getIntrinsicHeight()};
      case WIDTH: {
        float scale = width / getIntrinsicWidth();
        return new float[] {width, getIntrinsicHeight() * scale};
      }
      case HEIGHT: {
        float scale = height / getIntrinsicHeight();
        return new float[] {getIntrinsicWidth() * scale, height};

      }
      default:
        throw new IllegalArgumentException();
    }
  }


}
