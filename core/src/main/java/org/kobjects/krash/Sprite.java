package org.kobjects.krash;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.kobjects.krash.api.Content;
import org.kobjects.krash.api.EmojiContent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;

public abstract class Sprite<T extends View> extends ViewHolder<AnchorLayout<T>> {
  protected final Object lock = new Object();

  public static final double MIN_OPACITY = 0.0001;
  public static final String DEFAULT_FACE = "\ud83d\ude03";

  public static final int POSITION_CHANGED = 1;
  public static final int SIZE_CHANGED = 2;
  public static final int CONTENT_CHANGED = 4;
  public static final int HIERARCHY_CHANGED = 8;
  public static final int STYLE_CHANGED = 16;

  protected float x;
  protected float y;
  protected float z;
  protected XAlign xAlign = XAlign.CENTER;
  protected YAlign yAlign = YAlign.CENTER;
  protected float opacity = 1;

  // For internal use!
  protected boolean visible = true;

  final Screen screen;
  protected float[] distances = new float[64];
  boolean syncRequested;

  ViewHolder<?> anchor;
  AndroidSprite label;
  AndroidSprite bubble;
  float size;
  float width;
  float height;
  int textColor = Color.BLACK;
  EnumSet<SizeComponent> manualSizeComponents = EnumSet.noneOf(SizeComponent.class);
  private ArrayList<Runnable> changeListeners;
  private float angle;
  private float speed;
  private float direction;
  private float grow;
  private float fade;
  private float rotation;
  private int fillColor;
  private int lineColor;
  private float lineWidth;
  private float cornerRadius;
  private float padding;
  private EdgeMode edgeMode = EdgeMode.NONE;
  private int changedProperties;


  Sprite(Screen screen, T view) {
    super(new AnchorLayout<>(view));
    synchronized (screen.lock) {
      screen.allWidgets.add(this);
    }
    this.screen = screen;
    this.anchor = screen;
    view.setTag(this);
  }



  public abstract void syncUi(int changedProperties);


