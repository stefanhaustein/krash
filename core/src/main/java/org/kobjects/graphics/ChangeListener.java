package org.kobjects.graphics;

public interface ChangeListener<T> {
  ChangeListener[] EMPTY_ARRAY = new ChangeListener[0];

  void notifyChanged(T object);
}
