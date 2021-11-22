package com.example.listingapp.model

import com.google.gson.annotations.SerializedName


data class WeatherModel(
    val base: String?,
    val clouds: Clouds?,
    val cod: Int?,
    val coord: Coord?,
    val dt: Int?,
    val id: Int?,
    val main: Main?,
    val name: String?,
    val sys: Sys?,
    val timezone: Int?,
    val weather: List<Weather>?,
    val wind: Wind?
)

data class Clouds(
    val all: Int?
)

data class Coord(
    val lat: Int?,
    val lon: Int?
)

data class Main(
    @SerializedName("feels_like")
    val feelsLike: Double?,
    val humidity: Int?,
    val pressure: Int?,
    val temp: Double?,
    @SerializedName("temp_max")
    val tempMax: Double?,
    @SerializedName("temp_min")
    val tempMin: Double?
)

data class Sys(
    val country: String?,
    val id: Int?,
    val message: Double?,
    val sunrise: Int?,
    val sunset: Int?,
    val type: Int?
)

data class Weather(
    val description: String?,
    val icon: String?,
    val id: Int?,
    val main: String?
)

data class Wind(
    val deg: Double?,
    val speed: Double?
)