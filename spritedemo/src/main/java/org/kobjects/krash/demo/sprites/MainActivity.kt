package org.kobjects.krash.demo.sprites

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.vanniktech.emoji.EmojiManager
import com.vanniktech.emoji.twitter.TwitterEmojiProvider
import org.kobjects.krash.android.AndroidScreen
import org.kobjects.krash.api.EdgeMode
import org.kobjects.krash.api.Sprite

class MainActivity : AppCompatActivity() {
    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EmojiManager.install(TwitterEmojiProvider())
        val screen = AndroidScreen(this)
        //  screen.setViewport(320, 200, true);
        screen.view.setBackgroundColor(Color.DKGRAY)
        setContentView(screen.view)

        // Size comparison
        val pen = screen.createPen()
        pen.drawRect(-15f, -15f, 30f, 30f)
        val grid = screen.createGrid(8, 8)
        for (y in 0..7) {
            for (x in 0..7) {
                grid[x, y] = screen.createEmoji(if (x + y and 1 == 0) "⬛" else "⬜")
            }
        }
        val gridHolder = screen.createSprite(grid);
        gridHolder.setWidth(200f)
        gridHolder.rotation = 1f
        val ball = screen.createSprite(
                screen.createSvg("<svg height='100' width='100'><circle cx='50' cy='50' r='40' stroke='black' stroke-width='3' fill='red' /></svg>"))
        //        ball.face = "⚽"
        ball.setWidth(20f)
        ball.speed = 20f
        ball.direction = 20f
        ball.edgeMode = EdgeMode.BOUNCE
        ball.rotation = -90f
        ball.say("Bounce")
        val ghost  = screen.createSprite(screen.createEmoji("\uD83D\uDC7B"))
        ghost.opacity = 0.8f
        ghost.speed = 10f
        ghost.direction = -50f
        ghost.z = 1f
        ghost.setWidth(30f)
        ghost.edgeMode = EdgeMode.WRAP
        ghost.say("Wrap")
        Thread {
            try {
                Thread.sleep(4000)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            ghost.setWidth(50f)
        }.start()
        // ghost.setText("Hello World");
    }
}