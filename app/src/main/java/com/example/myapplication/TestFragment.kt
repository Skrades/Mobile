package com.example.myapplication

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment

class TestFragment : Fragment() {

    companion object {
        fun newInstance(tabNumber: Int): TestFragment {
            val fragment = TestFragment()
            val args = Bundle()
            args.putInt("tabNumber", tabNumber)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val tabNumber = arguments?.getInt("tabNumber", 1) ?: 1

        val view = LinearLayout(requireContext()).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setBackgroundColor(Color.WHITE)
            gravity = Gravity.CENTER
            orientation = LinearLayout.VERTICAL
        }

        val textView = TextView(requireContext()).apply {
            text = "Вкладка $tabNumber"
            textSize = 24f
            setTextColor(Color.BLACK)
            gravity = Gravity.CENTER
        }

        view.addView(textView)
        return view
    }
}