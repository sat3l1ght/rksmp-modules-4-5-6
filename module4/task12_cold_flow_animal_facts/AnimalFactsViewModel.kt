package com.example.module4.task12

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.random.Random

class AnimalFactsViewModel : ViewModel() {
    private val facts = listOf(
        "У осьминога три сердца и голубая кровь.",
        "Сердце синего кита весит около 180 кг.",
        "Выдры держатся за лапки во сне, чтобы не уплыть друг от друга.",
        "Колибри — единственная птица, способная летать задом наперёд.",
        "Отпечатки пальцев коалы почти неотличимы от человеческих.",
        "У жирафа столько же шейных позвонков, сколько у человека — семь.",
        "Дельфины дают друг другу уникальные имена-свисты.",
        "Слоны — единственные млекопитающие, которые не умеют прыгать.",
        "Пчела может взмахивать крыльями до 200 раз в секунду.",
        "Улитка способна проспать до трёх лет подряд.",
        "Фламинго рождаются серыми, а розовеют из-за пищи.",
        "У тигров полосатый не только мех, но и кожа.",
        "Морские звёзды не имеют мозга, но обладают нервной сетью.",
        "Совы не могут двигать глазами, зато поворачивают голову на 270°.",
        "Гепард разгоняется до 100 км/ч примерно за 3 секунды."
    )

    private val _currentFact = MutableStateFlow<String?>(null)
    val currentFact: StateFlow<String?> = _currentFact.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun getRandomFact(): Flow<String> = flow {
        delay(Random.nextLong(1500L, 3001L))
        emit(facts.random())
    }

    fun requestNewFact() {
        viewModelScope.launch {
            _isLoading.value = true
            getRandomFact().collect { fact ->
                _currentFact.value = fact
                _isLoading.value = false
            }
        }
    }
}
