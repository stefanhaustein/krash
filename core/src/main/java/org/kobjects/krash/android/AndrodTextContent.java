package org.kobjects.krash.android;

import android.graphics.Color;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.kobjects.krash.api.Sprite;
import org.kobjects.krash.api.Text;

public class AndrodTextContent implements AndroidContent, Text {
  private final AndroidScreen screen;
  private String text;
  private TextPaint paint = new TextPaint();
  private int color = Color.BLACK;
  private float size = 10;

  public AndrodTextContent(AndroidScreen screen, String text) {
    this.screen = screen;
    this.text = text;
  }

  @Override
  public View createView() {
    TextView textView = new TextView(screen.activity);
    textView.setText(text);
    return textView;
  }

  @Override
  public float[] adjustSize(float width, float height, AndroidSprite.SizeComponent sizeComponent) {
    paint.setTextSize(size * screen.scale);
    paint.setTypeface(Typeface.DEFAULT);

    if (sizeComponent == Sprite.SizeComponent.NONE) {
      width = 100;
    }

    if (sizeComponent != Sprite.SizeComponent.HEIGHT) {
        width = Math.min((paint.measureText(text) + 2) / screen.scale, 70);
        StaticLayout staticLayout = new StaticLayout(text, paint, (int) (width * screen.scale), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        height = staticLayout.getHeight() / screen.scale;
      }

    return new float[]{width, height};
  }

  @Override
  public void sync(View view) {
    TextView textView = ((TextView) view);
    textView.setTextColor(getColor());
    textView.setPadding(0, 0, 0, 0);
    textView.setLineSpacing(0, 0);
    textView.setIncludeFontPadding(false);
//    textView.setFirstBaselineToTopHeight(0);
    textView.setTypeface(Typeface.DEFAULT);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, size * screen.scale);
  }

  @Override
  public String getText() {
    return text;
  }

  @Override
  public int getColor() {
    return color;
  }

  @Override
  public void setColor(int color) {
    this.color = color;
  }

  @Override
  public float getSize() {
    return size;
  }

  @Override
  public void setSize(float size) {
    this.size = size;
  }
}
