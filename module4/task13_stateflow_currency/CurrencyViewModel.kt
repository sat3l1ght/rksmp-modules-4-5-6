package com.example.module4.task13

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt
import kotlin.random.Random

data class CurrencyState(
    val rate: Double = 90.50,
    val previousRate: Double = 90.50
) {
    val isUp: Boolean get() = rate >= previousRate
}

class CurrencyViewModel : ViewModel() {
    private val _state = MutableStateFlow(CurrencyState())
    val state: StateFlow<CurrencyState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(5000L)
                refreshNow()
            }
        }
    }

    fun refreshNow() {
        val current = _state.value.rate
        val next = ((90.5 + Random.nextDouble(-2.0, 2.0)) * 100).roundToInt() / 100.0
        _state.value = CurrencyState(rate = next, previousRate = current)
    }
}
