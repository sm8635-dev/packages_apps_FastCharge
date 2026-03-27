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

import android.content.Context
import com.android.fastcharge.utils.FileUtils

class FastChargeConfig private constructor(context: Context) {

    val fastChargePath: String = context.resources.getString(com.android.fastcharge.R.string.config_FastChargePath)

    fun getCurrentValue(node: String): String {
        return FileUtils.readOneLine(node)?.trim() ?: "1"
    }

    companion object {
        private var instance: FastChargeConfig? = null

        const val FASTCHARGE_KEY = "fast_charging_mode"
        const val ACTION_FAST_CHARGE_SERVICE_CHANGED = "com.android.fastcharge.battery.FAST_CHARGE_SERVICE_CHANGED"
        const val EXTRA_FAST_CHARGE_STATE = "fastcharge_mode"

        fun getInstance(context: Context): FastChargeConfig {
            return instance ?: synchronized(this) {
                instance ?: FastChargeConfig(context.applicationContext).also { instance = it }
            }
        }
    }
}
