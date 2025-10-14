package com.example.myapplication
import android.icu.util.Calendar
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CalendarView
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Registration : Fragment() {

    private lateinit var name: EditText
    private lateinit var maleButton: RadioButton
    private lateinit var femaleButton: RadioButton
    private lateinit var course: Spinner
    private lateinit var difficulty: SeekBar
    private lateinit var date: CalendarView
    private lateinit var signUpButton: Button
    private lateinit var personInfo: TextView
    private lateinit var image: ImageView
    val db : PlayerDatabase? = App.getInstance()?.getDataBase()
    val playerDao = db?.playerDao()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(
            R.layout.registration, container, false
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRegistrationForm(view)
    }

    fun setupRegistrationForm(view: View)
    {
        name = view.findViewById(R.id.name)
        maleButton = view.findViewById(R.id.male)
        femaleButton = view.findViewById(R.id.female)
        course = view.findViewById(R.id.course)
        difficulty = view.findViewById(R.id.difficulty)
        date = view.findViewById(R.id.date)
        signUpButton = view.findViewById(R.id.sign_up_button)
        personInfo = view.findViewById(R.id.person_info)
        image = view.findViewById(R.id.image)

        var gender = ""
        maleButton.setOnClickListener { gender = maleButton.getText().toString() }
        femaleButton.setOnClickListener { gender = femaleButton.getText().toString() }

        val courses = arrayOf("1 курс", "2 курс", "3 курс", "4 курс")
        val adapter: ArrayAdapter<String> = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, courses)
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
            val player = Player(
                name = personName,
                gender = personGender,
                course = personCourse,
                difficulty = personDiff,
                date = personDate,  // исправлено с date на birthDate
                zodiac = personZodiac
            )
            personInfo.text = player.toString()
            image.setImageDrawable(ResourcesCompat.getDrawable(resources, getImageBySign(personZodiac), null))
            lifecycleScope.launch(Dispatchers.IO) {
                playerDao?.insert(player)
            }
            println("Игрок сохранен")
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
}