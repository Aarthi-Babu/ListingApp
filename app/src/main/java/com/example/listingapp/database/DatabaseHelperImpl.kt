package com.example.listingapp.database


class DatabaseHelperImpl(private val appDatabase: UserDatabase) : DataBaseHelper {

    override suspend fun getUsers(): List<User> = appDatabase.userDao().getUsers()

    override suspend fun insertAll(users: List<User>) = appDatabase.userDao().insertAll(users)

}