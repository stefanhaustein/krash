package org.kobjects.krash.api;

import android.graphics.Matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public abstract class Sprite<T extends Content> implements Anchor {

  public enum SizeComponent {
    NONE,
    WIDTH,
    HEIGHT,
  }


  protected final Object lock = new Object();

  public static final double MIN_OPACITY = 0.0001;
  public static final String DEFAULT_FACE = "\ud83d\ude03";

  public static final int POSITION_CHANGED = 1;
  public static final int SIZE_CHANGED = 2;
  public static final int CONTENT_CHANGED = 4;
  public static final int HIERARCHY_CHANGED = 8;
  public static final int STYLE_CHANGED = 16;
  public static final int LISTENERS_CHANGED = 32;

  protected float x;
  protected float y;
  protected float z;
  protected float opacity = 1;

  private float anchorX;
  private float anchorY;
  private float pivotX = 0.5f;
  private float pivotY = 0.5f;

  // For internal use!
  protected boolean visible = true;

  protected final Screen screen;
  protected float[] distances = new float[64];

  protected Anchor anchor;
  Sprite bubbleSprite;
  private float width;
  private float height;
  protected EnumSet<SizeComponent> manualSizeComponents = EnumSet.noneOf(SizeComponent.class);
  protected ArrayList<Runnable> changeListeners;
  private float angle;
  private float speed;
  private float direction;
  private float grow;
  private float fade;
  private float rotation;
  private EdgeMode edgeMode = EdgeMode.NONE;
  protected int changedProperties;
  Object tag;

  protected ArrayList<DragListener> dragListeners;
  private ArrayList<Sprite<?>> children = new ArrayList<>();


  protected Sprite(Screen screen, T content) {
    this.screen = screen;
    setAnchor(screen);
    setContent(content);
  }

  public void addDragListener(DragListener dragListener) {
    synchronized (lock) {
      if (dragListeners == null) {
        dragListeners = new ArrayList<>();
      }
      dragListeners.add(dragListener);
      requestSync(LISTENERS_CHANGED);
    }
  }

  public void setPivotX(float pivotX) {
    this.pivotX = pivotX;
  }
  public void setPivotY(float pivotY) {
    this.pivotY = pivotY;
  }

  public float getPivotX() {
    return pivotX;
  }

  public float getPivotY() {
    return pivotY;
  }


  protected final boolean notifyDragged(DragListener.DragState state, float x, float y) {
    synchronized (lock) {
      if (dragListeners != null) {
        for (DragListener dragListener : dragListeners) {
          if (dragListener.drag(state, x, y)) {
            return true;
          }
        }
      }
    }
    return false;
  }


  public void setTag(Object tag) {
    this.tag = tag;
  }

  public Object getTag() {
    return tag;
  }




  abstract protected void requestSync(int newChangedProperties);


  abstract protected void syncNative(Matrix matrix);

  public static float clockwiseDegToDx(float deg) {
    return (float) Math.cos(Math.toRadians(90 - deg));
  }

  public static float clockwiseDegToDy(float deg) {
    return (float) Math.sin(Math.toRadians(90 - deg));
  }

  static float dxDyToClockwiseDeg(float dx, float dy) {
    return  90 - (float) Math.toDegrees(Math.atan2(dy, dx));
  }

  public String getFace() {
    return getContent() instanceof Emoji ? ((Emoji) getContent()).getCodepoint() : "";
  }

  public abstract T getContent();

  public float getAngle() {
    return angle;
  }

  protected boolean shouldBeAttached() {
    return opacity > MIN_OPACITY;
  }


  public Anchor getAnchor() {
    return anchor;
  }

  public Screen getScreen() {
    return screen;
  }

  public float getRelativeX() {
    if (anchor == screen) {
      return (screen.getWidth() - getWidth()) / 2 + x;
    } else {
      return (anchor.getWidth() - getWidth()) / 2 + x;
    }
  }

  public float getRelativeY() {
    if (anchor == screen) {
      return (screen.getHeight() - getHeight()) / 2 - y;
    } else {
      return (anchor.getHeight() - getHeight()) / 2 - y;
    }
  }

  public float getScreenCX() {
    float result = getRelativeX() + getWidth() / 2;
    Anchor current = anchor;
    while (current instanceof Sprite) {
      result += ((Sprite) current).getRelativeX();
      current = ((Sprite) current).anchor;
    }
    return result;
  }

  public float getScreenCY() {
    float result = getRelativeY() + getHeight() / 2;
    Anchor current = anchor;
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

  public void setAnchor(Anchor anchor) {
    anchor(anchor,0.5f, 0.5f);
  }

  public void anchor(Anchor anchor, float anchorX, float anchorY) {
    this.anchorX = anchorX;
    this.anchorY = anchorY;
    if (this.anchor != anchor) {
      if (this.anchor != null) {
        this.anchor.removeChild(this);
      }
      this.anchor = anchor;
      anchor.addChild(this);
      requestSync(HIERARCHY_CHANGED);
    }
  }

  public void setZ(float z) {
    if (z != this.z) {
      this.z = z;
      requestSync(POSITION_CHANGED);
    }
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

  public void addChangeListener(Runnable changeListener) {
    synchronized (screen.getLock()) {
      if (changeListeners == null) {
        changeListeners = new ArrayList<>();
      }
    }
    synchronized (changeListeners) {
      changeListeners.add(changeListener);
    }
  }


  public boolean setContent(T content) {
    if (content instanceof Emoji && getContent() instanceof Emoji && !manualSizeComponents.isEmpty()) {
      float size = getHeight();
      boolean result = setContentImpl(content);
      setHeight(size);
      return result;
    }
    return setContentImpl(content);
  }

  protected abstract boolean setContentImpl(Content content);

  protected abstract void adjustSize(SizeComponent sizeComponent);

  public void setWidth(float width) {
    this.width = width;
    adjustSize(SizeComponent.WIDTH);
  }

  public void setHeight(float height) {
    this.height = height;
    adjustSize(SizeComponent.HEIGHT);
  }


  public Sprite getBubble() {
    if (bubbleSprite == null) {
      Text bubbleText = screen.createText("");
      bubbleText.setColor(0xff000000);

      Bubble bubble = screen.createBubble();
      bubble.setPadding(3);
      bubble.setFillColor(0xffffffff);
      bubble.setLineColor(0xff000000);
      bubble.setCornerRadius(3);
      bubble.setContent(bubbleText);

      bubbleSprite = screen.createSprite(bubble);
      bubbleSprite.anchor(this, 0.5f, 0);
      bubbleSprite.setY(-10 - bubbleSprite.height);
    }
    return bubbleSprite;
  }


  public boolean setEdgeMode(EdgeMode newValue) {
    if (edgeMode == newValue) {
      return false;
    }
    edgeMode = newValue;
    return true;
  }

  @Deprecated
  public boolean setFace(String face) {
    return setContent((T) screen.createEmoji(face));
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
    Sprite bubbleSprite = getBubble();
    Bubble bubble = (Bubble) bubbleSprite.getContent();
    bubble.setContent(screen.createText(text));
    bubble.setDY(5);
    bubbleSprite.setVisible(!text.isEmpty());

    bubbleSprite.adjustSize(SizeComponent.NONE);
    // getBubble().requestSync(HIERARCHY_CHANGED);
  }

  public void sync(float dt, Matrix parentTransformation) {
    synchronized (lock) {

      int propertiesChanged = 0;

      Matrix transformation = new Matrix(parentTransformation);

      if (speed != 0.0) {
        propertiesChanged = POSITION_CHANGED;
        double theta = Math.toRadians(90 - direction);
        double delta = dt * speed / 1000;
        double dx = Math.cos(theta) * delta;
        double dy = Math.sin(theta) * delta;
        x += dx;
        y += dy;

        if (edgeMode != EdgeMode.NONE && anchor == screen) {
          float radius = (getWidth() + getHeight()) / 2 / 2;
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
        setWidth(getWidth() + grow * dt / 1000F);
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

      float tx = x + anchor.getWidth() * anchorX - width * pivotX;
      float ty = y + anchor.getHeight() * anchorY - height * pivotY;


      transformation.preTranslate(tx, ty);

      syncNative(transformation);

      for (Sprite<?> child : children) {
        child.sync(dt, transformation);
      }
    }
  }

  boolean checkCollision(Sprite other) {
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
    float radius = Math.max(getWidth(), getHeight()) * distances[directionIndex] / 2;

    float otherDirection = Sprite.dxDyToClockwiseDeg(-distX, -distY) + other.getAngle();
    int otherDirectionIndex = ((int) (otherDirection * 64 / 360)) & 63;
    float otherRadius = Math.max(other.getWidth(), other.getHeight()) * other.distances[otherDirectionIndex] / 2;

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
    synchronized (screen.getLock()) {
      ArrayList<Sprite> result = new ArrayList<>();
      // StringBuilder debug = new StringBuilder();
      for (Sprite other : screen.allSprites()) {
        if (other.shouldBeAttached()) {
          if (checkCollision(other)) {
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


  /**
   * Adjusts the size without triggering anything
   */
  public void setAdjustedSize(float[] size) {
    this.width = size[0];
    this.height = size[1];
  }

  public Set<SizeComponent> getManualSizeComponents() {
    return manualSizeComponents;
  }


  @Override
  public void addChild(Sprite<? extends Content> child) {
    synchronized (lock) {
      children.add(child);
    }
  }

  @Override
  public void removeChild(Sprite<? extends Content> child) {
    synchronized (lock) {
      children.remove(child);
    }
  }
}
