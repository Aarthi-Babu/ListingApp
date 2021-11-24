package com.example.listingapp.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "first_name") val firstName: String?,
    @ColumnInfo(name = "last_name") val lastName: String?,
    @ColumnInfo(name = "thumbnail") val thumbnail: String?,
    @ColumnInfo(name = "age") val age: String?,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "phone") val phone: String?,
    @ColumnInfo(name = "gender") val gender: String?,
    @ColumnInfo(name = "city") val city: String?,
    @ColumnInfo(name = "dob") val dob: String?,
    @ColumnInfo(name = "lat") val latitude: Double?,
    @ColumnInfo(name = "long") val longitude: Double?
)