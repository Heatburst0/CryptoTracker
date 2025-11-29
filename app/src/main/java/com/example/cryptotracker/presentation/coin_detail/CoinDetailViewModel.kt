package com.example.cryptotracker.presentation.coin_detail

import android.R
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.domain.model.Coin
import com.example.cryptotracker.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val repository: CoinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(){
    private val _state = MutableStateFlow(CoinDetailState())
    val state: StateFlow<CoinDetailState> = _state

    init{
        savedStateHandle.get<String>("coinId")?.let { coinId ->
            getCoin(coinId)
        }
    }
    private fun getCoin(coinId : String){
        viewModelScope.launch {
            repository.getCoinById(coinId).let { result ->
                when(result) {
                    is Resource.Success -> {
                        _state.value = CoinDetailState(coin = result.data)
                    }
                    is Resource.Error -> {
                        _state.value = _state.value.copy( // Keep old data if possible
                            error = result.message ?: "An unexpected error occurred",
                            isLoading = false
                        )
                    }
                    is Resource.Loading -> {
                        _state.value = _state.value.copy( // Keep old data visible
                            isLoading = true
                        )
                    }
                }
            }
        }
    }
}