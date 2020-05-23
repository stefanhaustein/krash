package org.kobjects.krash.demo.sprites;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.krash.EdgeMode;
import org.kobjects.krash.Pen;
import org.kobjects.krash.Screen;
import org.kobjects.krash.Sprite;

public class MainActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EmojiManager.install(new TwitterEmojiProvider());

    Screen screen = new Screen(this);
  //  screen.setViewport(320, 200, true);

    screen.view.setBackgroundColor(Color.DKGRAY);
    setContentView(screen.view);

    // Size comparison
    Pen pen = screen.createPen();
    pen.drawRect(-15, -15, 30, 30);


    Sprite ball = new Sprite(screen);
    ball.setFace("⚽");
    ball.setSize(30);
    ball.setSpeed(20);
    ball.setDirection(20);
    ball.setEdgeMode(EdgeMode.BOUNCE);
    ball.setRotation(-90);
    ball.say("Bounce");

    Sprite ghost = new Sprite(screen);
    ghost.setFace("\uD83D\uDC7B");
    ghost.setOpacity(0.8f);
    ghost.setSpeed(10);
    ghost.setDirection(-50);
    ghost.setZ(1);
    ghost.setSize(30);
    ghost.setEdgeMode(EdgeMode.WRAP);
    ghost.say("Wrap");

  }
}