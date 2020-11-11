package org.kobjects.krash.api;

public interface Screen extends Anchor {

  <T extends Content> Sprite<T> createSprite(T content);

  Emoji createEmoji(String codepoint);

  Object getLock();

  Iterable<Sprite> allSprites();

  Text createText(String text);

  Tiles createGrid(int width, int height);

  Svg createSvg(String svg);

  Bubble createBubble();
}