  void requestSync(int newChangedProperties) {
    synchronized (lock) {
      this.changedProperties |= newChangedProperties;
      if (!syncRequested) {
        syncRequested = true;
        screen.activity.runOnUiThread(() -> {
          synchronized (lock) {
            syncRequested = false;
            int changedProperties = Sprite.this.changedProperties;
            Sprite.this.changedProperties = 0;
            view.setVisibility(visible ? View.VISIBLE : View.GONE);
            view.wrapped.setAlpha(opacity);
            // visible is used internally to handle bubble visibility and to remove everything on clear, so it
            // gets special treatment here.
            boolean shouldBeAttached = visible && shouldBeAttached();
            ViewGroup expectedParent = shouldBeAttached ? anchor.view : null;
            if (view.getParent() != expectedParent) {
              if (view.getParent() != null) {
                ((ViewGroup) view.getParent()).removeView(view);
              }
              if (expectedParent == null) {
                return;
              }
              expectedParent.addView(view, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
            }
            syncUi(changedProperties);

            view.setTranslationX(getRelativeX() * screen.scale);
            view.setTranslationY(getRelativeY() * screen.scale);

            view.setTranslationZ(z);

            if (changeListeners != null) {
              synchronized (changeListeners) {
                for (Runnable changeListener : changeListeners) {
                  changeListener.run();
                }
              }
            }
            this.changedProperties = 0;
          }
        });
      }
    }
  }


  static float clockwiseDegToDx(float deg) {
    return (float) Math.cos(Math.toRadians(90 - deg));
  }

  static float clockwiseDegToDy(float deg) {
    return (float) Math.sin(Math.toRadians(90 - deg));
  }

  static float dxDyToClockwiseDeg(float dx, float dy) {
    return  90 - (float) Math.toDegrees(Math.atan2(dy, dx));
  }

  public String getFace() {
    return getContent() instanceof EmojiContent ? ((EmojiContent) getContent()).getCodepoint() : "";
  }

  protected abstract Content getContent();

  public float getSize() {
    return (width + height) / 2;
  }

  public float getAngle() {
    return angle;
  }

  boolean shouldBeAttached() {
    return opacity > MIN_OPACITY;
  }


  public ViewHolder<?> getAnchor() {
    return anchor;
  }

  public Screen getScreen() {
    return screen;
  }

  public T getWrapped() {
    return view.wrapped;
  }

  public float getRelativeX() {
    if (anchor == screen) {
      switch (xAlign) {
        case LEFT:
          return x;
        case RIGHT:
          return screen.getWidth() - x - getWidth();
        default:
          return (screen.getWidth() - getWidth()) / 2 + x;
      }
    } else {
      switch (xAlign) {
        case LEFT:
          return anchor.getWidth() + x;
        case RIGHT:
          return -x - getWidth();
        default:
          return (anchor.getWidth() - getWidth()) / 2 + x;
      }
    }
  }

  public float getRelativeY() {
    if (anchor == screen) {
      switch (yAlign) {
        case TOP:
          return y;
        case BOTTOM:
          return screen.getHeight() - getHeight() - y;
        default:
          return (screen.getHeight() - getHeight()) / 2 - y;
      }
    } else {
      switch (yAlign) {
        case TOP:
          return anchor.getHeight() + y;
        case BOTTOM:
          return -y - getHeight();
        default:
          return (anchor.getHeight() - getHeight()) / 2 - y;
      }
    }
  }

  public float getScreenCX() {
    float result = getRelativeX() + getWidth() / 2;
    ViewHolder current = anchor;
    while (current instanceof Sprite) {
      result += ((Sprite) current).getRelativeX();
      current = ((Sprite) current).anchor;
    }
    return result;
  }

  public float getScreenCY() {
    float result = getRelativeY() + getHeight() / 2;
    ViewHolder current = anchor;
    while (current instanceof Sprite) {
      result += ((Sprite) current).getRelativeY();
      current = ((Sprite) current).anchor;
    }
    return result;
  }

  public float getX() {
    return x;
  }

  public float getY() {
    return y;
  }

  public float getZ() {
    return z;
  }

  public float getOpacity() {
    return opacity;
  }

  public boolean setX(float x) {
    if (x == this.x) {
      return false;
    }
    this.x = x;
    requestSync(POSITION_CHANGED);
    return true;
  }

  public boolean setY(float y) {
    if (y == this.y) {
      return false;
    }
    this.y = y;
    requestSync(POSITION_CHANGED);
    return true;
  }


  public boolean setOpacity(float opacity) {
    opacity = Math.max(0, Math.min(opacity, 1));
    if (opacity == this.opacity) {
      return false;
    }
    this.opacity = opacity;
    requestSync(CONTENT_CHANGED);
    return true;
  }

  public boolean setAnchor(ViewHolder<?> anchor) {
    if (this.anchor == anchor) {
      return false;
    }
    this.anchor = anchor;
    requestSync(HIERARCHY_CHANGED);
    return true;
  }

  public boolean setZ(float z) {
    if (z == this.z) {
      return false;
    }
    this.z = z;
    requestSync(POSITION_CHANGED);

    return true;
  }

  public boolean getVisible() {
    return visible;
  }

  public boolean setVisible(boolean value) {
    if (value == visible) {
      return false;
    }
    visible = value;
    requestSync(/*false*/ CONTENT_CHANGED);
    return true;
  }

  public XAlign getXAlign() {
    return xAlign;
  }

  public YAlign getYAlign() {
    return yAlign;
  }

  public boolean setXAlign(XAlign newValue) {
    if (xAlign == newValue) {
      return false;
    }
    xAlign = newValue;
    requestSync(POSITION_CHANGED);
    return true;
  }


  public boolean setYAlign(YAlign newValue) {
    if (yAlign == newValue) {
      return false;
    }
    yAlign = newValue;
    requestSync(POSITION_CHANGED);
    return true;
  }


  public void addChangeListener(Runnable changeListener) {
    synchronized (screen.lock) {
      if (changeListeners == null) {
        changeListeners = new ArrayList<>();
      }
    }
    synchronized (changeListeners) {
      changeListeners.add(changeListener);
    }
  }

  public void setText(String text) {
    setContent(new AndrodTextContent(screen, text));
  }

  public abstract boolean setContent(Content content);

  public void setSize(float size) {
    this.size = size;
    adjustSize(SizeComponent.SIZE);
  }

  protected abstract void adjustSize(SizeComponent sizeComponent);

  public void setWidth(float width) {
    this.width = width;
    adjustSize(SizeComponent.WIDTH);
  }

  public void setHeight(float height) {
    this.height = height;
    adjustSize(SizeComponent.HEIGHT);
  }

  public AndroidSprite getLabel() {
    if (label == null) {
      label = new AndroidSprite(screen);
      label.setAnchor(this);
      label.setTextColor(0xff000000);
      label.setFillColor(0xffffffff);
      label.setLineColor(0xff000000);
      label.setY((getHeight() + this.label.getHeight()) / -2);
      label.setYAlign(YAlign.TOP);
    }
    return label;
  }

  public boolean setLabel(AndroidSprite label) {
    if (label == this.label) {
      return false;
    }
    this.label = label;
    label.anchor = this;
    return true;
  }

  public AndroidSprite getBubble() {
    if (bubble == null) {
      bubble = new AndroidSprite(screen);
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
    setContent(new AndroidBitmapContent(screen, bitmap));
  }

  public boolean setBubble(AndroidSprite bubble) {
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
    return setContent(new AndroidEmojiContent(screen, face));
  }


  public boolean setFillColor(int fillColor) {
    if (fillColor == this.fillColor) {
      return false;
    }
    this.fillColor = fillColor;
    requestSync(STYLE_CHANGED);
    return true;
  }

  public boolean setLineColor(int lineColor) {
    if (lineColor == this.lineColor) {
      return false;
    }
    this.lineColor = lineColor;
    requestSync(STYLE_CHANGED);
    return true;
  }

  public boolean setTextColor(int textColor) {
    if (textColor == this.textColor) {
      return false;
    }
    this.textColor = textColor;
    requestSync(STYLE_CHANGED);
    return true;
  }

  public boolean setLineWidth(float lineWidth) {
    if (lineWidth == this.lineWidth) {
      return false;
    }
    this.lineWidth = lineWidth;
    requestSync(STYLE_CHANGED);
    return true;
  }

  public boolean setCornerRadius(float cornerRadius) {
    if (cornerRadius == this.cornerRadius) {
      return false;
    }
    this.cornerRadius = cornerRadius;
    requestSync(STYLE_CHANGED);
    return true;
  }

  public boolean setPadding(float padding) {
    if (padding == this.padding) {
      return false;
    }
    this.padding = padding;
    requestSync(STYLE_CHANGED);
    return true;
  }

  public boolean setAngle(float angle) {
    if (angle == this.angle) {
      return false;
    }
    this.angle = angle;
    requestSync(POSITION_CHANGED);
    return true;
  }

  @Override
  public float getWidth() {
    return width;
  }

  @Override
  public float getHeight() {
    return height;
  }

  public void say(String text) {
    getBubble().setText(text);
    getBubble().setVisible(!text.isEmpty());
    getBubble().requestSync(HIERARCHY_CHANGED);
  }

  public void animate(float dt) {
    int propertiesChanged = 0;

    if (speed != 0.0) {
      propertiesChanged = POSITION_CHANGED;
      double theta = Math.toRadians(90 - direction);
      double delta = dt * speed / 1000;
      double dx = Math.cos(theta) * delta;
      double dy = Math.sin(theta) * delta;
      x += dx;
      y += dy;

       if (edgeMode != EdgeMode.NONE && anchor == screen) {
        float radius = getSize() / 2;
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
      propertiesChanged = POSITION_CHANGED;
      angle += rotation * dt / 1000F;
    }
    if (grow != 0F) {
      propertiesChanged = SIZE_CHANGED;
      setSize(getSize() + grow * dt / 1000F);
    }
    if (fade != 0F) {
      propertiesChanged = STYLE_CHANGED;
      opacity += fade * dt / 1000F;
    }

    if (propertiesChanged != 0) {
      requestSync(propertiesChanged);
    }

    if (tag instanceof Animated) {
      ((Animated) tag).animate(dt, propertiesChanged != 0);
    }
/*
    if (collisions().size() > 0) {
      view.wrapped.setBackgroundColor(0xffff0000);
    } else {
      view.wrapped.setBackgroundColor(0);
    } */
  }

  boolean checkCollision(AndroidSprite other) {
    float sx = getScreenCX();
    float sy = getScreenCY();
    float distX = other.getScreenCX() - sx;
    float distY = other.getScreenCY() - sy;
    float size = Math.max(width, height);
    float otherSize = Math.max(other.width, other.height);
    double minDist = (otherSize + size) * 0.5;
    float centerDistanceSq = distX * distX + distY * distY;
    if (centerDistanceSq > minDist * minDist) {
      return false;
    }

    float direction = Sprite.dxDyToClockwiseDeg(distX, distY) + angle;
    int directionIndex = ((int) (direction * 64 / 360)) & 63;
    float radius = getSize() * distances[directionIndex] / 2;

    float otherDirection = Sprite.dxDyToClockwiseDeg(-distX, -distY) + other.getAngle();
    int otherDirectionIndex = ((int) (otherDirection * 64 / 360)) & 63;
    float otherRadius = other.getSize() * other.distances[otherDirectionIndex] / 2;

    minDist = radius + otherRadius;

    return centerDistanceSq < minDist * minDist;
  }

  /**
   * Checks all sprites, as allWidgets is flattened.
   */
  public Collection<AndroidSprite> collisions() {
    if (!shouldBeAttached()) {
      return Collections.emptyList();
    }
    synchronized (screen.lock) {
      ArrayList<AndroidSprite> result = new ArrayList<>();
      // StringBuilder debug = new StringBuilder();
      for (Sprite<?> widget : screen.allWidgets) {
        if (widget != this && widget instanceof AndroidSprite && widget.shouldBeAttached()) {
          AndroidSprite other = (AndroidSprite) widget;
          if (checkCollision((AndroidSprite) widget)) {
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
    return speed * Sprite.clockwiseDegToDx(direction);
  }

  public float getDy() {
    return speed * Sprite.clockwiseDegToDy(direction);
  }

  boolean setDxy(float dx, float dy) {
    float newSpeed = (float) Math.sqrt(dx * dx + dy * dy);

    if (newSpeed == 0) {
      return setSpeed(0);
    }

    return setSpeed(newSpeed) | setDirection(Sprite.dxDyToClockwiseDeg(dx, dy));
  }

  public boolean setDx(float dx) {
    return setDxy(dx, getDy());
  }

  public boolean setDy(float dy) {
    return setDxy(getDx(), dy);
  }

  public int getLineColor() {
    return lineColor;
  }

  public float getLineWidth() {
    return lineWidth;
  }

  public int getFillColor() {
    return fillColor;
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  public int getTextColor() {
    return textColor;
  }

  public float getPadding() {
    return padding;
  }

  enum SizeComponent {
    NONE,
    WIDTH,
    HEIGHT,
    SIZE
  }
}
