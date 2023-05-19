package com.example.petdaycare

import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AlertDialog
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class MainActivity : AppCompatActivity() {
    private lateinit var userEmailText: EditText
    private lateinit var userPasswordText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val signUpButton: TextView = findViewById(R.id.signUpButton)
        val logInButton: TextView = findViewById(R.id.logInButton)

        userEmailText = findViewById(R.id.userEmailText)
        userPasswordText = findViewById(R.id.userPasswordText)

        val instaImage: ImageView = findViewById(R.id.instaImage)
        val twitterImage: ImageView = findViewById(R.id.twitterImage)
        val fbImage: ImageView = findViewById(R.id.fbImage)


        signUpButton.setOnClickListener {
            Log.i("OnClick", "Clicked SignUp")
            showRegisterAlert()
        }
        logInButton.setOnClickListener {
            Log.i("OnClick", "Clicked Login")
            val email = userEmailText.text.toString()
            val password = userPasswordText.text.toString()
            var validEmail = true
            var validPass = true
            if (email.isEmpty()) {
                validEmail = false
            }
            if (password.isEmpty()) {
                validPass = false
            }

            if (validEmail && validPass) {
                logIn(email, password)
            } else if (validEmail && !validPass) {
                Toast.makeText(
                    applicationContext,
                    "The field password should not be empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (!validEmail && validPass) {
                Toast.makeText(
                    applicationContext,
                    "The field email should not be empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "The fields in the log in should not be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        instaImage.setOnClickListener {
            Log.i("OnClick", "Clicked Instagram")
            launchInsta()
        }
        twitterImage.setOnClickListener {
            Log.i("OnClick", "Clicked Twitter")
            launchTwitter()
        }
        fbImage.setOnClickListener {
            Log.i("OnClick", "Clicked Facebook")
            launchFb()
        }


        val test = FirebaseAnalytics.getInstance(this)
    }


    private fun launchInsta() {
        val uriForApp: Uri = Uri.parse("http://instagram.com/_u/centronelson")
        val forApp = Intent(Intent.ACTION_VIEW, uriForApp)

        val uriForBrowser: Uri = Uri.parse("http://instagram.com/centronelson")
        val forBrowser = Intent(Intent.ACTION_VIEW, uriForBrowser)

        forApp.component =
            ComponentName(
                "com.instagram.android",
                "com.instagram.android.activity.UrlHandlerActivity"
            )

        try {
            startActivity(forApp)
        } catch (e: ActivityNotFoundException) {
            startActivity(forBrowser)
        }
    }

    private fun launchTwitter() {
        val uriForApp: Uri = Uri.parse("http://twitter.com/_u/CentroNelson")
        val forApp = Intent(Intent.ACTION_VIEW, uriForApp)

        val uriForBrowser: Uri = Uri.parse("http://twitter.com/CentroNelson")
        val forBrowser = Intent(Intent.ACTION_VIEW, uriForBrowser)

        forApp.component =
            ComponentName(
                "com.twitter.android",
                "com.twitter.android.activity.UrlHandlerActivity"
            )

        try {
            startActivity(forApp)
        } catch (e: ActivityNotFoundException) {
            startActivity(forBrowser)
        }
    }

    private fun launchFb() {
        val uriForApp: Uri = Uri.parse("http://facebook.com/_u/CentroNelson")
        val forApp = Intent(Intent.ACTION_VIEW, uriForApp)

        val uriForBrowser: Uri = Uri.parse("http://facebook.com/CentroNelson")
        val forBrowser = Intent(Intent.ACTION_VIEW, uriForBrowser)

        forApp.component =
            ComponentName(
                "com.facebook.android",
                "com.facebook.android.activity.UrlHandlerActivity"
            )

        try {
            startActivity(forApp)
        } catch (e: ActivityNotFoundException) {
            startActivity(forBrowser)
        }
    }

    private fun validateEmail(email: String): Boolean {
        val regex = Regex("^\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,})+\$")

        return regex.matches(email)
    }

    private fun validatePassword(password: String): Boolean {
        val regex = Regex("^(?=\\w*\\d)(?=\\w*[A-Z])(?=\\w*[a-z])\\S{8,16}$")

        return regex.matches(password)
    }

    private fun logIn(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        if (!validateEmail(email)) {
            Toast.makeText(this, "The email is not valid.", Toast.LENGTH_SHORT).show()
        } else {
            if (!validatePassword(password)) {
                Toast.makeText(
                    this,
                    "The password does not meet the requirements.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val intent = Intent(this, PetListActivity::class.java)
                            startActivity(intent)
                            finish()
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthInvalidUserException) {
                                Toast.makeText(
                                    this,
                                    "The email address entered does not exist.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else if (exception is FirebaseAuthInvalidCredentialsException) {
                                Toast.makeText(
                                    this,
                                    "The password entered is incorrect.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    this,
                                    "Error logging in. Please check your email and password.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
            }
        }


    }


    private fun showRegisterAlert() {
        val builder = AlertDialog.Builder(this)
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.register_alert_frame, null)

        val userEmailText = dialogView.findViewById<EditText>(R.id.registEmailText)
        val registPasswordText = dialogView.findViewById<EditText>(R.id.registPasswordText)

        val negativeBtn = dialogView.findViewById<Button>(R.id.negativeBtn)
        val positiveBtn = dialogView.findViewById<Button>(R.id.positiveBtn)

        builder.setView(dialogView)

        val dialog = builder.create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()

        fun register(email: String, password: String) {
            val auth = FirebaseAuth.getInstance()
            auth.fetchSignInMethodsForEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val signInMethods = task.result?.signInMethods
                        if (signInMethods != null && signInMethods.isNotEmpty()) {
                            // El correo electrónico ya está registrado
                            Toast.makeText(
                                applicationContext,
                                "The email is already in use",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {
                            if (!validateEmail(email)) {
                                Toast.makeText(this, "The email is not valid.", Toast.LENGTH_SHORT)
                                    .show()
                            } else {
                                if (!validatePassword(password)) {
                                    Toast.makeText(
                                        this,
                                        "The password does not meet the requirements.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                } else {
                                    auth.createUserWithEmailAndPassword(email, password)
                                        .addOnCompleteListener { registerTask ->
                                            if (registerTask.isSuccessful) {
                                                dialog.dismiss()
                                                finish()
                                                // Registro exitoso
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Registered Successfully",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                startActivity(
                                                    Intent(
                                                        applicationContext,
                                                        PetListActivity::class.java
                                                    )
                                                )
                                            } else {
                                                // Error en el registro
                                                Toast.makeText(
                                                    applicationContext,
                                                    "Error in the register: ${registerTask.exception?.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                        }
                                }
                            }

                        }
                    } else {
                        // Error al verificar el correo electrónico
                        Toast.makeText(
                            applicationContext,
                            "Error trying to verify the email",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }

        positiveBtn.setOnClickListener {
            val email = userEmailText.text.toString()
            val password = registPasswordText.text.toString()
            var validEmail = true
            var validPass = true
            if (email.isEmpty()) {
                validEmail = false
            }
            if (password.isEmpty()) {
                validPass = false
            }

            if (validEmail && validPass) {
                register(email, password)
            } else if (!validEmail && validPass) {
                Toast.makeText(
                    applicationContext,
                    "The field email should not be empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else if (validEmail && !validPass) {
                Toast.makeText(
                    applicationContext,
                    "The field password should not be empty",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    applicationContext,
                    "The fields in the register should not be empty",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }

        negativeBtn.setOnClickListener() {
            dialog.dismiss()
        }
    }


}