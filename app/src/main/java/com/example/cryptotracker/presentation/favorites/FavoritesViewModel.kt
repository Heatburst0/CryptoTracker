package com.example.cryptotracker.presentation.favorites

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.domain.repository.CoinRepository
import com.example.cryptotracker.presentation.coin_list.CoinListState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val repository: CoinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    val state : StateFlow<CoinListState> = _state

    init {
        getFavorites()
    }

    private fun getFavorites(){
        repository.getFavoriteCoins().onEach{ result->
            when(result){
                is Resource.Success ->{
                    _state.value = _state.value.copy(
                        coins = result.data ?: emptyList(),
                        isLoading = false,
                        error = ""
                    )
                }
                is Resource.Loading ->{
                    _state.value = _state.value.copy(
                        isLoading = true)
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        error = result.message ?: "Unexpected error",
                        isLoading = false
                    )
                }
            }
        }.launchIn(viewModelScope)
    }
}