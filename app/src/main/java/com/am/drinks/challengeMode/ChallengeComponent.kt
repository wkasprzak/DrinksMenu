package com.am.drinks.challengeMode

import android.text.InputType
import android.widget.EditText
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.am.drinks.ui.theme.DrinksTheme
import kotlinx.coroutines.delay
import androidx.compose.ui.viewinterop.AndroidView

// Composable that displays the challenge mode timer
@Composable
fun ChallengeComponent(drinkId: String, onDismiss: () -> Unit, backgroundColor: Color, viewModel: ChallengeViewModel = viewModel()) {
    val context = LocalContext.current

    val isRunning = viewModel.isRunning
    val mode = viewModel.mode
    val remainingTime = viewModel.remainingTime
    val elapsedSeconds = viewModel.elapsedSeconds
    val showSuccessDialog = viewModel.showSuccessDialog

    // User input for countdown (e.g., 60 seconds)
    var inputText by remember { mutableStateOf("") }
    var shouldDismiss by remember { mutableStateOf(false) }

    if (shouldDismiss) {
        LaunchedEffect(Unit) {
            delay(800)
            onDismiss()
        }
    }

    // Formats seconds into MM:SS
    fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    Dialog(onDismissRequest = {
        if (viewModel.shouldAskForPbConfirmation) {
            // Asks user if they completed the challenge in time
            viewModel.showSuccessDialog = true
        } else {
            viewModel.stopTimer()
            viewModel.resetTimer()
            inputText = ""
            onDismiss()
        }
    }) {
        Surface(modifier = Modifier.fillMaxWidth().wrapContentHeight(), tonalElevation = 8.dp, shape = MaterialTheme.shapes.large, color = MaterialTheme.colorScheme.background) {
            Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Challenge mode", fontSize = 22.sp, color = backgroundColor)
                Spacer(modifier = Modifier.height(12.dp))

                // Timer mode toggle (count up / count down)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    ModeButton("Count up", mode == "count_up", backgroundColor, !isRunning) {
                        viewModel.mode = "count_up"
                    }
                    ModeButton("Count down", mode == "count_down", backgroundColor, !isRunning) {
                        viewModel.mode = "count_down"
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Input field for countdown time (if selected)
                if (mode == "count_down" && !isRunning) {
                    NumericInputField(
                        value = inputText,
                        onValueChange = {
                            inputText = it
                            val parsed = it.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                viewModel.customInput = parsed
                                viewModel.remainingTime = parsed
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Timer display with animation
                val displayTime = if (mode == "count_up") elapsedSeconds else remainingTime
                AnimatedContent(targetState = displayTime, label = "TimerAnimation") { value ->
                    Text(
                        text = "Timer: ${formatTime(value)}",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                    IconButton(onClick = {
                        viewModel.stopTimer()
                        viewModel.resetTimer()
                        inputText = ""
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel", tint = backgroundColor)
                    }

                    if (!isRunning) {
                        IconButton(onClick = {
                            viewModel.startTimer()
                        }) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Start", tint = backgroundColor)
                        }
                    } else {
                        IconButton(onClick = {
                            viewModel.stopTimer()
                            if (mode == "count_up") {
                                viewModel.shouldAskForPbConfirmation = true
                            }
                        }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop", tint = backgroundColor)
                        }
                    }
                }
            }
        }
    }

    if (showSuccessDialog) {
        SuccessDialog(
            backgroundColor = backgroundColor,
            onConfirm = {
                viewModel.confirmAndSaveIfBest(context, drinkId)
                viewModel.resetTimer()
                onDismiss()
            },
            onDismiss = {
                viewModel.showSuccessDialog = false
                viewModel.resetTimer()
                onDismiss()
            }
        )
    }
}

@Composable
fun NumericInputField(value: String, onValueChange: (String) -> Unit, modifier: Modifier = Modifier) {
    AndroidView(
        factory = { context ->
            EditText(context).apply {
                inputType = InputType.TYPE_CLASS_NUMBER
                filters = arrayOf(android.text.InputFilter.LengthFilter(5))
                hint = "Enter seconds (max 5 digits)"
            }
        },
        update = {
            if (it.text.toString() != value) {
                it.setText(value)
                it.setSelection(it.text.length)
            }
            it.setOnEditorActionListener { v, _, _ ->
                onValueChange(v.text.toString())
                true
            }
            it.setOnFocusChangeListener { v, hasFocus ->
                if (!hasFocus) {
                    onValueChange((v as EditText).text.toString())
                }
            }
        },
        modifier = modifier
    )
}

// Dialog asking the user to confirm if they completed the challenge successfully
@Composable
private fun SuccessDialog(backgroundColor: Color, onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Yes, I made it! 🎉")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("No, not yet")
            }
        },
        title = {
            Text("Did you finish in time?", fontSize = 20.sp, color = backgroundColor)
        },
        text = {
            Text("The timer has ended. Did you manage to complete the challenge?")
        },
        containerColor = MaterialTheme.colorScheme.background,
        tonalElevation = 8.dp
    )
}

@Composable
private fun ModeButton(text: String, selected: Boolean, backgroundColor: Color, enabled: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(containerColor = if (selected) backgroundColor else Color.LightGray, disabledContainerColor = Color.Gray)) {
        Text(text, color = Color.White)
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewChallengeDialog() {
    DrinksTheme {
        ChallengeComponent(
            drinkId = "preview",
            onDismiss = {},
            backgroundColor = Color.Green
        )
    }
}
