package org.kobjects.krash.android;

import android.view.View;
import android.widget.GridLayout;
import android.widget.TableLayout;
import android.widget.TableRow;

import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.GridContent;

public class AndroidGrid implements GridContent, AndroidContent {

  private int columnCount;
  private int rowCount;
  private AndroidContent[][] grid;
  private final AndroidScreen screen;

  public AndroidGrid(AndroidScreen screen, int columnCount, int rowCount) {
    this.screen = screen;
    this.columnCount = columnCount;
    this.rowCount = rowCount;
    grid = new AndroidContent[rowCount][];
  }

  @Override
  public void set(int x, int y, Content content) {
    if (grid[y] == null) {
      grid[y] = new AndroidContent[columnCount];
    }
    grid[y][x] = (AndroidContent) content;
  }

  @Override
  public View getView() {
    TableLayout tableLayout = new TableLayout(screen.activity);

    for (int rowIndex = 0; rowIndex < rowCount; rowIndex++) {
      AndroidContent[] row = grid[rowIndex];
      TableRow tableRow = new TableRow(screen.activity);
      if (row != null) {
        for (int columnIndex = 0; columnIndex < columnCount; columnIndex++) {
          AndroidContent cell = row[columnIndex];
          if (cell != null) {
            View view = cell.getView();
            tableRow.addView(view);
            TableRow.LayoutParams layoutParams = (TableRow.LayoutParams) view.getLayoutParams();
            layoutParams.weight = 1;
          }
        }
        tableLayout.addView(tableRow);
        TableLayout.LayoutParams layoutParams = (TableLayout.LayoutParams) tableRow.getLayoutParams();
        layoutParams.weight = 1;

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

  public void adjustSize(AndroidSprite sprite, AndroidSprite.SizeComponent sizeComponent) {
    switch (sizeComponent) {
      case NONE:
        sprite.setAdjustedSize(getIntrinsicWidth(), getIntrinsicHeight());
        break;
      case WIDTH: {
        float scale = sprite.getWidth() / getIntrinsicWidth();
        sprite.setAdjustedSize(sprite.getWidth(), getIntrinsicHeight() * scale);
        break;
      }
      case HEIGHT: {
        float scale = sprite.getHeight() / getIntrinsicHeight();
        sprite.setAdjustedSize(getIntrinsicWidth() * scale, sprite.getHeight());
        break;
      }
    }
  }


}
