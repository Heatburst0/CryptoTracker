package com.example.cryptotracker.presentation.coin_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CoinListViewModel @Inject constructor(
    private val repository: CoinRepository
) : ViewModel() {

    private val _state = MutableStateFlow(CoinListState())
    val state: StateFlow<CoinListState> = _state

    init {
        // Start the polling loop
        startPolling()
    }

    private fun startPolling() {
        // launchIn(viewModelScope) ensures this dies when the user leaves the screen
        viewModelScope.launch {
            while(true) {
                // 1. Fetch data
                // We handle the Flow inside the loop
                repository.getCoins(fetchFromRemote = true).collect { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                coins = result.data ?: emptyList(),
                                isLoading = false,
                                error = "" // Clear any previous errors on success
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                error = result.message ?: "Unexpected error",
                                isLoading = false
                            )
                        }
                        is Resource.Loading -> {
                            // Only show the big loading spinner on the VERY FIRST load.
                            // We don't want the screen to flicker every 30 seconds.
                            if (_state.value.coins.isEmpty()) {
                                _state.value = _state.value.copy(isLoading = true)
                            }
                        }
                    }
                }

                // 2. Wait for 30 seconds before next fetch
                // This is "Cooperative Cancellation" friendly.
                delay(30 * 1000L)
            }
        }
    }

    fun onSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
    }
}