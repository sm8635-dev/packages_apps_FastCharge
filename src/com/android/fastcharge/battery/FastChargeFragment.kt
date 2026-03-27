/*
 * Copyright (C) 2020 YAAP
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
import android.os.Bundle
import android.os.UserHandle
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.android.settingslib.widget.SettingsBasePreferenceFragment
import com.android.fastcharge.R
import com.android.fastcharge.utils.FileUtils

class FastChargeFragment : SettingsBasePreferenceFragment(), Preference.OnPreferenceChangeListener {

    private var mFastChargePreference: ListPreference? = null
    private lateinit var mConfig: FastChargeConfig

    private val mServiceStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED) {
                intent.getStringExtra(FastChargeConfig.EXTRA_FAST_CHARGE_STATE)?.let {
                    mFastChargePreference?.value = it
                }
            }
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.fastcharge_settings, rootKey)
        mConfig = FastChargeConfig.getInstance(requireContext())
        mFastChargePreference = findPreference(FastChargeConfig.FASTCHARGE_KEY)

        mFastChargePreference?.let {
            if (FileUtils.fileExists(mConfig.fastChargePath)) {
                it.isEnabled = true
                it.onPreferenceChangeListener = this
            } else {
                it.setSummary(R.string.fast_charging_summary_not_supported)
                it.isEnabled = false
            }
        }

        updateUI()

        val filter = IntentFilter(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED)
        requireContext().registerReceiver(mServiceStateReceiver, filter, Context.RECEIVER_EXPORTED)
    }

    private fun updateUI() {
        mFastChargePreference?.value = mConfig.getCurrentValue(mConfig.fastChargePath)
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    override fun onPreferenceChange(preference: Preference, newValue: Any): Boolean {
        if (FastChargeConfig.FASTCHARGE_KEY == preference.key) {
            val mode = newValue as String
            val context = requireContext()

            FileUtils.writeLine(mConfig.fastChargePath, mode)
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putString(FastChargeConfig.FASTCHARGE_KEY, mode).apply()

            val intent = Intent(FastChargeConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED).apply {
                putExtra(FastChargeConfig.EXTRA_FAST_CHARGE_STATE, mode)
                flags = Intent.FLAG_RECEIVER_REGISTERED_ONLY
            }
            context.sendBroadcastAsUser(intent, UserHandle.CURRENT)
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        requireContext().unregisterReceiver(mServiceStateReceiver)
    }
}
