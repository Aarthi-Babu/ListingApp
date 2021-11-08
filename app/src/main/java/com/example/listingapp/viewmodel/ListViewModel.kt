package com.example.listingapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listingapp.api.ListingRepository
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.model.ResponseModel
import kotlinx.coroutines.launch

class ListViewModel : ViewModel() {
    val userDetailsResponse = MutableLiveData<ResponseModel>()
    val userDetailsFromDb = MutableLiveData<List<User>>()
    fun getUserDetails(dbHelper: DatabaseHelperImpl) {
        var userResponse: ResponseModel?
        viewModelScope.launch {
            userResponse = ListingRepository().getDetails()
            val users = mutableListOf<User>()
            val len = userResponse?.results?.size
            if (len != null)
                for (i in 0 until len) {
                    val user = userResponse?.results?.get(i)?.let {
                        User(
                            it.cell, it.name.first,
                            it.name.last,
                            it.picture.thumbnail
                        )
                    }
                    if (user != null) {
                        users.add(user)
                    }
                }
            dbHelper.insertAll(users)
            userDetailsResponse.postValue(userResponse)
        }
    }

    fun fetchDataFromDb(dbHelper: DatabaseHelperImpl) {
        viewModelScope.launch {
            userDetailsFromDb.postValue(dbHelper.getUsers())
        }
    }

}