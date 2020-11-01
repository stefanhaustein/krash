package org.kobjects.krash.api;

public interface DragListener {

  enum DragState {
    START, MOVE, END, CANCEL
  }

  boolean drag(DragState state, float x, float y);
}
