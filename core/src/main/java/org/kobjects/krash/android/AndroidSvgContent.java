package org.kobjects.krash.android;

import android.graphics.drawable.Drawable;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.kobjects.krash.api.Svg;


public class AndroidSvgContent extends AndroidDrawableContent implements Svg {

  private final SVG svg;

  AndroidSvgContent(AndroidScreen screen, String svg) {
    super(screen);
    try {
      this.svg = SVG.getFromString(svg);
    } catch (SVGParseException e) {
      throw new IllegalArgumentException(e);
    }
  }

  @Override
  Drawable getDrawable() {
    return new SvgDrawable(svg);
  }

  @Override
  public String getSvg() {
    return svg.toString();
  }
}
