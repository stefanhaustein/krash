package org.kobjects.krash.api;

public interface Anchor {

  /**
   * Returns the normalized width of the view. For the screen, this value is negative.
   */
  float getWidth();

  /**
   * Returns the normalized height of the view. For the screen, this value is negative.
   */
  float getHeight();

  Object getTag();
}