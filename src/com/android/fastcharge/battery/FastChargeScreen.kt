/*
 * Copyright (C) 2015-2016 The CyanogenMod Project
 *               2020 YAAP
 *               2023-2026 cyberknight777
 *               2026 zenin1504
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.fastcharge.battery

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.UserHandle
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.OfflineBolt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.preference.PreferenceManager
import com.android.fastcharge.R
import com.android.fastcharge.utils.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FastChargeScreen() {
    val context = LocalContext.current
    val config = remember { FastChargeConfig.getInstance(context) }
    val isSupported = remember { FileUtils.fileExists(config.fastChargePath) }
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    
    var currentMode by remember { mutableStateOf(config.getCurrentValue(config.fastChargePath)) }
    var showDialog by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (intent.action == FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED) {
                    currentMode = intent.getStringExtra(FastChargeConfig.EXTRA_FAST_CHARGE_STATE) ?: currentMode
                }
            }
        }
        context.registerReceiver(receiver, IntentFilter(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED), Context.RECEIVER_EXPORTED)
        onDispose { context.unregisterReceiver(receiver) }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            LargeTopAppBar(
                title = { Text(stringResource(R.string.fast_charging_title)) },
                navigationIcon = {
                    IconButton(onClick = { (context as? FastChargeActivity)?.finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                StatusHeroCard(currentMode, isSupported)
            }

            item {
                Text(
                    text = "Configuration",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(start = 8.dp, bottom = 4.dp)
                )
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    ListItem(
                        modifier = Modifier.clickable(enabled = isSupported) { showDialog = true },
                        headlineContent = { Text(stringResource(R.string.fast_charging_title)) },
                        supportingContent = { 
                            Text(if (isSupported) getSummary(currentMode) 
                                 else stringResource(R.string.fast_charging_summary_not_supported)) 
                        },
                        leadingContent = { 
                            Icon(Icons.Default.OfflineBolt, contentDescription = null, tint = MaterialTheme.colorScheme.primary) 
                        }
                    )
                }
            }

            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = "Turbo mode allows the device to pull more current from the charger. High speeds may increase device temperature.",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.fast_charging_title)) },
            text = {
                val modes = arrayOf("0", "1", "2")
                val labels = stringArrayResource(R.array.fast_charging_entries)
                Column {
                    modes.forEachIndexed { index, mode ->
                        ListItem(
                            modifier = Modifier.clickable {
                                updateMode(context, config, mode)
                                currentMode = mode
                                showDialog = false
                            },
                            headlineContent = { Text(labels[index]) },
                            leadingContent = { RadioButton(selected = (currentMode == mode), onClick = null) }
                        )
                    }
                }
            },
            confirmButton = { TextButton(onClick = { showDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
fun StatusHeroCard(mode: String, isSupported: Boolean) {
    val containerColor by animateColorAsState(
        targetValue = when {
            !isSupported -> MaterialTheme.colorScheme.surfaceVariant
            mode == "2" -> MaterialTheme.colorScheme.primaryContainer
            mode == "1" -> MaterialTheme.colorScheme.secondaryContainer
            else -> MaterialTheme.colorScheme.tertiaryContainer
        }, label = "color"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(28.dp)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Current Status", style = MaterialTheme.typography.labelMedium)
                Text(
                    text = if (isSupported) getSummary(mode) else "Unsupported",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Icon(
                imageVector = Icons.Default.BatteryChargingFull,
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@Composable
private fun getSummary(mode: String): String {
    val labels = stringArrayResource(R.array.fast_charging_entries)
    return when (mode) {
        "0" -> labels[0]
        "1" -> labels[1]
        "2" -> labels[2]
        else -> labels[1]
    }
}

private fun updateMode(context: Context, config: FastChargeConfig, mode: String) {
    FileUtils.writeLine(config.fastChargePath, mode)
    PreferenceManager.getDefaultSharedPreferences(context).edit().putString(FastChargeConfig.FASTCHARGE_KEY, mode).apply()
    val intent = Intent(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED).apply {
        putExtra(FastChargeConfig.EXTRA_FAST_CHARGE_STATE, mode)
        flags = Intent.FLAG_RECEIVER_REGISTERED_ONLY
    }
    context.sendBroadcastAsUser(intent, UserHandle.CURRENT)
}
