package com.example.listingapp.utils


data class Resource<out T>(
    val status: Status,
    val data: T?,
    val throwable: Throwable?
) {
    companion object {
        fun <T> success(data: T?): Resource<T> {
            return Resource(
                Status.SUCCESS,
                data,
                null
            )
        }

        fun <T> error(msg: Throwable? = null): Resource<T> {
            return Resource(
                Status.ERROR,
                null,
                msg
            )
        }

        fun <T> loading(): Resource<T> {
            return Resource(
                Status.LOADING,
                null,
                null
            )
        }
    }

    enum class Status {
        SUCCESS,
        ERROR,
        LOADING
    }
}
