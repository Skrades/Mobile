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
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.AudioAttributes
import android.media.SoundPool
import android.view.MotionEvent
import android.view.ViewGroup
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AlertDialog
import kotlin.math.cos
import kotlin.math.sin
import androidx.core.view.contains
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.atan2

class Game : AppCompatActivity() {
    private lateinit var gameLayout: FrameLayout
    private lateinit var scoreTextView: TextView
    private lateinit var timerTextView: TextView
    private lateinit var menuButton: Button

    private var id : Long = 0
    private var difficulty : String? = "0"
    private var score = 0

    private var currentGoldRate: Double = 0.0
    private var modificator = 0
    private var gameTime = 60000L
    private var isGameRunning = false
    private var isPaused = false
    private var bonusActive = false
    private var lastSpawnTime = 0L;
    private var lastSpawn = 0L;
    private val insects = mutableListOf<ImageView>()
    private val insectTypes = listOf(
        Insect("Жук", R.drawable.bug, 10, 1.0f),
        Insect("Таракан", R.drawable.cockroach, 20, 2.0f),
        Insect("Муха", R.drawable.fly, 100, 4.0f),
        Insect("Паук", R.drawable.spider, -30, 1.0f),
        Insect("Божья коровка", R.drawable.ladybug, 5, 0.5f),
        Insect("Золотой жук", R.drawable.gold, currentGoldRate.toInt(), 0.5f)
    )

    private lateinit var countDownTimer: CountDownTimer
    private val handler = Handler(Looper.getMainLooper())

