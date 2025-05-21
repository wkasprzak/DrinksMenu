package com.am.drinks.challengeMode

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.content.edit

class ChallengeViewModel : ViewModel() {

    var isRunning by mutableStateOf(false)
    var mode by mutableStateOf("count_up")
    var customInput by mutableIntStateOf(60)
    var remainingTime by mutableIntStateOf(60)
    var elapsedSeconds by mutableIntStateOf(0)
    var showSuccessDialog by mutableStateOf(false)
    var shouldAskForPbConfirmation by mutableStateOf(false)

    private var timerJob: Job? = null

    fun startTimer() {
        if (isRunning) return

        isRunning = true
        shouldAskForPbConfirmation = false

        if (mode == "count_down" && remainingTime <= 0) {
            remainingTime = customInput
        }

        timerJob = viewModelScope.launch {
            while (isRunning) {
                delay(1000)
                if (mode == "count_up") {
                    elapsedSeconds++
                } else if (mode == "count_down") {
                    if (remainingTime > 0) {
                        remainingTime--
                        if (remainingTime == 0) {
                            isRunning = false
                            showSuccessDialog = true
                        }
                    }
                }
            }
        }
    }

    fun stopTimer() {
        isRunning = false
        timerJob?.cancel()

        if (mode == "count_up") {
            shouldAskForPbConfirmation = true
        }
    }

    fun confirmAndSaveIfBest(context: Context, drinkId: String) {
        if (mode == "count_up" && elapsedSeconds > 0) {
            trySetPersonalBest(context, drinkId, elapsedSeconds)
        } else if (mode == "count_down" && customInput > 0) {
            trySetPersonalBest(context, drinkId, customInput)
        }
        shouldAskForPbConfirmation = false
    }

    fun resetTimer() {
        isRunning = false
        elapsedSeconds = 0
        remainingTime = customInput
        shouldAskForPbConfirmation = false
        showSuccessDialog = false
    }

    fun getPersonalBest(context: Context, drinkId: String): Int? {
        val prefs = context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE)
        return if (prefs.contains(drinkId)) prefs.getInt(drinkId, -1) else null
    }

    fun trySetPersonalBest(context: Context, drinkId: String, time: Int) {
        val previous = getPersonalBest(context, drinkId)
        if (previous == null || time < previous) {
            context.getSharedPreferences("challenge_prefs", Context.MODE_PRIVATE).edit {
                putInt(drinkId, time)
            }
        }
    }
}
