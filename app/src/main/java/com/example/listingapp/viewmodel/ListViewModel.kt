package com.example.listingapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.listingapp.api.ListingRepository
import com.example.listingapp.database.DatabaseHelperImpl
import com.example.listingapp.database.User
import com.example.listingapp.model.ResponseModel
import com.example.listingapp.model.WeatherModel
import kotlinx.coroutines.launch

class ListViewModel : ViewModel() {
    val userDetailsResponse = MutableLiveData<ArrayList<User>>()
    val weatherModel: MutableLiveData<WeatherModel> by lazy { MutableLiveData<WeatherModel>() }
    val userDetailsFromDb = MutableLiveData<ArrayList<User>>()
    private val listingRepository: ListingRepository by lazy { ListingRepository() }
    fun getUserDetails(dbHelper: DatabaseHelperImpl) {
        var userResponse: ResponseModel?
        viewModelScope.launch {
            userResponse = listingRepository.getDetails()
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
//            dbHelper.nukeTable()
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

    fun <T> MutableLiveData<T>.notifyObserver() {
        this.value = this.value
    }

    fun fetchDataFromDb(dbHelper: DatabaseHelperImpl) {
        viewModelScope.launch {
            userDetailsFromDb.postValue(dbHelper.getUsers() as ArrayList<User>?)
        }
    }

    fun getWeatherDetails(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            val weatherResponse = listingRepository.getWeatherData(latitude, longitude)
            weatherModel.postValue(weatherResponse)
        }
    }

}