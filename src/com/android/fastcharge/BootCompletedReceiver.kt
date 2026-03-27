/*
 * Copyright (C) 2015 The CyanogenMod Project
 *               2017-2019 The LineageOS Project
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

package com.android.fastcharge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.preference.PreferenceManager
import com.android.fastcharge.battery.FastChargeConfig
import com.android.fastcharge.utils.FileUtils

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED || action == Intent.ACTION_LOCKED_BOOT_COMPLETED) {
            if (DEBUG) Log.d(TAG, "Received boot completed intent: $action")

            val mConfig = FastChargeConfig.getInstance(context)
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)

            val fastchargeMode = sharedPrefs.getString(FastChargeConfig.FASTCHARGE_KEY, "1") ?: "1"
            
            if (FileUtils.fileExists(mConfig.fastChargePath)) {
                FileUtils.writeLine(mConfig.fastChargePath, fastchargeMode)
            }
        }
    }

    companion object {
        private const val DEBUG = false
        private const val TAG = "FastCharge"
    }
}
