package org.kobjects.krash.android;

import android.view.View;
import android.widget.FrameLayout;


/**
 * Derives the size from the wrapped view. All views are positioned at the top left corner,
 * similar to FrameLayout.
 */
class AnchorLayout<T extends View> extends FrameLayout {

  T wrapped;

  public AnchorLayout(T wrapped) {
    super(wrapped.getContext());
    setClipChildren(false);
    setWrapped(wrapped);
  }

  void setWrapped(T wrapped) {
    if (this.wrapped != null) {
      removeView(this.wrapped);
    }
    this.wrapped = wrapped;
    if (wrapped != null) {
      addView(wrapped);
    }
  }


  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
   super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    setMeasuredDimension(wrapped.getMeasuredWidth(), wrapped.getMeasuredHeight());
  }
}
