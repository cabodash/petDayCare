package com.example.petdaycare

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class PetListActivity : AppCompatActivity() {
    private var petList= arrayListOf<Pet>()
    private lateinit var petListView: ListView
    private lateinit var empty : TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pet_list)
        val petOptionsImage: ImageView = findViewById(R.id.petOptionsImage)
        petListView = findViewById(R.id.petListView)
        empty = findViewById(R.id.empty)

        getPetCollection()



        petOptionsImage.setOnClickListener {
            val intent = Intent(this, OptionPetActivity::class.java)
            intent.putExtra("action", "new")
            startActivity(intent)
        }
        petListView.setOnItemClickListener { _, _, i, _ ->
            val intent = Intent(this, OptionPetActivity::class.java)
            intent.putExtra("action", "edit")
            intent.putExtra("pet", petList[i])
            startActivity(intent)
        }
    }


    override fun onBackPressed() {
    }


    private fun getPetCollection() {
        val db = Firebase.firestore

        db.collection("pets")
            .get()
            .addOnSuccessListener { result ->
                for (document in result) {
                    val currentId = document.id
                    val currentName = document.data["name"].toString()
                    val currentType = document.data["type"].toString()
                    val currentGender = when (document.data["gender"].toString()) {
                        "male" -> Gender.MALE
                        "female" -> Gender.FEMALE
                        else -> Gender.OTHER
                    }
                    val currentWeight = document.data["weight"].toString().toFloat()
                    val currentImage = when (document.data["image"].toString()){
                        "true" -> true
                        else -> false
                    }

                    val currentPet = Pet(currentId, currentName, currentType, currentGender, currentWeight, currentImage)
                    petList.add(currentPet)
                }

                val petAdapter = PetListAdapter(applicationContext, R.layout.pet_list_item, petList)
                petListView.adapter = petAdapter
                petListView.emptyView = empty
            }
            .addOnFailureListener {
                Toast.makeText(applicationContext, "An error has occurred while loading pets", Toast.LENGTH_LONG).show()
                petListView.emptyView = empty
            }
    }
}