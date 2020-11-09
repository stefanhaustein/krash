package org.kobjects.krash.api;

public interface Screen extends Anchor {

  Sprite createSprite();

  Emoji createEmoji(String codepoint);

  Object getLock();

  Iterable<Sprite> allSprites();

  Text createText(String text);

  Tiles createGrid(int width, int height);

  Svg createSvg(String svg);

  Bubble createBubble();
}
