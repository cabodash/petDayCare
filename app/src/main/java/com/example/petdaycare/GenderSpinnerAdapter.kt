package com.example.petdaycare

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class GenderSpinnerAdapter(context: Context, genderList: Array<Gender>, private val genderImages: Array<Int>) :
    ArrayAdapter<Gender>(context, R.layout.spinner_item, genderList) {

    private val inflater: LayoutInflater = LayoutInflater.from(context)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.spinner_item, parent, false)

        val gender = getItem(position)

        // Configurar el ImageView y el TextView
        val imageView = view.findViewById<ImageView>(R.id.imageItemSpinner)
        val textView = view.findViewById<TextView>(R.id.textItemSpinner)

        imageView.setImageResource(genderImages[position])
        textView.text = gender?.let { getGenderDisplayName(it) }

        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: inflater.inflate(R.layout.spinner_item, parent, false)

        val gender = getItem(position)

        // Configurar el ImageView y el TextView
        val imageView = view.findViewById<ImageView>(R.id.imageItemSpinner)
        val textView = view.findViewById<TextView>(R.id.textItemSpinner)

        imageView.setImageResource(genderImages[position])
        textView.text = gender?.let { getGenderDisplayName(it) }

        return view
    }

    private fun getGenderDisplayName(gender: Gender): String {
        // Devuelve el nombre del género como se mostrará en el Spinner
        return when (gender) {
            Gender.FEMALE -> "Female"
            Gender.MALE -> "Male"
            Gender.OTHER -> "Other"
        }
    }
}