package com.example.metcast.data

data class WeatherModule(
    val city: String,
    val time: String,
    val tempCurrent: String,
    val condition: String,
    val icon: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String,
)
