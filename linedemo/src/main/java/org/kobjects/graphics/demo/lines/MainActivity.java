package org.kobjects.graphics.demo.lines;

import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.twitter.TwitterEmojiProvider;

import org.kobjects.graphics.Pen;
import org.kobjects.graphics.Screen;

public class MainActivity extends AppCompatActivity {
  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    EmojiManager.install(new TwitterEmojiProvider());

    Screen screen = new Screen(this);

    screen.view.setBackgroundColor(Color.DKGRAY);
    setContentView(screen.view);
    final Pen pen = screen.createPen();

    new Thread(() -> {
      while (true) {
        pen.setLineColor((int) (Math.random() * 0xffffff) | 0xff000000);
        pen.drawLine(
            (float) (Math.random() * 400 - 200),
            (float) (Math.random() * 400 - 200),
            (float) (Math.random() * 400 - 200),
            (float) (Math.random() * 400 - 200));
      }
    }).start();
  }
}