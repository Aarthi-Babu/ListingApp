package com.example.listingapp.di

import android.content.Context
import com.example.listingapp.BuildConfig
import com.example.listingapp.service.ListingRepository
import com.example.listingapp.service.ListingService
import com.example.listingapp.service.WeatherService
import com.example.listingapp.utils.ProgressDialog
import dagger.Module
import dagger.Provides
import dagger.assisted.AssistedFactory
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    private const val baseUrl = "https://randomuser.me/"
    private const val weatherUrl = "https://api.openweathermap.org/data/2.5/"

    @Singleton
    @Provides
    fun provideRetrofit(state: Int = 0): Retrofit {
        return Retrofit.Builder()
            .baseUrl(if (state == 0) baseUrl else weatherUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .client(provideOkHttpClient())
            .build()
    }

    @Singleton
    @Provides
    fun provideService(): ListingService {
        return provideRetrofit().create(ListingService::class.java)
    }

    @Singleton
    @Provides
    fun provideWeatherService(): WeatherService {
        return provideRetrofit(1).create(WeatherService::class.java)
    }

    @Singleton
    @Provides
    fun provideRepo(): ListingRepository {
        return ListingRepository(provideService(), provideWeatherService())
    }

    @Singleton
    @Provides
    fun provideLogger(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
            else HttpLoggingInterceptor.Level.NONE
        }
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().addInterceptor(provideLogger()).build()
    }

    @AssistedFactory
    interface ProgressDialogFactory {
        fun create(context: Context?): ProgressDialog
    }
}