package com.example.cryptotracker.common

object Constants {
    const val BASE_URL = "https://api.coingecko.com/api/v3/"

    fun parseErrorMessage(error: Throwable): String {
        return when (error) {
            is java.net.UnknownHostException, is java.io.IOException -> "No Internet Connection"
            is retrofit2.HttpException -> "Server Error (${error.code()})"
            else -> "Something went wrong"
        }
    }
}