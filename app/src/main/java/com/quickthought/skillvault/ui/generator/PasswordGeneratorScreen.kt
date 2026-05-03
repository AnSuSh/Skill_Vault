package com.quickthought.skillvault.ui.generator

import android.content.ClipData
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipEntry
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.quickthought.skillvault.R
import com.quickthought.skillvault.util.PasswordGenerator
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(
    modifier: Modifier = Modifier
) {
    var length by remember { mutableFloatStateOf(16f) }
    var includeUpper by remember { mutableStateOf(true) }
    var includeLower by remember { mutableStateOf(true) }
    var includeDigits by remember { mutableStateOf(true) }
    var includeSymbols by remember { mutableStateOf(true) }

    var generatedPassword by remember { mutableStateOf("") }

    LaunchedEffect(
        length, includeUpper, includeLower, includeDigits, includeSymbols
    ) {
        generatedPassword = PasswordGenerator.generate(
            length.toInt(),
            includeUpper,
            includeLower,
            includeDigits,
            includeSymbols
        )
    }

    val strength = remember(generatedPassword) {
        PasswordGenerator.calculateStrength(generatedPassword)
    }

    val clipboardManager = LocalClipboard.current

    Scaffold(
        topBar = {
            TopAppBar(title = {
                Text(
                    stringResource(R.string.password_lab_title),
                    style = MaterialTheme.typography.headlineSmall
                )
            })
        },
        modifier = modifier
    ) { padding ->

        val coroutineScope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // 1. Result Display
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier.padding(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = generatedPassword,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = {
                        generatedPassword = PasswordGenerator.generate(
                            length.toInt(),
                            includeUpper,
                            includeLower,
                            includeDigits,
                            includeSymbols
                        )
                    }) {
                        Icon(Icons.Default.Refresh, "Regenerate")
                    }
                }
            }

            // 2. Strength Meter
            Spacer(Modifier.height(8.dp))

            val targetColor = when {
                strength < 0.3f -> Color.Red
                strength < 0.6f -> Color.Yellow
                strength < 0.9f -> Color.Green
                else -> Color.Blue
            }

            val animatedColor by androidx.compose.animation.animateColorAsState(
                targetValue = targetColor,
                // Using a longer duration (800ms) to make it "slower"
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 800),
                label = "StrengthColorAnimation"
            )

            val animatedStrength by androidx.compose.animation.core.animateFloatAsState(
                targetValue = strength,
                animationSpec = androidx.compose.animation.core.tween(durationMillis = 800),
                label = "StrengthAnimation"
            )

            LinearProgressIndicator(
                progress = { animatedStrength },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = animatedColor,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
            Spacer(Modifier.height(8.dp))

            val (timeValue, caption) = remember(generatedPassword) {
                PasswordGenerator.estimateCrackTime(generatedPassword)
            }

            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "Time to crack: $timeValue",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = caption,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(32.dp))

            // 3. Length Control
            Text("Length: ${length.toInt()}", style = MaterialTheme.typography.titleMedium)
            Slider(
                value = length,
                onValueChange = { length = it },
                valueRange = 8f..64f,
                steps = 56
            )

            Spacer(Modifier.height(16.dp))

            // 4. Complexity Toggles
            ComplexityToggle("Include Uppercase", includeUpper) { includeUpper = it }
            ComplexityToggle("Include Lowercase", includeLower) { includeLower = it }
            ComplexityToggle("Include Numbers", includeDigits) { includeDigits = it }
            ComplexityToggle("Include Symbols", includeSymbols) { includeSymbols = it }

            Spacer(Modifier.weight(1f))

            // 5. Action Button
            Button(
                onClick = {
                    coroutineScope.launch {
                        clipboardManager.setClipEntry(
                            ClipEntry(ClipData.newPlainText("", AnnotatedString(generatedPassword)))
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, null)
                Spacer(Modifier.width(8.dp))
                Text("Copy Secure Password")
            }
        }
    }
}

@Composable
fun ComplexityToggle(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label)
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}