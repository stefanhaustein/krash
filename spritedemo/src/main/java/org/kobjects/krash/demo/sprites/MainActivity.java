package org.kobjects.krash.demo.sprites;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.krash.api.EdgeMode;
import org.kobjects.krash.android.Pen;
import org.kobjects.krash.android.AndroidScreen;
import org.kobjects.krash.android.AndroidSprite;
import org.kobjects.krash.api.GridContent;
import org.kobjects.krash.api.Sprite;

public class MainActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EmojiManager.install(new TwitterEmojiProvider());

    AndroidScreen screen = new AndroidScreen(this);
  //  screen.setViewport(320, 200, true);

    screen.getView().setBackgroundColor(Color.DKGRAY);
    setContentView(screen.getView());

    // Size comparison
    Pen pen = screen.createPen();
    pen.drawRect(-15, -15, 30, 30);

    GridContent grid = screen.createGrid(8, 8);
    for (int y = 0; y < 8; y++) {
      for (int x = 0; x < 8; x++) {
        grid.set(x, y, screen.createEmoji(((x+y) & 1) == 0 ? "⬛" : "⬜"));
      }
    }

    Sprite gridHolder = screen.createSprite();
    gridHolder.setContent(grid);
    gridHolder.setSize(200);
    gridHolder.setRotation(1);

    Sprite ball = screen.createSprite();
    ball.setFace("⚽");
    ball.setSize(20);
    ball.setSpeed(20);
    ball.setDirection(20);
    ball.setEdgeMode(EdgeMode.BOUNCE);
    ball.setRotation(-90);
    ball.say("Bounce");

    Sprite ghost = screen.createSprite();
    ghost.setFace("\uD83D\uDC7B");
    ghost.setOpacity(0.8f);
    ghost.setSpeed(10);
    ghost.setDirection(-50);
    ghost.setZ(1);
    ghost.setSize(30);
    ghost.setEdgeMode(EdgeMode.WRAP);
    ghost.say("Wrap");


    new Thread(() -> {
      try {
        Thread.sleep(4000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      ghost.setSize(50);}).start();
    // ghost.setText("Hello World");
  }
}