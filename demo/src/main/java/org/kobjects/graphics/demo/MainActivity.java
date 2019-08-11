package org.kobjects.graphics.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.graphics.EdgeMode;
import org.kobjects.graphics.Screen;
import org.kobjects.graphics.Sprite;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EmojiManager.install(new TwitterEmojiProvider());

    Screen screen = new Screen(this);

    screen.view.setBackgroundColor(Color.DKGRAY);
    setContentView(screen.view);

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
//    ghost.setOpacity(0.8f);  // hides text somehow... :-/
    ghost.setSpeed(10);
    ghost.setDirection(80);
    ghost.setZ(1);
    ghost.setSize(30);
    ghost.setEdgeMode(EdgeMode.WRAP);
    ghost.say("Wrap");

    new Timer().scheduleAtFixedRate(new TimerTask() {
      long lastCall = System.currentTimeMillis();
      @Override
      public void run() {
        long now = System.currentTimeMillis();
        long dt = now - lastCall;
        if (dt > 5) {
          screen.animate(dt);
          lastCall = now;
        }
      }
    }, 0, 1000/60);
  }
}