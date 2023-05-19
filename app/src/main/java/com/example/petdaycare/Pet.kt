package com.example.petdaycare

import java.io.Serializable

data class Pet(
    var id: String,
    var name: String,
    var type: String,
    var gender: Gender,
    var weight: Float,
    var image: Boolean


) : Serializable

