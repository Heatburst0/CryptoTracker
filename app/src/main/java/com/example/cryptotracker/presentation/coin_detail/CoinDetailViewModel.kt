package com.example.cryptotracker.presentation.coin_detail

import android.R
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.common.UiEvent
import com.example.cryptotracker.domain.model.Coin
import com.example.cryptotracker.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinDetailViewModel @Inject constructor(
    private val repository: CoinRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel(){
    private val _state = MutableStateFlow(CoinDetailState())
    private val _uiEvent = Channel<UiEvent>()
    val uiEvent = _uiEvent.receiveAsFlow()
    val state: StateFlow<CoinDetailState> = _state

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing : StateFlow<Boolean> = _isRefreshing

    private val coinId: String? = savedStateHandle.get<String>("coinId")


    init{
        getCoin()
        refreshCoin()
    }
    private fun getCoin(){
        coinId?.let { id->
            viewModelScope.launch {
                repository.getCoinById(id).let { result ->
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

    fun onFavoriteClick(coinId: String, isFavorite: Boolean){
        viewModelScope.launch {
            repository.toggleFavoriteCoin(coinId, isFavorite)
            _state.value = _state.value.copy(
                coin = _state.value.coin?.copy(isFavorite = isFavorite)
            )
            val message = if (isFavorite) "Added to Watchlist" else "Removed from Watchlist"
            _uiEvent.send(UiEvent.ShowSnackbar(message))
        }
    }

    fun refreshCoin(){
        coinId?.let { id ->
            viewModelScope.launch {
                _isRefreshing.value=true
                val result = repository.refreshCoin(id)
                if(result is Resource.Success) {
                    getCoin()
                    Log.e("Refresh","Refresh is done")
                } else {
                    // Optional: Show a "Toast" or error message for refresh failure
                    Log.e("Refresh","Refresh failure ${result.message}")
                }

                _isRefreshing.value = false
            }
        }
    }
}