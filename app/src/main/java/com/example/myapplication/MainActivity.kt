package com.example.myapplication

import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.core.content.res.ResourcesCompat
import com.example.myapplication.ui.theme.MyApplicationTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.main)

        val name: EditText = findViewById(R.id.name)
        val maleButton: RadioButton = findViewById(R.id.male)
        val femaleButton: RadioButton = findViewById(R.id.female)
        val course: Spinner = findViewById(R.id.course)
        val difficulty: SeekBar = findViewById(R.id.difficulty)
        val date: CalendarView = findViewById(R.id.date)
        val signUpButton: Button = findViewById(R.id.sign_up_button)
        val personInfo: TextView = findViewById(R.id.person_info)
        val image: ImageView = findViewById(R.id.image)

        var gender = ""
        maleButton.setOnClickListener { gender = maleButton.getText().toString() }
        femaleButton.setOnClickListener { gender = femaleButton.getText().toString() }

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val adapter: ArrayAdapter<String> = ArrayAdapter(this, android.R.layout.simple_spinner_item, courses)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        course.setAdapter(adapter);

        difficulty.setMax(2)

        date.setOnDateChangeListener { calView, year, month, dayOfMonth ->
            val calendar: Calendar = Calendar.getInstance()
            calendar.set(year, month, dayOfMonth)
            calView.setDate(calendar.timeInMillis, true, true)
        }

        signUpButton.setOnClickListener {
            val personName = name.text.toString()
            val personGender = gender
            val personCourse = course.selectedItem.toString()
            val personDiff = difficulty.progress.toString()
            val personDate = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
                .format(Date(date.date)).toString()
            val personZodiac = getSignByDate(personDate.split(".")[0].toInt() ,
                personDate.split(".")[1].toInt())

            val person = Person(personName, personGender, personCourse, personDiff, personDate, personZodiac)

            personInfo.text = person.toString()
            image.setImageDrawable(ResourcesCompat.getDrawable(resources, getImageBySign(personZodiac), null))
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Box(modifier = Modifier.fillMaxSize(), Alignment.Center)
    {
        Text(text = "Hello, $name!", modifier = modifier, textAlign = TextAlign.Center,
            color = Color.Blue, fontSize = 50.sp)
    }
}

private fun getSignByDate(day: Int, month: Int): String {
    return when (month) {
        1 -> if (day <= 20) "Козерог" else "Водолей"
        2 -> if (day <= 19) "Водолей" else "Рыбы"
        3 -> if (day <= 20) "Рыбы" else "Овен"
        4 -> if (day <= 20) "Овен" else "Телец"
        5 -> if (day <= 21) "Телец" else "Близнецы"
        6 -> if (day <= 21) "Близнецы" else "Рак"
        7 -> if (day <= 22) "Рак" else "Лев"
        8 -> if (day <= 23) "Лев" else "Дева"
        9 -> if (day <= 23) "Дева" else "Весы"
        10 -> if (day <= 23) "Весы" else "Скорпион"
        11 -> if (day <= 22) "Скорпион" else "Стрелец"
        12 -> if (day <= 21) "Стрелец" else "Козерог"
        else -> "Неизвестно"
    }
}

private fun getImageBySign(sign: String): Int {
    return when (sign) {
        "Козерог" -> R.drawable.capricorn
        "Водолей" -> R.drawable.aquarius
        "Рыбы" -> R.drawable.pisces
        "Овен" -> R.drawable.aries
        "Телец" -> R.drawable.taurus
        "Близнецы" -> R.drawable.gemini
        "Рак" -> R.drawable.cancer
        "Лев" -> R.drawable.leo
        "Дева" -> R.drawable.virgo
        "Весы" -> R.drawable.libra
        "Скорпион" -> R.drawable.scorpio
        "Стрелец" -> R.drawable.sagittarius
        else -> R.drawable.ic_launcher_foreground
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    MyApplicationTheme {
        Greeting("World")
    }
}