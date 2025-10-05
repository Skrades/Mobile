package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.core.graphics.toColorInt


class MainActivity : FragmentActivity() {
    private lateinit var viewPager: ViewPager2
    private lateinit var tab1: Button
    private lateinit var tab2: Button
    private lateinit var tab3: Button
    private lateinit var tab4: Button

    private lateinit var start: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        viewPager = findViewById(R.id.pager)
        tab1 = findViewById(R.id.tab1)
        tab2 = findViewById(R.id.tab2)
        tab3 = findViewById(R.id.tab3)
        tab4 = findViewById(R.id.tab4)
        start = findViewById(R.id.startGame)

        val adapter = ViewPagerAdapter(this)
        viewPager.adapter = adapter

        tab1.setOnClickListener {
            viewPager.currentItem = 0
            updateTabStyles(0)
        }
        tab2.setOnClickListener {
            viewPager.currentItem = 1
            updateTabStyles(1)
        }
        tab3.setOnClickListener {
            viewPager.currentItem = 2
            updateTabStyles(2)
        }
        tab4.setOnClickListener {
            viewPager.currentItem = 3
            updateTabStyles(3)
        }

        start.setOnClickListener {
            try {
                startActivity(Intent(this@MainActivity, Game::class.java))
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Ошибка запуска игры: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateTabStyles(position)
            }
        })

        updateTabStyles(0)
    }

    private fun updateTabStyles(selectedPosition: Int) {
        val selectedColor = Color.WHITE
        val unselectedColor = "#e0e0e0".toColorInt()

        tab1.setBackgroundColor(if (selectedPosition == 0) selectedColor else unselectedColor)
        tab2.setBackgroundColor(if (selectedPosition == 1) selectedColor else unselectedColor)
        tab3.setBackgroundColor(if (selectedPosition == 2) selectedColor else unselectedColor)
        tab4.setBackgroundColor(if (selectedPosition == 3) selectedColor else unselectedColor)
    }
}