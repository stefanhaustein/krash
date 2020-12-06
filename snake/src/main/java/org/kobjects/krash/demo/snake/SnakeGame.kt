package org.kobjects.krash.demo.snake

import android.graphics.Color
import android.webkit.WebHistoryItem
import org.kobjects.krash.api.GamepadKey
import org.kobjects.krash.api.Screen

class SnakeGame(val screen: Screen) {

    val SIZE = 20
    val ROCK = screen.createEmoji("◻️")
    val APPLE = screen.createEmoji("\uD83C\uDF4E")
    val SKULL = screen.createEmoji("\uD83D\uDC80")
    val SNAKE = screen.createEmoji("🐍");
    val SNAKE_HEAD = screen.createEmoji("\uD83D\uDC38")
    val SNAKE_SEGMENT = screen.createEmoji("\uD83D\uDFE2")
    val BONE = screen.createEmoji("⚪");

    val grid = screen.createGrid(SIZE, SIZE)
    val gridSprite = screen.createSprite(grid);
    val snakeTail = mutableListOf<Int>()
    val scoreSprite = screen.createSprite(screen.createText(""))
    val highScoreSprite = screen.createSprite(screen.createText(""))

    var snakeX = SIZE / 2
    var snakeY = SIZE / 2
    var snakeDirection: GamepadKey? = null
    var tailPos = 0
    var score = 0
    var gameOver = false
    var highScore = 0

    init {
        gridSprite.setWidth(200F)

        scoreSprite.anchor(screen, 1f, 0f);
        scoreSprite.setPivotX(1f);
        scoreSprite.setPivotY(0f);
        highScoreSprite.anchor(screen, 0f, 0f);
        highScoreSprite.setPivotX(0f);
        highScoreSprite.setPivotY(0f);

        reset()

        screen.addKeyDownListener(GamepadKey.UP, { snakeDirection = GamepadKey.UP})
        screen.addKeyDownListener(GamepadKey.DOWN, { snakeDirection = GamepadKey.DOWN })
        screen.addKeyDownListener(GamepadKey.RIGHT, { snakeDirection = GamepadKey.RIGHT })
        screen.addKeyDownListener(GamepadKey.LEFT, { snakeDirection = GamepadKey.LEFT })

        screen.schedule(0.2F, ::moveSnake);
    }

    fun reset() {
        score = 0
        snakeX = SIZE / 2
        snakeY = SIZE / 2
        snakeTail.clear()
        for (i in 1..6) {
            snakeTail.add(SIZE / 2)
        }
        tailPos = 0
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                grid.tile(x, y).content = if (x == 0 || x == SIZE - 1 || y == 0 || y == SIZE - 1) ROCK else null
            }
        }

        placeApple()

        grid.tile(snakeX, snakeY).content = SNAKE
    }

    fun upadateScore() {
        val scoreText = screen.createText(score.toString())
        scoreText.size = 16F
        scoreText.color = Color.WHITE

        scoreSprite.setContent(scoreText)
    }

    fun placeApple() {
        while (true) {
            val x = 1 + (Math.random() * (SIZE - 2)).toInt()
            val y = 1 + (Math.random() * (SIZE - 2)).toInt()
            if (grid.tile(x, y).content == null) {
                grid.tile(x, y).content = APPLE
                return
            }
        }
    }

    fun gameOver() {
        gameOver = true
        snakeDirection = null
        for (i in 0 until snakeTail.size step 2) {
            grid.tile(snakeTail[i], snakeTail[i+1]).content = BONE
        }
        grid.tile(snakeX, snakeY).setContent(SKULL)
        if (score > highScore) {
            highScoreSprite.setContent(scoreSprite.content);
        }

    }

    fun moveSnake() {
        if (gameOver) {
            if (snakeDirection != null) {
                gameOver = false;
                reset()
                upadateScore()
                snakeDirection = null;
            }
            return
        }

        if (snakeDirection == null) {
            return
        }
        val oldX = snakeTail[tailPos]
        val oldY = snakeTail[tailPos+1]
        grid.tile(oldX, oldY).content = null
        grid.tile(snakeX, snakeY).content = SNAKE_SEGMENT
        when (snakeDirection) {
            GamepadKey.UP -> snakeY--
            GamepadKey.RIGHT -> snakeX++
            GamepadKey.DOWN -> snakeY++
            GamepadKey.LEFT -> snakeX--
        }

        when(grid.tile(snakeX, snakeY).getContent()) {
            APPLE -> {
                snakeTail.add(tailPos, 0)
                snakeTail.add(tailPos, 0)
                score++
                upadateScore()
                placeApple()
            }
            null -> {}
            else -> {
                gameOver()
                return
            }
        }
        grid.tile(snakeX, snakeY).setContent(SNAKE_HEAD)

        snakeTail[tailPos++] = snakeX;
        snakeTail[tailPos++] = snakeY;
        tailPos %= snakeTail.size;
    }
}