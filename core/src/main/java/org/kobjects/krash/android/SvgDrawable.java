package org.kobjects.krash.android;

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
  private int alpha;
  private ColorFilter colorFilter;

  public SvgDrawable(SVG svg) {
    this.svg = svg;
  }

  private PictureDrawable getWrapped() {
    if (wrapped == null) {
      onBoundsChange(getBounds());
    }
    return wrapped;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    getWrapped().draw(canvas);
  }

  @Override
  public void setAlpha(int i) {
    this.alpha = i;
    getWrapped().setAlpha(i);
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    RenderOptions renderOptions = new RenderOptions();
    renderOptions.viewPort(bounds.left, bounds.top, bounds.width(), bounds.height());
    wrapped = new PictureDrawable(svg.renderToPicture(renderOptions));
    wrapped.setBounds(bounds);
    wrapped.setColorFilter(colorFilter);
    wrapped.setAlpha(alpha);
  }

  public int getIntrinsicWidth() {
    return (int) svg.getDocumentWidth();
  }

  public int getIntrinsicHeight() {
    return (int) svg.getDocumentHeight();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    this.colorFilter = colorFilter;
    getWrapped().setColorFilter(colorFilter);
  }

  @Override
  public int getOpacity() {
    return getWrapped().getOpacity();
  }
}
