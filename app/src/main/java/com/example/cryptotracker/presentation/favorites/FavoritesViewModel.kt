package com.example.cryptotracker.presentation.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.domain.repository.CoinRepository
import com.example.cryptotracker.presentation.coin_list.CoinListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: CoinRepository
) : ViewModel() {

    val state: StateFlow<CoinListState> = repository.getFavoriteCoins() // Returns Flow<List<Coin>>
        .map { coins ->
            CoinListState(
                coins = coins,
                isLoading = false // DB data is always "loaded" instantly
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CoinListState(isLoading = true) // Only initial state is loading
        )
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    init {
        refreshFavorites()
    }



    fun refreshFavorites() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val result = repository.refreshFavorites() // API -> DB
            if(result is Resource.Error){
                println("Sync Error: ${result.message}")
            }
            _isRefreshing.value = false
        }
    }
}