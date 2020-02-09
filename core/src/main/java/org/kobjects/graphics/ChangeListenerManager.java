package org.kobjects.graphics;

import java.util.ArrayList;
import java.util.concurrent.Executor;

public class ChangeListenerManager<T> {

  private final T observed;
  private final Executor executor;
  private final Object lock = new Object();

  ArrayList<ChangeListener<T>> listeners;
  boolean notificationPending;

  ChangeListenerManager(T observed, Executor executor) {
    this.observed = observed;
    this.executor = executor;
  }

  public void addChangeListener(ChangeListener<? extends T> changeListener) {
    synchronized (lock) {
      if (listeners == null) {
        listeners = new ArrayList<>();
      }
      listeners.add((ChangeListener<T>) changeListener);
    }
  }

  public synchronized void notifyChanged() {
    if (listeners != null && !notificationPending) {
      notificationPending = true;
      executor.execute(() -> {
        Thread.yield();
        notificationPending = false;
        synchronized (lock) {
          for (ChangeListener listener : listeners) {
            executor.execute(() -> listener.notifyChanged(observed));
          }
        }
      });
    }
  }
}
