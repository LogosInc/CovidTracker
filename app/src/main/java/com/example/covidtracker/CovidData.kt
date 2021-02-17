package com.example.covidtracker

import java.util.*

data class CovidData(

    val dateChecked: Date,
    val positiveIncrease: Int,
    val negetiveIncrease: Int,
    val deathIncrease: Int,
    val state: String

)