package com.example.cryptotracker.common

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}