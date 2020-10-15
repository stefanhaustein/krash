package org.kobjects.krash;

import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

public class AndrodTextContent implements AndroidViewContent {
  private final Screen screen;
  private String text;
  private TextPaint paint = new TextPaint();
  private StaticLayout.Builder staticLayoutBuilder;

  public AndrodTextContent(Screen screen, String text) {
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
    if (sizeComponent == Sprite.SizeComponent.NONE) {
      sprite.size = 10;
    }
    if (!sprite.manualSizeComponents.contains(Sprite.SizeComponent.HEIGHT)) {
      paint.setTextSize(sprite.size * screen.scale);
      paint.setTypeface(Typeface.DEFAULT);

      if (!sprite.manualSizeComponents.contains(Sprite.SizeComponent.WIDTH)) {
        sprite.width = Math.min((paint.measureText(text) + 2) / screen.scale, 70);
        StaticLayout staticLayout = new StaticLayout(text, paint, (int) (sprite.width * screen.scale), Layout.Alignment.ALIGN_NORMAL, 1, 1, false);
        sprite.height = staticLayout.getHeight() / screen.scale;
      }
    }
  }

  @Override
  public void sync(AndroidSprite sprite) {
    TextView textView = ((TextView) sprite.view.wrapped);
    textView.setTextColor(sprite.textColor);
    textView.setPadding(0, 0, 0, 0);
    textView.setLineSpacing(0, 0);
    textView.setIncludeFontPadding(false);
//    textView.setFirstBaselineToTopHeight(0);
    textView.setTypeface(Typeface.DEFAULT);
    textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, sprite.size * screen.scale);
  }
}
