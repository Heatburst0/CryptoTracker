package com.example.cryptotracker.presentation.coin_detail

import com.example.cryptotracker.domain.model.Coin

data class CoinDetailState(
    val isLoading: Boolean = false,
    val coin: Coin? = null,
    val error: String = ""
)