package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class Authors : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.authors, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val authorsListView = view.findViewById<ListView>(R.id.authorsListView)

        val authors = listOf(
            Author("Чечиков Илья", "Главный разработчик", R.drawable.ic_launcher_foreground),
            //Author("123", "Разработчик", R.drawable.ic_launcher_background),
        )

        val adapter = AuthorAdapter(requireContext(), authors)
        authorsListView.adapter = adapter
    }
}

data class Author(
    val name: String,
    val role: String,
    val photoResId: Int
)

class AuthorAdapter(context: Context, private val authors: List<Author>) : ArrayAdapter<Author>(context, R.layout.item_author, authors) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(R.layout.item_author, parent, false)

        val author = getItem(position)
        val authorName: TextView = view.findViewById(R.id.authorName)
        val authorRole: TextView = view.findViewById(R.id.authorRole)
        val authorPhoto: ImageView = view.findViewById(R.id.authorPhoto)

        author?.let {
            authorName.text = it.name
            authorRole.text = it.role
            authorPhoto.setImageResource(it.photoResId)
        }

        return view
    }
}