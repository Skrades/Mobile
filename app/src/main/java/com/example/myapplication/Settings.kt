package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class Settings : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupSettings(view)
    }

    private fun setupSettings(view: View) {
        val speedSeekBar = view.findViewById<SeekBar>(R.id.speedSeekBar)
        val speedValue = view.findViewById<TextView>(R.id.speedValue)
        val cockroachesSeekBar = view.findViewById<SeekBar>(R.id.cockroachesSeekBar)
        val cockroachesValue = view.findViewById<TextView>(R.id.cockroachesValue)
        val bonusIntervalSeekBar = view.findViewById<SeekBar>(R.id.bonusIntervalSeekBar)
        val bonusIntervalValue = view.findViewById<TextView>(R.id.bonusIntervalValue)
        val roundDurationSeekBar = view.findViewById<SeekBar>(R.id.roundDurationSeekBar)
        val roundDurationValue = view.findViewById<TextView>(R.id.roundDurationValue)
        val saveButton = view.findViewById<Button>(R.id.saveSettingsButton)

        setupSeekBar(speedSeekBar, speedValue, arrayOf("Очень медленно", "Медленно", "Средне", "Быстро", "Очень быстро"))

        cockroachesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                cockroachesValue.text = "${progress + 5} тараканов"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        setupSeekBar(bonusIntervalSeekBar, bonusIntervalValue, arrayOf("Очень редко", "Редко", "Средне", "Часто", "Очень часто"))

        setupSeekBar(roundDurationSeekBar, roundDurationValue, arrayOf("1 минута", "2 минуты", "3 минуты", "4 минуты", "5 минут"))

        saveButton.setOnClickListener {
            saveSettings(
                speedSeekBar.progress,
                cockroachesSeekBar.progress + 5,
                bonusIntervalSeekBar.progress,
                roundDurationSeekBar.progress
            )
        }
    }

    private fun setupSeekBar(seekBar: SeekBar, textView: TextView, labels: Array<String>) {
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                textView.text = labels[progress]
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun saveSettings(speed: Int, cockroaches: Int, bonusInterval: Int, roundDuration: Int) {
        val sharedPref = requireActivity().getSharedPreferences("game_settings", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("game_speed", speed)
            putInt("max_cockroaches", cockroaches)
            putInt("bonus_interval", bonusInterval)
            putInt("round_duration", roundDuration)
            apply()
        }

        Toast.makeText(requireContext(), "Настройки сохранены!", Toast.LENGTH_SHORT).show()
    }
}