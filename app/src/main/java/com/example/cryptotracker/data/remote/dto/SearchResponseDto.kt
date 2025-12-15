package com.example.cryptotracker.data.remote.dto

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class SearchResponseDto(
    val coins : List<SearchCoinDto>
)

@JsonClass(generateAdapter = true)
data class SearchCoinDto(
    val id: String,
    val name: String,
    val symbol: String,
    val thumb: String
)