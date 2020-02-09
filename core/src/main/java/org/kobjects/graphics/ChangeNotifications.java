package org.kobjects.graphics;

public interface ChangeNotifications<T> {
  void addChangeListener(ChangeListener<T> changeListener);
}
