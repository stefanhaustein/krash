package org.kobjects.krash.api;

public interface Grid<T extends Content> extends Content {

  Tile<T> tile(int x, int y);
}