    val db : PlayerDatabase? = App.getInstance()?.getDataBase()
    val playerDao = db?.playerDao()

    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var sound: SoundPool? = null
    private var soundId: Int = 0
    private var accelerationX = 0f
    private var accelerationY = 0f
    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent?) {
            if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER && bonusActive) {
                accelerationX = event.values[0]
                accelerationY = event.values[1]
                applyBonusToInsects()
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.game)

        id = intent.getLongExtra("PLAYER_ID", 0)
        difficulty = intent.getStringExtra("PLAYER_DIFFICULTY")
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        sound = SoundPool.Builder()
            .setMaxStreams(5)
            .setAudioAttributes(audioAttributes)
            .build()

        soundId = sound?.load(this, R.raw.sound, 1) ?: 0

        lifecycleScope.launch {
            currentGoldRate = RetrofitClient.getGoldRate()
            insectTypes[5].points = currentGoldRate.toInt() * 3
        }

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
            bonusActive = false
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
            0 -> 30000L
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
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - lastSpawnTime >= 15000 && !bonusActive) {
                        spawnBonus()
                        lastSpawnTime = currentTime
                    }
                    if(currentTime - lastSpawn >= 20000)
                    {
                        spawnGold()
                        lastSpawn = currentTime
                    }
                }
            }

            override fun onFinish() {
                saveGameResults()
                isGameRunning = false
                bonusActive = false
                AlertDialog.Builder(this@Game)
                    .setTitle("Игра завершена!")
                    .setMessage("Ваш счет: $score")
                    .setPositiveButton("OK") { dialog, which ->
                        startActivity(Intent(this@Game, MainActivity::class.java))
                    }
                    .setCancelable(false)
                    .show()
            }
        }.start()
        startInsectSpawning()
    }

    private fun saveGameResults() {
        lifecycleScope.launch(Dispatchers.IO) {
            val player = playerDao?.getPlayerById(id)

            player?.let {
                val updatedPlayer = it.copy(
                    bestScore = maxOf(it.bestScore, score),
                )
                playerDao.update(updatedPlayer)
            }
        }
    }

    private fun activateBonus() {
        bonusActive = true

        sound?.play(0, 1.0f, 1.0f, 1, 0, 1.0f)

        sensorManager?.registerListener(
            sensorListener,
            accelerometer,
            SensorManager.SENSOR_DELAY_GAME
        )

        gameLayout.setBackgroundColor(Color.argb(30, 255, 0, 0))

        val soundHandler = Handler(Looper.getMainLooper())
        val soundRunnable = object : Runnable {
            override fun run() {
                if (bonusActive && isGameRunning) {
                    sound?.play(soundId, 1.0f, 1.0f, 1, 0, 1.0f)
                    soundHandler.postDelayed(this, 500)
                }
            }
        }
        soundHandler.post(soundRunnable)

        Handler(Looper.getMainLooper()).postDelayed({
            bonusActive = false
            sensorManager?.unregisterListener(sensorListener)

            gameLayout.setBackgroundColor(Color.TRANSPARENT)

            accelerationX = 0f
            accelerationY = 0f
        }, 5000L)
    }

    private fun spawnBonus() {

        val bonusView = ImageView(this).apply {
            setImageResource(R.drawable.bonus)
            layoutParams = ViewGroup.LayoutParams(100, 100)

            setOnClickListener {
                activateBonus()
                (it.parent as? ViewGroup)?.removeView(it)
            }
        }

        bonusView.x = Random.nextInt(bonusView.width + 100, gameLayout.width - 100).toFloat()
        bonusView.y = Random.nextInt(bonusView.height + 100, gameLayout.height - 100).toFloat()

        gameLayout.addView(bonusView)

        Handler(Looper.getMainLooper()).postDelayed({
            if (bonusView.parent != null && !bonusActive) {
                (bonusView.parent as? ViewGroup)?.removeView(bonusView)
            }
        }, 3000)
    }

    private fun spawnGold() {
        if (isPaused) return

        val insect = insectTypes[5]
        val insectView = ImageView(this).apply {
            setImageResource(insect.drawableRes)
            layoutParams = FrameLayout.LayoutParams(100, 100)
            setOnClickListener { onInsectClick(this, insect) }
        }

        insectView.x = Random.nextInt(insectView.width + 100, gameLayout.width - 100).toFloat()
        insectView.y = Random.nextInt(insectView.height + 100, gameLayout.height - 100).toFloat()

        gameLayout.addView(insectView)
        insects.add(insectView)

        startInsectMovement(insectView, insect.speedMultiplier)
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

        startInsectMovement(insectView, insect.speedMultiplier)
    }

    private fun startInsectMovement(insectView: ImageView, multiplier: Float) {
        val speed = getGameSpeed()
        val startX = insectView.x
        val startY = insectView.y

        val angle = Random.nextDouble(0.0, 2 * Math.PI).toFloat()
        val distance = 200f
        val targetX = startX + cos(angle) * distance
        val targetY = startY + sin(angle) * distance

        val animator = ValueAnimator.ofFloat(0f, 1f)
        animator.duration = (300 * (5-speed) / multiplier).toLong()
        animator.interpolator = LinearInterpolator()

        animator.addUpdateListener { animation ->
            if (!isPaused && isGameRunning && gameLayout.contains(insectView)) {
                if (bonusActive) {
                    return@addUpdateListener
                }
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
                    startInsectMovement(insectView, multiplier)
                }
            }
        })

        animator.start()
    }

    private fun applyBonusToInsects() {
        insects.forEach { insectView ->
            if (gameLayout.contains(insectView)) {
                val startX = insectView.x
                val startY = insectView.y
                val targetX = insectView.x - accelerationX * 20
                val targetY = insectView.y + accelerationY * 20

                val finalTargetX = targetX.coerceIn(0f, (gameLayout.width - insectView.width).toFloat())
                val finalTargetY = targetY.coerceIn(0f, (gameLayout.height - insectView.height).toFloat())

                val animator = ValueAnimator.ofFloat(0f, 1f).apply {
                    duration = 50
                    interpolator = LinearInterpolator()

                    addUpdateListener { animation ->
                        if (bonusActive && gameLayout.contains(insectView)) {
                            val fraction = animation.animatedValue as Float

                            insectView.x = startX + (finalTargetX - startX) * fraction
                            insectView.y = startY + (finalTargetY - startY) * fraction

                            val angle = Math.toDegrees(atan2(
                                (finalTargetY - startY).toDouble(),
                                (finalTargetX - startX).toDouble()
                            )).toFloat()
                            insectView.rotation = angle
                        }
                    }
                }
                animator.start()
            }
        }
    }

    private fun onInsectClick(insectView: ImageView, insect: Insect) {
        if (isPaused) return

        gameLayout.removeView(insectView)
        insects.remove(insectView)

        modificator = if(insect.type == "Паук") {
            insect.points * difficulty!!.toInt()
        } else {
            insect.points * (4 - (difficulty!!.toInt() + 1))
        }
        score += modificator
        updateScore()

        showPointsAnimation(insectView.x, insectView.y, modificator)
    }

    private fun onEmptyClick(x: Float, y: Float) {
        if (isPaused) return
        modificator = (difficulty!!.toInt() * 3)
        score -= modificator
        updateScore()

        showPointsAnimation(x, y, -modificator)
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
                    handler.postDelayed(this, 400 * (difficulty!!.toLong() + 1))
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
        var points: Int,
        val speedMultiplier: Float
    )
}