package org.kobjects.krash.android;

import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.kobjects.krash.api.Sprite;
import org.kobjects.krash.api.TextContent;

public class AndrodTextContent implements AndroidContent, TextContent {
  private final AndroidScreen screen;
  private String text;
  private TextPaint paint = new TextPaint();

  public AndrodTextContent(AndroidScreen screen, String text) {
    this.screen = screen;
    this.text = text;
  }

  @Override
  public View getView() {
    TextView textView = new TextView(screen.activity);
    textView.setText(text);
    return textView;
  }

  @Override
  public void adjustSize(AndroidSprite sprite, AndroidSprite.SizeComponent sizeComponent) {
    float size = sizeComponent == Sprite.SizeComponent.NONE ? 10 : sprite.getSize();

    float width = sprite.getWidth();
    float height = sprite.getHeight();

    if (!sprite.getManualSizeComponents().contains(Sprite.SizeComponent.HEIGHT)) {
      paint.setTextSize(size * screen.scale);
      paint.setTypeface(Typeface.DEFAULT);

      if (!sprite.getManualSizeComponents().contains(Sprite.SizeComponent.WIDTH)) {
        width = Math.min((paint.measureText(text) + 2) / screen.scale, 70);
        StaticLayout staticLayout = new StaticLayout(text, paint, (int) (width * screen.scale), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        height = staticLayout.getHeight() / screen.scale;
      }
    }

    sprite.setAdjustedSize(width, height, size);
  }

  @Override
  public void sync(AndroidSprite sprite) {
    TextView textView = ((TextView) sprite.view.wrapped);
    textView.setTextColor(sprite.getTextColor());
    textView.setPadding(0, 0, 0, 0);
    textView.setLineSpacing(0, 0);
    textView.setIncludeFontPadding(false);
//    textView.setFirstBaselineToTopHeight(0);
    textView.setTypeface(Typeface.DEFAULT);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sprite.getSize() * screen.scale);
  }

  @Override
  public String getText() {
    return text;
  }
}
