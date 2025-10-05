package com.example.myapplication

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.graphics.Color
import android.view.MotionEvent
import android.view.animation.LinearInterpolator
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.view.contains
import kotlin.math.atan2

class Game : AppCompatActivity() {
    private lateinit var gameLayout: FrameLayout
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var menuButton: Button

    private var score = 0
    private var gameTime = 60000L
    private var isGameRunning = false
    private var isPaused = false

    private val insects = mutableListOf<ImageView>()
    private val insectTypes = listOf(
        Insect("Жук", R.drawable.bug, 10, 1.0f),
        Insect("Таракан", R.drawable.cockroach, 20, 2.0f),
        Insect("Муха", R.drawable.fly, 100, 4.0f),
        Insect("Паук", R.drawable.spider, -10, 1.0f),
        Insect("Божья коровка", R.drawable.ladybug, 5, 0.5f),
    )

    private lateinit var countDownTimer: CountDownTimer
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)

        initializeViews()
        setupGame()
        startGame()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initializeViews() {
        gameLayout = findViewById(R.id.gameLayout)
        scoreTextView = findViewById(R.id.scoreTextView)
        timerTextView = findViewById(R.id.timerTextView)
        menuButton = findViewById(R.id.backButton)

        menuButton.setOnClickListener {
            isGameRunning = false
            countDownTimer.cancel()
            startActivity(Intent(this@Game, MainActivity::class.java))
        }

        gameLayout.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val x = event.x
                    val y = event.y
                    onEmptyClick(x, y)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupGame() {
        val sharedPref = getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        val roundDuration = sharedPref.getInt("round_duration", 1)

        gameTime = when (roundDuration) {
            0 -> 60000L
            1 -> 120000L
            2 -> 180000L
            3 -> 240000L
            4 -> 300000L
            else -> 120000L
        }

        score = 0
        updateScore()
        updateTimer(gameTime)
    }

    private fun startGame() {
        isGameRunning = true
        isPaused = false

        countDownTimer = object : CountDownTimer(gameTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                if (!isPaused && isGameRunning) {
                    updateTimer(millisUntilFinished)
                }
            }

            override fun onFinish() {
                isGameRunning = false
                startActivity(Intent(this@Game, MainActivity::class.java))
            }
        }.start()
        startInsectSpawning()
    }

    private fun spawnInsect() {
        if (insects.size >= getMaxInsects() || isPaused) return

        val insect = getRandomInsect()
        val insectView = ImageView(this).apply {
            setImageResource(insect.drawableRes)
            layoutParams = FrameLayout.LayoutParams(100, 100)
            setOnClickListener { onInsectClick(this, insect) }
        }

        insectView.x = Random.nextInt(insectView.width + 100, gameLayout.width - 100).toFloat()
        insectView.y = Random.nextInt(insectView.height + 100, gameLayout.height - 100).toFloat()

        gameLayout.addView(insectView)
        insects.add(insectView)

        startInsectMovement(insectView, insect)
    }

    private fun startInsectMovement(insectView: ImageView, insect: Insect) {
        val speed = getGameSpeed()
        val startX = insectView.x
        val startY = insectView.y

        val angle = Random.nextDouble(0.0, 2 * Math.PI).toFloat()
        val distance = 200f
        val targetX = startX + cos(angle) * distance
        val targetY = startY + sin(angle) * distance

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = (300 * (5-speed) / insect.speedMultiplier).toLong()
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            if (!isPaused && isGameRunning && gameLayout.contains(insectView)) {
                val fraction = animation.animatedValue as Float
                insectView.x = startX + (targetX - startX) * fraction
                insectView.y = startY + (targetY - startY) * fraction
            }
            var angle = Math.toDegrees(atan2(
                (targetY - startY).toDouble(),
                (targetX - startX).toDouble()
            )).toFloat()
            angle += 90
            insectView.rotation = angle

            if (insectView.x + insectView.width < 0 || insectView.x - gameLayout.width > insectView.width ||
                insectView.y + insectView.height < 0 || insectView.y - gameLayout.height > insectView.height) {
                gameLayout.removeView(insectView)
                insects.remove(insectView)
            }
        }

        animator.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                if (!isPaused && isGameRunning && gameLayout.contains(insectView)) {
                    startInsectMovement(insectView, insect)
                }
            }
        })

        animator.start()
    }

    private fun onInsectClick(insectView: ImageView, insect: Insect) {
        if (isPaused) return

        gameLayout.removeView(insectView)
        insects.remove(insectView)

        score += insect.points
        updateScore()

        showPointsAnimation(insectView.x, insectView.y, insect.points)
    }

    private fun onEmptyClick(x: Float, y: Float) {
        if (isPaused) return

        score -= 3
        updateScore()

        showPointsAnimation(x, y, -3)
    }

    private fun showPointsAnimation(x: Float, y: Float, points: Int) {
        val pointsView = TextView(this).apply {
            text = if (points > 0) "+$points" else "$points"
            setTextColor(if (points > 0) Color.GREEN else Color.RED)
            textSize = 20f
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                leftMargin = x.toInt()
                topMargin = y.toInt()
            }
        }

        gameLayout.addView(pointsView)

        pointsView.animate()
            .alpha(0f)
            .translationY(-100f)
            .setDuration(1000)
            .withEndAction {
                gameLayout.removeView(pointsView)
            }
            .start()
    }

    private fun startInsectSpawning() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (isGameRunning && !isPaused) {
                    spawnInsect()
                    val delay = Random.nextLong(500, 2000)
                    handler.postDelayed(this, delay)
                }
            }
        }, 1000)
    }

    private fun getBonusInterval(): Int {
        val sharedPref = getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        return sharedPref.getInt("bonus_interval", 2)
    }

    private fun getRandomInsect(): Insect {
        val weights = when (getBonusInterval()) {
            0 -> listOf(40, 30, 20, 10, 0)
            1 -> listOf(35, 30, 20, 10, 5)
            2 -> listOf(30, 25, 20, 15, 10)
            3 -> listOf(25, 20, 20, 15, 20)
            4 -> listOf(20, 15, 15, 10, 40)
            else -> listOf(30, 25, 20, 15, 10)
        }

        val random = Random.nextInt(100)
        var currentWeight = 0

        for (i in weights.indices) {
            currentWeight += weights[i]
            if (random < currentWeight) {
                return insectTypes[i]
            }
        }

        return insectTypes[0]
    }

    private fun getMaxInsects(): Int {
        val sharedPref = getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        return sharedPref.getInt("max_cockroaches", 8)
    }

    private fun getGameSpeed(): Int {
        val sharedPref = getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        return sharedPref.getInt("game_speed", 2)
    }

    private fun updateScore() {
        scoreTextView.text = "Очки: $score"
    }

    private fun updateTimer(millisUntilFinished: Long) {
        val seconds = millisUntilFinished / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        timerTextView.text = String.format("%02d:%02d", minutes, remainingSeconds)
    }

    data class Insect(
        val type: String,
        val drawableRes: Int,
        val points: Int,
        val speedMultiplier: Float
    )
}