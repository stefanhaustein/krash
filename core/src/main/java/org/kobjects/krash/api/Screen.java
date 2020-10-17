package org.kobjects.krash.api;

public interface Screen extends Anchor {

  Sprite createSprite();

  EmojiContent createEmoji(String codepoint);

  Object getLock();

  Iterable<Sprite> allSprites();

  Content createTextContent(String text);
}