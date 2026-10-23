/*
 * Copyright (C) 2026 Paranoid Android
 *
 * SPDX-License-Identifier: Apache-2.0
 */

package co.aospa.dolby.xiaomi

import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.AudioDeviceCallback
import android.media.AudioDeviceInfo
import android.media.AudioManager
import android.os.Handler
import android.os.Looper
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_BASS
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_DIALOGUE
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_DIALOGUE_AMOUNT
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_HP_VIRTUALIZER
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_IEQ
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_PROFILE
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_SPK_VIRTUALIZER
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_STEREO_WIDENING
import co.aospa.dolby.xiaomi.DolbyConstants.Companion.PREF_VOLUME
import co.aospa.dolby.xiaomi.geq.EqualizerActivity
import co.aospa.dolby.xiaomi.preference.DolbyPreferenceStore

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
internal fun DolbySettingsScreen(context: Context, controller: DolbyController, modifier: Modifier = Modifier) {
    var enabled by remember { mutableStateOf(controller.dsOn) }
    var profile by remember { mutableStateOf(controller.profile) }
    var speaker by remember { mutableStateOf(true) }
    var bass by remember { mutableStateOf(false) }
    var volume by remember { mutableStateOf(false) }
    var dialogue by remember { mutableStateOf(false) }
    var hpVirtualizer by remember { mutableStateOf(false) }
    var spkVirtualizer by remember { mutableStateOf(false) }
    var ieq by remember { mutableStateOf(0) }
    var dialogueAmount by remember { mutableStateOf(0f) }
    var stereoAmount by remember { mutableStateOf(0f) }
    var confirmReset by remember { mutableStateOf(false) }
    val store = remember(context) { DolbyPreferenceStore(context).apply { profile = controller.profile } }
    val audioManager = remember(context) { context.getSystemService(AudioManager::class.java)!! }

    fun refreshRoute() {
        audioManager.getDevicesForAttributes(MEDIA_ATTRIBUTES).firstOrNull()?.let {
            speaker = it.type == AudioDeviceInfo.TYPE_BUILTIN_SPEAKER
        }
    }

    DisposableEffect(audioManager) {
        val callback = object : AudioDeviceCallback() {
            override fun onAudioDevicesAdded(addedDevices: Array<AudioDeviceInfo>) = refreshRoute()
            override fun onAudioDevicesRemoved(removedDevices: Array<AudioDeviceInfo>) = refreshRoute()
        }
        audioManager.registerAudioDeviceCallback(callback, Handler(Looper.getMainLooper()))
        refreshRoute()
        onDispose { audioManager.unregisterAudioDeviceCallback(callback) }
    }

    LaunchedEffect(profile) {
        store.profile = profile
        bass = controller.getBassEnhancerEnabled(profile)
        volume = controller.getVolumeLevelerEnabled(profile)
        if (profile == 0) return@LaunchedEffect
        dialogue = controller.getDialogueEnhancerEnabled(profile)
        hpVirtualizer = controller.getHeadphoneVirtEnabled(profile)
        spkVirtualizer = controller.getSpeakerVirtEnabled(profile)
        ieq = controller.getIeqPreset(profile)
        dialogueAmount = controller.getDialogueEnhancerAmount(profile).toFloat()
        stereoAmount = controller.getStereoWideningAmount(profile).toFloat()
    }

    Column(
        modifier = modifier.fillMaxWidth().verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Card(
            shape = MaterialTheme.shapes.extraLarge,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        ) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 22.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        stringResource(R.string.dolby_title),
                        style = MaterialTheme.typography.headlineMediumEmphasized,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Text(
                        stringResource(R.string.dolby_expressive_summary),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                    Surface(
                        shape = MaterialTheme.shapes.large,
                        color = if (enabled) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                    ) {
                        Text(
                            stringResource(if (enabled) R.string.dolby_on else R.string.dolby_off),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLargeEmphasized,
                            color = if (enabled) MaterialTheme.colorScheme.onTertiaryContainer
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
                Switch(checked = enabled, onCheckedChange = { value ->
                    controller.setDsOnAndPersist(value)
                    enabled = value
                })
            }
        }

        if (enabled) {
            ProfileCard(profile = profile, onSelected = { value ->
                controller.profile = value
                PreferenceManager.getDefaultSharedPreferences(context).edit()
                    .putString(PREF_PROFILE, value.toString()).apply()
                profile = value
            })
            SectionTitle(stringResource(R.string.dolby_category_settings))
            SettingSwitch(stringResource(R.string.dolby_bass_enhancer), bass) { value ->
                controller.setBassEnhancerEnabled(value, profile)
                store.putBoolean(PREF_BASS, value)
                bass = value
            }
            Card(
                onClick = { context.startActivity(Intent(context, EqualizerActivity::class.java)) },
                shape = MaterialTheme.shapes.extraLarge,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Row(
                    Modifier.fillMaxWidth().padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(stringResource(R.string.dolby_preset), style = MaterialTheme.typography.titleMediumEmphasized)
                        Text(controller.getPresetName(), style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Text("›", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            if (profile == 0) {
                Text(stringResource(R.string.dolby_adv_settings_footer),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp))
            } else {
                SectionTitle(stringResource(R.string.dolby_category_adv_settings))
                ChoiceCard(
                    title = stringResource(R.string.dolby_ieq),
                    selected = ieq,
                    entries = stringArrayResource(R.array.dolby_ieq_entries),
                    values = stringArrayResource(R.array.dolby_ieq_values),
                    onSelected = { selected ->
                        val value = selected.toInt()
                        controller.setIeqPreset(value, profile)
                        store.putString(PREF_IEQ, value.toString())
                        ieq = value
                    },
                )
                SettingSwitch(
                    stringResource(if (speaker) R.string.dolby_spk_virtualizer else R.string.dolby_hp_virtualizer),
                    if (speaker) spkVirtualizer else hpVirtualizer,
                ) { value ->
                    if (speaker) {
                        controller.setSpeakerVirtEnabled(value, profile)
                        store.putBoolean(PREF_SPK_VIRTUALIZER, value)
                        spkVirtualizer = value
                    } else {
                        controller.setHeadphoneVirtEnabled(value, profile)
                        store.putBoolean(PREF_HP_VIRTUALIZER, value)
                        hpVirtualizer = value
                    }
                }
                SettingSwitch(stringResource(R.string.dolby_dialogue_enhancer), dialogue) { value ->
                    controller.setDialogueEnhancerEnabled(value, profile)
                    store.putBoolean(PREF_DIALOGUE, value)
                    dialogue = value
                }
                SettingSlider(
                    stringResource(R.string.dolby_strength), dialogueAmount,
                    context.resources.getInteger(R.integer.dialogue_enhancer_min).toFloat()..
                        context.resources.getInteger(R.integer.dialogue_enhancer_max).toFloat(), dialogue,
                    onFinished = { value ->
                        controller.setDialogueEnhancerAmount(value, profile)
                        store.putInt(PREF_DIALOGUE_AMOUNT, value)
                    },
                    onValueChange = { dialogueAmount = it },
                )
                if (context.resources.getBoolean(R.bool.dolby_stereo_widening_supported)) {
                    SettingSlider(
                        stringResource(R.string.dolby_strength), stereoAmount,
                        context.resources.getInteger(R.integer.stereo_widening_min).toFloat()..
                            context.resources.getInteger(R.integer.stereo_widening_max).toFloat(), true,
                        onFinished = { value ->
                            controller.setStereoWideningAmount(value, profile)
                            store.putInt(PREF_STEREO_WIDENING, value)
                        },
                        onValueChange = { stereoAmount = it },
                    )
                }
                if (context.resources.getBoolean(R.bool.dolby_volume_leveler_supported)) {
                    SettingSwitch(stringResource(R.string.dolby_volume_leveler), volume) { value ->
                        controller.setVolumeLevelerEnabled(value, profile)
                        store.putBoolean(PREF_VOLUME, value)
                        volume = value
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            OutlinedButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.dolby_reset_all))
            }
        }
    }

    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text(stringResource(R.string.dolby_reset_all)) },
            text = { Text(stringResource(R.string.dolby_reset_all_message)) },
            confirmButton = { TextButton(onClick = {
                controller.resetAllProfiles()
                PreferenceManager.getDefaultSharedPreferences(context).edit().putString(PREF_PROFILE, "0").apply()
                profile = 0
                confirmReset = false
            }) { Text(stringResource(android.R.string.yes)) } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) {
                Text(stringResource(android.R.string.no))
            } },
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun SectionTitle(title: String) {
    Text(title, style = MaterialTheme.typography.titleLargeEmphasized, modifier = Modifier.padding(start = 4.dp))
}

@Composable
private fun ProfileCard(profile: Int, onSelected: (Int) -> Unit) {
    val entries = stringArrayResource(R.array.dolby_profile_entries)
    val values = stringArrayResource(R.array.dolby_profile_values)
    val index = values.indexOf(profile.toString()).coerceAtLeast(0)
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.dolby_profile_title), style = MaterialTheme.typography.titleLargeEmphasized)
                Text(entries[index], style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                entries.forEachIndexed { i, entry ->
                    val value = values.getOrNull(i)?.toIntOrNull() ?: return@forEachIndexed
                    FilterChip(selected = value == profile, onClick = { onSelected(value) }, label = { Text(entry) })
                }
            }
        }
    }
}

