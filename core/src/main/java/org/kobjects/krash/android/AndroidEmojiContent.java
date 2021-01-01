package org.kobjects.krash.android;

import android.graphics.drawable.Drawable;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.kobjects.krash.api.Emoji;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class AndroidEmojiContent extends AndroidDrawableContent implements Emoji {

  // TODO: add a static SVG that can be used to mark loading errors.
  static Map<String, SVG> svgCache = new HashMap<>();




  static SVG ERROR_SVG;
  static {
    try {
      ERROR_SVG = SVG.getFromString("" +
          "<svg version=\"1.1\" baseProfile=\"full\" width=\"200\" height=\"200\" xmlns=\"http://www.w3.org/2000/svg\">" +
          "<circle cx=\"100\" cy=\"100\" r=\"80\" fill=\"red\" />" +
          "</svg>");
    } catch (SVGParseException e) {
      throw new RuntimeException();
    }
  }

  final String codepoint;


  AndroidEmojiContent(AndroidScreen screen, String codepoint) {
    super(screen);
    this.codepoint = codepoint;
  }


  @Override
  public Drawable getDrawable() {
    synchronized (svgCache) {
      SVG svg = svgCache.get(codepoint);
      if (svg == null) {
        String imageName = "twmoji/" + Long.toHexString(Character.codePointAt(codepoint, 0)) + ".svg";
        try {
          svg = SVG.getFromAsset(screen.activity.getAssets(), imageName);
        } catch (Exception e) {
          e.printStackTrace();
          return new SvgDrawable(ERROR_SVG);
        }
        svgCache.put(codepoint, svg);
      }
      return new SvgDrawable(svg);
    }
  }


  @Override
  public String getCodepoint() {
    return codepoint;
  }

  @Override
  public float getIntrinsicWidth() {
    return 64;
  }

  @Override
  public float getIntrinsicHeight() {
    return 64;
  }
}
