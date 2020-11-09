package org.kobjects.krash.android;

import android.graphics.drawable.Drawable;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import org.kobjects.krash.api.Emoji;

import java.io.InputStream;
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
    super(screen);    this.codepoint = codepoint;
    synchronized (svgCache) {
      if (svgCache.get(codepoint) == null) {
        requestSvg(codepoint);
      }
    }
  }


  @Override
  public Drawable getDrawable() {
    synchronized (svgCache) {
      SVG svg = svgCache.get(codepoint);
      if (svg != null && svg != ERROR_SVG) {
        return new SvgDrawable(svg);
      }
      Drawable drawable = Emojis.getDrawable(screen.getView().getContext(), codepoint);
      return drawable != null ? drawable : Emojis.getDrawable(screen.getView().getContext(), "\uD83D\uDEAB");
    }
  }

  void requestSvg(String name) {
    int codePoint = Character.codePointAt(name, 0);

    new Thread(() -> {
      try {
        URL url = new URL("https://twemoji.maxcdn.com/v/latest/svg/" + Integer.toHexString(codePoint) + ".svg");
        InputStream is = url.openConnection().getInputStream();
        SVG svg = SVG.getFromInputStream(is);
        is.close();

        synchronized (svgCache) {
          svgCache.put(name, svg);
        }
        // TODO
        // imageDirty = true;
        // requestSync(true);
      } catch (Exception e) {
        e.printStackTrace();
        synchronized (svgCache) {
          svgCache.put(name, ERROR_SVG);
        }
      }

    }).start();


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
