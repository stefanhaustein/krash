package org.kobjects.krash.demo.snake

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import org.kobjects.krash.android.AndroidScreen
import org.kobjects.krash.api.Emoji
import org.kobjects.krash.api.Grid
import org.kobjects.krash.api.Sprite

class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val screen = AndroidScreen(this);

        setContentView(screen.view)

        SnakeGame(screen)
    }
}