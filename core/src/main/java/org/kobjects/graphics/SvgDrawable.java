package org.kobjects.graphics;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.PictureDrawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.caverock.androidsvg.RenderOptions;
import com.caverock.androidsvg.SVG;


/**
 * Workaround for PictureDrawable scaling issues.
 */
public class SvgDrawable extends Drawable{

  private final SVG svg;
  private PictureDrawable wrapped;

  public SvgDrawable(SVG svg) {
    this.svg = svg;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (wrapped == null) {
      onBoundsChange(getBounds());
    }
    wrapped.draw(canvas);
  }

  @Override
  public void setAlpha(int i) {
    wrapped.setAlpha(i);
  }

  protected void onBoundsChange(Rect bounds) {
    RenderOptions renderOptions = new RenderOptions();
    renderOptions.viewPort(bounds.left, bounds.top, bounds.width(), bounds.height());
    wrapped = new PictureDrawable(svg.renderToPicture(renderOptions));
    wrapped.setBounds(bounds);
  }

  public int getIntrinsicWidth() {
    return (int) svg.getDocumentWidth();
  }

  public int getIntrinsicHeight() {
    return (int) svg.getDocumentHeight();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    wrapped.setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return wrapped.getOpacity();
  }
}