@Composable
private fun SettingSwitch(title: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Card(shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun ChoiceCard(
    title: String, selected: Int, entries: Array<String>, values: Array<String>, onSelected: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val index = values.indexOf(selected.toString()).coerceAtLeast(0)
    Card(shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleLargeEmphasized)
            Spacer(Modifier.height(8.dp))
            Surface(onClick = { expanded = true }, shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainerHigh) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween) {
                    Text(entries.getOrElse(index) { "" }, style = MaterialTheme.typography.bodyLarge)
                    Text("⌄", color = MaterialTheme.colorScheme.primary)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                entries.forEachIndexed { i, entry ->
                    DropdownMenuItem(text = { Text(entry) }, onClick = {
                        expanded = false
                        values.getOrNull(i)?.let(onSelected)
                    })
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    title: String, value: Float, range: ClosedFloatingPointRange<Float>, enabled: Boolean,
    onFinished: (Int) -> Unit, onValueChange: (Float) -> Unit,
) {
    Card(shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow)) {
        Column(Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(title, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
                Text(value.toInt().toString(), style = MaterialTheme.typography.labelLarge)
            }
            Slider(value = value.coerceIn(range), onValueChange = onValueChange, valueRange = range,
                enabled = enabled, onValueChangeFinished = { onFinished(value.toInt()) })
        }
    }
}

private val MEDIA_ATTRIBUTES = AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_MEDIA).build()
