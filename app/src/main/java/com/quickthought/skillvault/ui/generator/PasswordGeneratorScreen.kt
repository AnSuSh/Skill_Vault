package com.quickthought.skillvault.ui.generator

import android.content.ClipData
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.quickthought.skillvault.R
import com.quickthought.skillvault.ui.theme.SkillVaultTheme
import com.quickthought.skillvault.util.PassphraseGenerator
import com.quickthought.skillvault.util.PasswordGenerator
import kotlinx.coroutines.launch

enum class GeneratorMode {
    Password, Passphrase
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(
    modifier: Modifier = Modifier,
    initialMode: GeneratorMode = GeneratorMode.Password
) {
    var mode by remember { mutableStateOf(initialMode) }

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

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            SingleChoiceSegmentedButtonRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
            ) {
                SegmentedButton(
                    selected = mode == GeneratorMode.Password,
                    onClick = { mode = GeneratorMode.Password },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Password")
                }
                SegmentedButton(
                    selected = mode == GeneratorMode.Passphrase,
                    onClick = { mode = GeneratorMode.Passphrase },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Passphrase")
                }
            }

            AnimatedContent(
                targetState = mode,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width } + fadeOut()
                    } else {
                        slideInHorizontally { width -> -width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }.using(SizeTransform(clip = false))
                },
                label = "GeneratorModeTransition",
                modifier = Modifier.fillMaxSize()
            ) { targetMode ->
                when (targetMode) {
                    GeneratorMode.Password -> {
                        PasswordGeneratorContent()
                    }

                    GeneratorMode.Passphrase -> {
                        PassphraseGeneratorContent()
                    }
                }
            }
        }
    }
}

@Composable
fun PasswordGeneratorContent() {
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
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Result Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .heightIn(min = 128.dp),
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

        val animatedColor by animateColorAsState(
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
                .height(16.dp),
            color = animatedColor,
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        Spacer(Modifier.height(8.dp))

        val (timeValue, caption) = remember(generatedPassword) {
            PasswordGenerator.estimateCrackTime(generatedPassword)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
                .animateContentSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Time to crack: $timeValue",
                style = MaterialTheme.typography.titleMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = caption,
                style = MaterialTheme.typography.bodySmallEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(32.dp))

        // 3. Length Control
        Text(
            "Length: ${length.toInt()}",
            style = MaterialTheme.typography.titleMediumEmphasized
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = length,
            onValueChange = { length = it },
            valueRange = 8f..64f,
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    thumbSize = DpSize(8.dp, 52.dp)
                )
            },
            track = {
                SliderDefaults.Track(
                    sliderState = it,
                    modifier = Modifier.height(40.dp)
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        // 4. Complexity Toggles
        ComplexityToggle("Include Uppercase", includeUpper) { includeUpper = it }
        ComplexityToggle("Include Lowercase", includeLower) { includeLower = it }
        ComplexityToggle("Include Numbers", includeDigits) { includeDigits = it }
        ComplexityToggle("Include Symbols", includeSymbols) { includeSymbols = it }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(16.dp))
        // 5. Action Button
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = {
                    generatedPassword = PasswordGenerator.generate(
                        length.toInt(),
                        includeUpper,
                        includeLower,
                        includeDigits,
                        includeSymbols
                    )
                }, modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, "Regenerate")
                Spacer(Modifier.width(8.dp))
                Text("Regenerate")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        clipboardManager?.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    "",
                                    AnnotatedString(generatedPassword)
                                )
                            )
                        )
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, "Copy the Generated password")
                Spacer(Modifier.width(8.dp))
                Text("Copy")
            }
        }
    }
}

@Composable
fun PassphraseGeneratorContent() {
    val context = LocalContext.current
    val generator = remember { PassphraseGenerator(context) }

    var wordCount by remember { mutableFloatStateOf(5f) }
    var separator by remember { mutableStateOf("-") }
    var capitalize by remember { mutableStateOf(false) }
    var includeNumber by remember { mutableStateOf(false) }

    var generatedPassphrase by remember { mutableStateOf("") }

    LaunchedEffect(wordCount, separator, capitalize, includeNumber) {
        generatedPassphrase = generator.generatePassphrase(
            wordCount = wordCount.toInt(),
            separator = separator,
            capitalize = capitalize,
            includeNumber = includeNumber
        )
    }

    val clipboardManager = LocalClipboard.current
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // 1. Result Display
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .heightIn(min = 128.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = generatedPassphrase,
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // 2. Word Count Control
        Text(
            "Words: ${wordCount.toInt()}",
            style = MaterialTheme.typography.titleMediumEmphasized
        )
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = wordCount,
            onValueChange = { wordCount = it },
            valueRange = 3f..10f,
            steps = 6, // 3, 4, 5, 6, 7, 8, 9, 10
            thumb = {
                SliderDefaults.Thumb(
                    interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                    thumbSize = DpSize(8.dp, 52.dp)
                )
            },
            track = {
                SliderDefaults.Track(
                    sliderState = it,
                    modifier = Modifier.height(40.dp)
                )
            }
        )

        Spacer(Modifier.height(16.dp))

        // 3. Separator Option
        Text(
            "Separator",
            style = MaterialTheme.typography.titleMediumEmphasized,
            modifier = Modifier.align(Alignment.Start)
        )
        Spacer(modifier = Modifier.height(8.dp))
        SingleChoiceSegmentedButtonRow(
            modifier = Modifier.fillMaxWidth()
        ) {
            val separators = listOf("-", " ", "_", ".")
            separators.forEachIndexed { index, s ->
                SegmentedButton(
                    selected = separator == s,
                    onClick = { separator = s },
                    shape = SegmentedButtonDefaults.itemShape(
                        index = index,
                        count = separators.size
                    )
                ) {
                    Text(if (s == " ") "Space" else s)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 4. Complexity Toggles
        ComplexityToggle("Capitalize", capitalize) { capitalize = it }
        ComplexityToggle("Include Number", includeNumber) { includeNumber = it }

        Spacer(Modifier.weight(1f))
        Spacer(Modifier.height(32.dp))

        // 5. Action Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Button(
                onClick = {
                    generatedPassphrase = generator.generatePassphrase(
                        wordCount = wordCount.toInt(),
                        separator = separator,
                        capitalize = capitalize,
                        includeNumber = includeNumber
                    )
                },
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Refresh, "Regenerate")
                Spacer(Modifier.width(8.dp))
                Text("Regenerate")
            }
            Button(
                onClick = {
                    coroutineScope.launch {
                        clipboardManager?.setClipEntry(
                            ClipEntry(
                                ClipData.newPlainText(
                                    "",
                                    AnnotatedString(generatedPassphrase)
                                )
                            )
                        )
                    }
                },
                modifier = Modifier
                    .height(56.dp)
                    .weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.ContentCopy, "Copy the Generated passphrase")
                Spacer(Modifier.width(8.dp))
                Text("Copy")
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
        Text(label, style = MaterialTheme.typography.titleMediumEmphasized)
        Switch(checked = checked, onCheckedChange = onCheckedChange, thumbContent = {
            if (checked) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = "Checked Icon",
                    modifier = Modifier.size(SwitchDefaults.IconSize)
                )
            }
        })
    }
}

@Preview(showBackground = true)
@Composable
fun PasswordGeneratorScreenPreview() {
    SkillVaultTheme {
        PasswordGeneratorScreen(initialMode = GeneratorMode.Passphrase)
    }
}