package com.example.listingapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.model.ResponseModel
import com.example.listingapp.model.WeatherModel
import com.example.listingapp.service.ListingRepository
import com.example.listingapp.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListViewModel @Inject constructor(private val repo: ListingRepository) : ViewModel() {
    val userDetailsResponse = MutableLiveData<ArrayList<User>>()
    val userDetailsFromDb = MutableLiveData<ArrayList<User>>()
    fun getUserDetails(dbHelper: DatabaseHelperImpl) {
        var userResponse: ResponseModel?
        viewModelScope.launch {
            userResponse = repo.getDetails()?.data
            val users = mutableListOf<User>()
            val len = userResponse?.results?.size
            if (len != null)
                for (i in 0 until len) {
                    val user = userResponse?.results?.get(i)?.let {
                        User(
                            it.cell, it.name.first,
                            it.name.last,
                            it.picture.large,
                            it.dob.age.toString(),
                            it.email,
                            it.phone,
                            it.gender,
                            it.location.city,
                            it.dob.date,
                            it.location.coordinates.latitude.toDouble(),
                            it.location.coordinates.longitude.toDouble()

                        )
                    }
                    if (user != null) {
                        users.add(user)
                    }
                }
            dbHelper.insertAll(users)
            if (userDetailsResponse.value?.isNotEmpty() == true) {
                userDetailsResponse.value?.addAll(users as ArrayList<User>)
                userDetailsResponse.notifyObserver()
            } else {
                userDetailsResponse.postValue(users as ArrayList<User>)
            }
            fetchDataFromDb(dbHelper)
        }
    }


    private fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    private fun fetchDataFromDb(dbHelper: DatabaseHelperImpl) {
        viewModelScope.launch {
            userDetailsFromDb.postValue(dbHelper.getUsers() as ArrayList<User>?)
        }
    }

    fun getWeatherDetails(
        latitude: Double,
        longitude: Double
    ): MutableLiveData<Resource<WeatherModel>> {
        val result = MutableLiveData<Resource<WeatherModel>>()
        result.value = Resource.loading()
        viewModelScope.launch(Dispatchers.IO) {
            result.postValue(repo.getWeatherData(latitude, longitude))
        }
        return result
    }

}