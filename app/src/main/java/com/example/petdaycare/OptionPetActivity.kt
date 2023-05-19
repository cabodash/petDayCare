package com.example.petdaycare

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class OptionPetActivity : AppCompatActivity() {
    companion object {
        private const val REQUEST_IMAGE_CAPTURE = 1
        private const val REQUEST_IMAGE_GALLERY = 2
    }


    var action: String? = ""
    var imageRes: Bitmap? = null
    var imageChanged: Boolean = false
    var editPet: Pet? = null
    var idEditPet: String = ""

    private lateinit var namePetText: EditText
    private lateinit var typePetText: EditText
    private lateinit var genderSpinner: Spinner
    private lateinit var weightPetText: EditText
    private lateinit var selectImageButton: Button
    private lateinit var saveButton: Button
    private lateinit var petImage: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        val bundle = intent.extras
        val action = bundle?.getString("action")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_option_pet)

        namePetText = findViewById(R.id.namePetText)
        typePetText = findViewById(R.id.typePetText)
        genderSpinner = findViewById(R.id.genderSpinner)
        weightPetText = findViewById(R.id.weightPetText)
        selectImageButton = findViewById(R.id.selectImageButton)
        saveButton = findViewById(R.id.saveButton)
        petImage = findViewById(R.id.petImage)

        selectImageButton.setOnClickListener {
            Log.i("Info", "Clicking SelectImageButton")
            selectImage()
        }
        val genderList = arrayOf(Gender.FEMALE, Gender.MALE, Gender.OTHER)
        val genderImages = arrayOf(R.drawable.female, R.drawable.male, R.drawable.other)
        val spinnerGenderAdapter = GenderSpinnerAdapter(this, genderList, genderImages)

        genderSpinner.adapter = spinnerGenderAdapter
        petImage.setImageResource(R.drawable.dog)


        //If there is a new pet to create or there is a pet to modify
        if (action == "new") {
            supportActionBar?.title = "New Pet"
        } else {
            supportActionBar?.title = "Edit Pet"
            saveButton.text = getString(R.string.modifyButton)

            val pet = intent.getSerializableExtra("pet") as? Pet

            if (pet != null) {
                idEditPet = pet.id
                namePetText.text = Editable.Factory.getInstance().newEditable(pet.name)
                typePetText.text = Editable.Factory.getInstance().newEditable(pet.type)
                val selectedOption =
                    when (pet.gender) {
                        Gender.FEMALE -> 0
                        Gender.MALE -> 1
                        else -> 2
                    }
                genderSpinner.setSelection(selectedOption)
                weightPetText.text =
                    Editable.Factory.getInstance().newEditable(pet.weight.toString())

                if (pet.image) {
                    //Load the image from the database stogare
                    val imageReference = Firebase.storage.reference.child("images/${pet.id}.png")
                    val storageReference =
                        Firebase.storage.getReferenceFromUrl(imageReference.toString())
                    val FIVE_MEGABYTES = 5 * 1024 * 1024.toLong()
                    storageReference.getBytes(FIVE_MEGABYTES)
                        .addOnSuccessListener { bytes ->
                            val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            petImage.setImageBitmap(bitmap)
                            imageRes = bitmap
                        }
                        .addOnFailureListener {
                            petImage.setImageResource(R.drawable.dog)
                        }
                }

            } else {
                //Return to the Pet list due to an error with the pet
                Toast.makeText(
                    applicationContext,
                    "An error occurred while loading the pet data",
                    Toast.LENGTH_LONG
                ).show()
                val intent = Intent(this, PetListActivity::class.java)
                startActivity(intent)
            }
        }

        saveButton.setOnClickListener {
            //TODO newPet or modifyPet, with an if
            if (action == "new") {
                newPet()
            } else {
                updatePet()
            }
        }
    }


    private fun uploadImageToFirebaseStorage(image: Bitmap?, petId: String) {
        val imageRef = Firebase.storage.reference.child("images/${petId}.png")

        val baos = ByteArrayOutputStream()
        image?.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val imageData = baos.toByteArray()

        val uploadTask = imageRef.putBytes(imageData)
        uploadTask.addOnSuccessListener {
            Log.i("Images", "Image uploaded successfully")
            startActivity(Intent(applicationContext, PetListActivity::class.java))
        }.addOnFailureListener { e ->
            Toast.makeText(
                applicationContext,
                "An error occurred while uploading the image",
                Toast.LENGTH_SHORT
            ).show()
            startActivity(Intent(applicationContext, PetListActivity::class.java))
        }
    }


    fun areEmptyFields(): Boolean {
        var valid = false
        val namePet = namePetText.text.toString().trim()
        val typePet = typePetText.text.toString().trim()
        val weightPet = weightPetText.text.toString().toFloatOrNull()
        if (namePet.isEmpty()) {
            valid = true
        }
        if (typePet.isEmpty()) {
            valid = true
        }
        if (weightPet == null || weightPet > 50F || weightPet < 0F) {
            valid = true
        }

        return valid
    }

    fun getDataFromFields(): Pet {
        val namePet = namePetText.text.toString().trim()
        val typePet = typePetText.text.toString().trim()
        val weightPet = weightPetText.text.toString().toFloatOrNull()
        var petImageOpt = false
        if (imageChanged) {
            petImageOpt = true
        }
        return Pet(
            "", namePet, typePet, (genderSpinner.selectedItem as Gender),
            weightPet!!, petImageOpt
        )

    }

    /** Create a new pet, get the texts out of the fields and check if there is an image in the image global variable
     */
    private fun newPet() {
        if (areEmptyFields()) {
            Toast.makeText(
                applicationContext,
                "There are fields with invalid data or without it, check them",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            val petDb = getDataFromFields()
            val genderPet = when (petDb.gender) {
                Gender.MALE -> "male"
                Gender.FEMALE -> "female"
                else -> "other"
            }
            val imagePet = when (imageChanged) {
                true -> "true"
                else -> "false"
            }
            val db = Firebase.firestore
            val pet = hashMapOf(
                "name" to petDb.name,
                "type" to petDb.type,
                "gender" to genderPet,
                "weight" to petDb.weight,
                "image" to imagePet
            )
            db.collection("pets")
                .add(pet)
                .addOnSuccessListener { documentReference ->
                    Toast.makeText(applicationContext, "Pet added succesfully", Toast.LENGTH_SHORT)
                        .show()
                    if (imageRes != null) {
                        val petId = documentReference.id
                        uploadImageToFirebaseStorage(imageRes, petId)
                    } else {
                        startActivity(Intent(applicationContext, PetListActivity::class.java))
                    }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        applicationContext,
                        "An error has occurred while saving the pet",
                        Toast.LENGTH_SHORT
                    ).show()
                }

        }

    }

    /** Edit the selected pet, get the texts out of the fields and check if there is an image in the image global variable
     */
    private fun updatePet() {
        if (areEmptyFields()) {
            Toast.makeText(
                applicationContext,
                "There are fields without data, please fill them in",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            editPet = getDataFromFields()
            editPet!!.id = idEditPet
            val oldPet = intent.getSerializableExtra("pet") as? Pet
            if (editPet!! == oldPet && !imageChanged) {
                Toast.makeText(
                    applicationContext,
                    "there are no changes in the pet",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val genderPet = when (editPet!!.gender) {
                    Gender.MALE -> "male"
                    Gender.FEMALE -> "female"
                    else -> "other"
                }
                var imagePet = ""
                if (oldPet!!.image) {
                    imagePet = "true"
                } else {
                    imagePet = "false"
                }
                if (imageChanged) {
                    imagePet = "true"
                }

                if (editPet != null) {
                    val db = Firebase.firestore
                    val updatedPet = hashMapOf(
                        "name" to editPet!!.name,
                        "type" to editPet!!.type,
                        "gender" to genderPet,
                        "weight" to editPet!!.weight,
                        "image" to imagePet
                    )
                    editPet?.id?.let {
                        db.collection("pets")
                            .document(it)
                            .set(updatedPet)
                            .addOnSuccessListener {
                                Toast.makeText(
                                    applicationContext,
                                    "Pet updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()

                                if (imageChanged) {
                                    uploadImageToFirebaseStorage(imageRes, editPet!!.id)
                                } else {
                                    startActivity(
                                        Intent(
                                            applicationContext,
                                            PetListActivity::class.java
                                        )
                                    )
                                }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(
                                    applicationContext,
                                    "An error has occurred while updating the pet",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            }

        }
    }


    //Show an alert if the changes are not saved
    private fun showExitAlert() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.exit_alert_frame, null)

        val negativeBtn = dialogView.findViewById<Button>(R.id.negativeBtn)
        val positiveBtn = dialogView.findViewById<Button>(R.id.positiveBtn)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        positiveBtn.setOnClickListener {
            action = ""
            dialog.dismiss()
            finish()
        }

        negativeBtn.setOnClickListener() {
            dialog.dismiss()
        }

    }

    //Show an alert with the options:
    // 1.Take photo
    // 2.Select photo from gallery
    // 3.Exit
    private fun selectImage() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.select_photo_frame, null)

        val takePhotoButton = dialogView.findViewById<Button>(R.id.takePhotoButton)
        val galleryButton = dialogView.findViewById<Button>(R.id.galleryButton)
        val cancelButton = dialogView.findViewById<Button>(R.id.cancelButton)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        takePhotoButton.setOnClickListener() {
            dialog.dismiss()
            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
        }

        galleryButton.setOnClickListener() {
            dialog.dismiss()
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(intent, REQUEST_IMAGE_GALLERY)
        }

        cancelButton.setOnClickListener() {
            dialog.dismiss()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_CAPTURE -> {
                    val imageBitmap = data?.extras?.get("data") as Bitmap
                    loadImage(imageBitmap)
                }
                REQUEST_IMAGE_GALLERY -> {
                    val imageUri = data?.data
                    val imageBitmap =
                        MediaStore.Images.Media.getBitmap(this.contentResolver, imageUri)
                    loadImage(imageBitmap)
                }
            }
        }
    }

    /** Loads a Bitmap image passed by parameter in the Image View and set image
     */
    private fun loadImage(imageBitmap: Bitmap) {
        val outputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)

        val imageBytes = outputStream.toByteArray()

        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)

        val imageWidth = options.outWidth
        val imageHeight = options.outHeight

        val imageSize = imageWidth * imageHeight * 4

        if (imageSize > 50000000) {
            Toast.makeText(this, "The image exceeds the maximum weight", Toast.LENGTH_SHORT).show()
            return
        }
        //load the image and set the var for the image state to true
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
        petImage.setImageBitmap(bitmap)
        imageRes = imageBitmap
        imageChanged = true
    }


    override fun onBackPressed() {
        showExitAlert()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                showExitAlert()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}