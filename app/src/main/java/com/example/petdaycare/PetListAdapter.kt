package com.example.petdaycare

import android.content.Context
import android.graphics.BitmapFactory
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage

class PetListAdapter (
    context: Context,
    viewToPaint: Int,
    private val petList: ArrayList<Pet>
        ) :
    ArrayAdapter<Pet>(context, viewToPaint, petList) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        Log.i("Demostration: ", "Executing getView")
        val inflater = LayoutInflater.from(context)
        val currentListItem = inflater.inflate(R.layout.pet_list_item, null)
        val petNameText: TextView = currentListItem.findViewById(R.id.petNameText)
        val petTypeText: TextView =currentListItem.findViewById(R.id.petTypeText)
        val petWeightText: TextView =currentListItem.findViewById(R.id.petWeightText)
        val petGenderText: TextView =currentListItem.findViewById(R.id.petGenderText)

        val petImage: ImageView =currentListItem.findViewById(R.id.petImage)
        val weightIcon: ImageView =currentListItem.findViewById(R.id.weightIcon)
        val genderIcon: ImageView =currentListItem.findViewById(R.id.genderIcon)



        val pet = getItem(position)
        petNameText.text= petList.get(position).name
        petTypeText.text= petList.get(position).type
        val petWeight= petList.get(position).weight.toString()
        petWeightText.text= "$petWeight Kg"
        petGenderText.text=  when (petList.get(position).gender) {
            Gender.FEMALE -> "Female"
            Gender.MALE -> "Male"
            Gender.OTHER -> "Other"
        }

        when (pet?.gender){
            Gender.FEMALE -> genderIcon.setImageResource(R.drawable.female_grey)
            Gender.MALE -> genderIcon.setImageResource(R.drawable.male_grey)
            else -> genderIcon.setImageResource(R.drawable.other_grey)
        }

        weightIcon.setImageResource(R.drawable.weight)
        weightIcon.setBackgroundResource(R.drawable.image_item_bg)

        if(pet!!.image){
            //get the image by id
            val imageReference = Firebase.storage.reference.child("images/${pet.id}.png")
            val storageReference = Firebase.storage.getReferenceFromUrl(imageReference.toString())
            val FIVE_MEGABYTES = 5 * 1024 * 1024.toLong()
            try {
                storageReference.getBytes(FIVE_MEGABYTES)
                    .addOnSuccessListener { bytes ->
                        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        petImage.setImageBitmap(bitmap)
                    }
                    .addOnFailureListener {
                        // Manejar el error si no se puede cargar la imagen
                        petImage.setImageResource(R.drawable.dog)
                    }
            }catch (e: Exception){
                Log.w("Image", "Error during image load from item in listView")
            }

        }else{
            petImage.setImageResource(R.drawable.dog)
        }



        return currentListItem
    }

}
