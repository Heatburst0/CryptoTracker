package com.example.cryptotracker.presentation.coin_list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.cryptotracker.common.Resource
import com.example.cryptotracker.domain.model.Coin
import com.example.cryptotracker.domain.repository.CoinRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
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
    private var searchJob: Job? = null
    val state: StateFlow<CoinListState> = _state

    val coinsPagingFlow: Flow<PagingData<Coin>> = repository.getCoinsPaged()
        .cachedIn(viewModelScope)



    private fun searchCoins() {
        // launchIn(viewModelScope) ensures this dies when the user leaves the screen
        val query = _state.value.searchQuery
        if (query.isBlank()) return

        // Use the new repository method
        repository.searchCoins(query).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _state.value = _state.value.copy(
                        coins = result.data ?: emptyList(),
                        isLoading = false,
                        error = ""
                    )
                }
                is Resource.Loading -> {
                    _state.value = _state.value.copy(
                        isLoading = result.isLoading ?: true, // Default to true if null
                        error = ""
                    )
                }
                is Resource.Error -> {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = result.message ?: "An unexpected error occurred"
                    )
                }
            }
        }.launchIn(viewModelScope)
    }

    fun onSearch(query: String) {
        _state.value = _state.value.copy(searchQuery = query)

        searchJob?.cancel()

        if (query.isNotEmpty()) {
            // Wait 500ms. If user types again, this block gets cancelled.
            // If they stop typing, searchCoins() finally runs.
            searchJob = viewModelScope.launch {
                delay(500L)
                searchCoins()
            }
        }
    }

    fun clearSearch() {
        _state.value = _state.value.copy(searchQuery = "", coins = emptyList())
    }
}