/*
 * Copyright (C) 2023-2026 cyberknight777
 * Copyright (C) 2026 zenin1504
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
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import androidx.preference.PreferenceManager
import com.android.fastcharge.utils.FileUtils

class FastChargeTileService : TileService() {

    private lateinit var mConfig: FastChargeConfig

    private val mServiceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            updateUI()
        }
    }

    private fun updateUI() {
        val tile = qsTile ?: return
        val mode = mConfig.getCurrentValue(mConfig.fastChargePath)

        tile.state = if (mode == "2") Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.updateTile()
    }

    override fun onStartListening() {
        super.onStartListening()
        mConfig = FastChargeConfig.getInstance(this)
        updateUI()

        val filter = IntentFilter(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED)
        registerReceiver(mServiceStateReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    override fun onStopListening() {
        super.onStopListening()
        unregisterReceiver(mServiceStateReceiver)
    }

    override fun onClick() {
        super.onClick()

        val currentMode = mConfig.getCurrentValue(mConfig.fastChargePath)
        val nextMode = when (currentMode) {
            "0" -> "1"
            "1" -> "2"
            else -> "0"
        }

        FileUtils.writeLine(mConfig.fastChargePath, nextMode)
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit().putString(FastChargeConfig.FASTCHARGE_KEY, nextMode).apply()

        val intent = Intent(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED).apply {
            putExtra(FastChargeConfig.EXTRA_FAST_CHARGE_STATE, nextMode)
            flags = Intent.FLAG_RECEIVER_REGISTERED_ONLY
        }
        sendBroadcastAsUser(intent, UserHandle.CURRENT)

        updateUI()
    }
}
