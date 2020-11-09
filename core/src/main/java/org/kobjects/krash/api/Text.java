package org.kobjects.krash.api;

public interface Text extends Content {
  String getText();
  int getColor();
  void setColor(int color);
  float getSize();
  void setSize(float size);

}
