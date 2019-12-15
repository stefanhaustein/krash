package org.kobjects.graphics;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ScaleDrawable;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.appcompat.widget.AppCompatImageView;

import com.caverock.androidsvg.SVG;
import com.caverock.androidsvg.SVGParseException;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Sprite extends PositionedViewHolder<ImageView>  {

  public static final String DEFAULT_FACE = "\ud83d\ude03";

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

  // TODO: add a static SVG that can be used to mark loading errors.
  static Map<String, SVG> svgCache = new HashMap<>();

  private static Canvas testCanvas;
  private static Bitmap testBitmap;

  static float clockwiseDegToDx(float deg) {
    return (float) Math.cos(Math.toRadians(90 - deg));
  }

  static float clockwiseDegToDy(float deg) {
    return (float) Math.sin(Math.toRadians(90 - deg));
  }

  static float dxDyToClockwiseDeg(float dx, float dy) {
    return  90 - (float) Math.toDegrees(Math.atan2(dy, dx));
  }

  TextBox label;
  TextBox bubble;
  private float size;
  private String face;
  private float angle;
  private float speed;
  private float direction;
  private float grow;
  private float fade;
  private float rotation;
  private Bitmap bitmap;

  private EdgeMode edgeMode = EdgeMode.NONE;

  private boolean imageDirty = true;
  private boolean sizeDirty = true;

  private float[] distances = new float[64];


  public Sprite(Screen screen) {
    super(screen, new AppCompatImageView(screen.activity));

   view.wrapped.setAdjustViewBounds(true);
   view.wrapped.setScaleType(ImageView.ScaleType.FIT_XY);

    setFace(DEFAULT_FACE);
  }

  public String getFace() {
    return face;
  }

  public float getSize() {
    return size;
  }

  public float getAngle() {
    return angle;
  }

  @Override
  boolean shouldBeAttached() {
    // Top level sprites without children will get checked for physical removal
    if (view.getChildCount() == 1 && anchor instanceof Screen) {
      // width / height swap is intended here: ranges go up to the double of the opposite dimension
      return opacity > MIN_OPACITY
          && x - size / 2 < screen.getLogicalViewportHeight() && x + size / 2 > -screen.getLogicalViewportHeight()
          && y - size / 2 < screen.getLogicalViewportWidth() && y + size / 2 > -screen.getLogicalViewportWidth();
    }
    return super.shouldBeAttached();
  }


  @Override
  void requestSync(boolean hard) {
    super.requestSync(hard);
    if (hard) {
      sizeDirty = true;
    }
  }

  void fillDistanceArray(Drawable drawable) {
    if (testCanvas == null) {
      testBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
      testCanvas = new Canvas(testBitmap);
    }

    Paint clearPaint = new Paint();
    clearPaint.setColor(0);
    clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC));

    testCanvas.drawRect(0, 0, 64, 64, clearPaint);

    ScaleDrawable scaleDrawable = new ScaleDrawable(drawable, Gravity.CENTER, 64, 64);
    scaleDrawable.setLevel(10000);
    scaleDrawable.setBounds(0, 0, 64, 64);
    scaleDrawable.draw(testCanvas);

    for (int i = 0; i < 64; i++) {
      distances[i] = 0;
      float deg = i * 360 / 64;
      float dx = clockwiseDegToDx(deg);
      float dy = clockwiseDegToDy(deg);
      for (int distance = (int) Math.sqrt(2 * 32 * 32); distance > 0; distance -= 2) {
        int x = 32 + (int) (dx * distance);
        int y = 32 + (int) (dy * distance);
        if (x >= 0 && y >= 0 && x < 64 && y < 64) {
          if (testBitmap.getPixel(x, y) != 0) {
            distances[i] = distance/32f;
            break;
          } else {
            testBitmap.setPixel(x, y, i < 8 ? 0xff00ff00 : i < 16 ? 0xff0000ff : 0xffff0000);
          }
        }
      }
    }

  }


  @Override
  public void syncUi() {
    if (imageDirty) {
      imageDirty = false;
      Drawable drawable;
      if (bitmap != null) {
        BitmapDrawable bitmapDrawable = new BitmapDrawable(view.getResources(), bitmap);
        bitmapDrawable.setFilterBitmap(false);
       // bitmapDrawable.setAntiAlias(false);
        drawable = bitmapDrawable;
        view.wrapped.setLayerType(View.LAYER_TYPE_HARDWARE, null);
      } else {
        synchronized (svgCache) {
          SVG svg = svgCache.get(face);
          if (svg != null && svg != ERROR_SVG) {
            view.wrapped.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            drawable = new SvgDrawable(svg);
          } else {
            view.wrapped.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            drawable = Emojis.getDrawable(view.getContext(), face);
          }
        }
      }

      fillDistanceArray(drawable);
      view.wrapped.setImageDrawable(drawable);

    }

    if (sizeDirty) {
      sizeDirty = false;

      int pixelSize = Math.round(screen.scale * size);

   /*   if (face != null && !face.isEmpty()) {
        synchronized (svgCache) {
          SVG svg = svgCache.get(face);
          if (svg != null) {
            view.wrapped.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            view.wrapped.setImageDrawable(new PictureDrawable(svg.renderToPicture(pixelSize, pixelSize)));
          }
        }
      }*/

          // view.wrapped.setBackgroundColor((int) (Math.random() * 0xffffff) | 0xff000000);
      view.wrapped.setLayoutParams(new FrameLayout.LayoutParams(pixelSize, pixelSize));
      view.wrapped.requestLayout();
      view.requestLayout();


    }
    view.wrapped.setRotation(angle);
  }


  public boolean setSize(float size) {
    if (size == this.size) {
      return false;
    }
    this.size = size;
    sizeDirty = true;
    requestSync(true);
    return true;
  }

  public TextBox getLabel() {
    if (label == null) {
      label = new TextBox(screen);
      label.setAnchor(this);
      label.setTextColor(0xff000000);
      label.setFillColor(0xffffffff);
      label.setLineColor(0xff000000);
      label.setY((getHeight() + this.label.getHeight()) / -2);
      label.setYAlign(YAlign.TOP);
    }
    return label;
  }

  public boolean setLabel(TextBox bubble) {
    if (bubble == this.label) {
      return false;
    }
    this.label = bubble;
    label.anchor = this;
    return true;
  }

  public TextBox getBubble() {
    if (bubble == null) {
      bubble = new TextBox(screen);
      bubble.setAnchor(this);
      bubble.setPadding(3);
      bubble.setTextColor(0xff000000);
      bubble.setFillColor(0xffffffff);
      bubble.setLineColor(0xff000000);
      bubble.setY(10);
      bubble.setYAlign(YAlign.BOTTOM);
      bubble.setCornerRadius(3);
    }
    return bubble;
  }

  public void setBitmap(Bitmap bitmap) {
    this.bitmap = bitmap;
    this.face = "";
    this.imageDirty = true;
    requestSync(false);
  }

  public boolean setBubble(TextBox bubble) {
    if (bubble == this.bubble) {
      return false;
    }
    this.bubble = bubble;
    bubble.anchor = this;
    return true;
  }

  public boolean setEdgeMode(EdgeMode newValue) {
    if (edgeMode == newValue) {
      return false;
    }
    edgeMode = newValue;
    return true;
  }

  public boolean setFace(String face) {
    if (Objects.equals(face, this.face)) {
      return false;
    }

    synchronized (svgCache) {
      if (svgCache.get(face) == null) {
        requestSvg(face);
      }
    }
    this.face = face;
    imageDirty = true;
    requestSync(true);
    return true;
  }

  public boolean setAngle(float angle) {
    if (angle == this.angle) {
      return false;
    }
    this.angle = angle;
    requestSync(false);
    return true;
  }


  @Override
  public float getWidth() {
    return size;
  }

  @Override
  public float getHeight() {
    return size;
  }

  public void say(String text) {
    getBubble().setText(text);
    getBubble().setVisible(!text.isEmpty());
  }

  public void animate(float dt) {
    boolean propertiesChanged = false;

    if (speed != 0.0) {
      propertiesChanged = true;
      double theta = Math.toRadians(90 - direction);
      double delta = dt * speed / 1000;
      double dx = Math.cos(theta) * delta;
      double dy = Math.sin(theta) * delta;
      x += dx;
      y += dy;

       if (edgeMode != EdgeMode.NONE && anchor == screen) {
        float radius = size / 2;
        switch (edgeMode) {
          case WRAP:
            if (dx > 0 && x - radius > screen.getWidth() / 2) {
              x = -screen.getWidth() / 2 - radius;
            } else if (dx < 0 && x + radius < screen.getWidth() / -2) {
              x = screen.getWidth() / 2 + radius;
            }
            if (dy > 0 && y - radius > screen.getHeight() / 2) {
              y = -screen.getHeight() / 2 - radius;
            } else if (dy < 0 && y + radius < screen.getHeight() / -2) {
              y = screen.getHeight() / 2 + radius;
            }
            break;
          case BOUNCE:
            if (dx > 0 && x + radius > screen.getWidth() / 2) {
              direction += dy < 0 ? 90 : -90;
            } else if (dx < 0 && x - radius < screen.getWidth() / -2) {
              direction += dy > 0 ? 90 : -90;
            }
            if (dy > 0 && y + radius > screen.getHeight() / 2) {
              direction += dx > 0 ? 90 : -90;
            } else if (dy < 0 && y - radius < screen.getHeight() / -2) {
              direction += dx < 0 ? 90 : -90;
            }
            break;
        }
      }
    }
    if (rotation != 0F) {
      propertiesChanged = true;
      angle += rotation * dt / 1000F;
    }
    if (grow != 0F) {
      propertiesChanged = true;
      size += grow * dt / 1000F;
    }
    if (fade != 0F) {
      propertiesChanged = true;
      opacity += fade * dt / 1000F;
    }

    if (propertiesChanged) {
      requestSync(false);
    }

    if (tag instanceof Animated) {
      ((Animated) tag).animate(dt, propertiesChanged);
    }
/*
    if (collisions().size() > 0) {
      view.wrapped.setBackgroundColor(0xffff0000);
    } else {
      view.wrapped.setBackgroundColor(0);
    } */
  }

  boolean checkCollision(Sprite other) {
    float sx = getScreenCX();
    float sy = getScreenCY();
    float distX = other.getScreenCX() - sx;
    float distY = other.getScreenCY() - sy;
    double minDist = (other.size + size) * 0.5;
    float centerDistanceSq = distX * distX + distY * distY;
    if (centerDistanceSq > minDist * minDist) {
      return false;
    }

    float direction = dxDyToClockwiseDeg(distX, distY) + angle;
    int directionIndex = ((int) (direction * 64 / 360)) & 63;
    float radius = size * distances[directionIndex] / 2;

    float otherDirection = dxDyToClockwiseDeg(-distX, -distY) + other.angle;
    int otherDirectionIndex = ((int) (otherDirection * 64 / 360)) & 63;
    float otherRadius = other.size * other.distances[otherDirectionIndex] / 2;

    minDist = radius + otherRadius;

    return centerDistanceSq < minDist * minDist;
  }



  /**
   * Checks all sprites, as allWidgets is flattened.
   */
  public Collection<Sprite> collisions() {
    if (!shouldBeAttached()) {
      return Collections.emptyList();
    }
    synchronized (screen.allWidgets) {
      ArrayList<Sprite> result = new ArrayList<>();
      // StringBuilder debug = new StringBuilder();
      for (PositionedViewHolder<?> widget : screen.allWidgets) {
        if (widget != this && widget instanceof Sprite && widget.shouldBeAttached()) {
          Sprite other = (Sprite) widget;
          if (checkCollision((Sprite) widget)) {
            result.add(other);
          }
        }
      }
      // say(debug.toString());
      return result;
    }
  }

  public float getSpeed() {
    return speed;
  }

  public boolean setSpeed(float speed) {
    if (speed != this.speed) {
      this.speed = speed;
      return true;
    }
    return false;
  }

  public float getDirection() {
    return direction;
  }

  public boolean setDirection(float direction) {
    if (this.direction != direction) {
      this.direction = direction;
      return true;
    }
    return false;
  }

  public EdgeMode getEdgeMode() {
    return edgeMode;
  }

  public float getGrow() {
    return grow;
  }

  public boolean setGrow(float grow) {
    if (this.grow != grow) {
      this.grow = grow;
      return true;
    }
    return false;
  }

  public float getFade() {
    return fade;
  }

  public boolean setFade(float fade) {
    if (this.fade != fade) {
      this.fade = fade;
      return true;
    }
    return false;
  }

  public float getRotation() {
    return rotation;
  }
  
  public boolean setRotation(float rotation) {
    if (this.rotation != rotation) {
      this.rotation = rotation;
      return true;
    }
    return false;
  }

  public float getDx() {
    return speed * clockwiseDegToDx(direction);
  }

  public float getDy() {
    return speed * clockwiseDegToDy(direction);
  }

  boolean setDxy(float dx, float dy) {
    float newSpeed = (float) Math.sqrt(dx * dx + dy * dy);

    if (newSpeed == 0) {
      return setSpeed(0);
    }


    return setSpeed(newSpeed) | setDirection(dxDyToClockwiseDeg(dx, dy));
  }

  public boolean setDx(float dx) {
    return setDxy(dx, getDy());
  }

  public boolean setDy(float dy) {
    return setDxy(getDx(), dy);
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
        imageDirty = true;
        requestSync(true);
      } catch (Exception e) {
        e.printStackTrace();
        synchronized (svgCache) {
          svgCache.put(name, ERROR_SVG);
        }
      }

    }).start();


  }

}
