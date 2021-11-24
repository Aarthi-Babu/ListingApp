package com.example.listingapp.database


interface DataBaseHelper {
    suspend fun getUsers(): List<User>
    suspend fun nukeTable()
    suspend fun insertAll(users: List<User>)
}