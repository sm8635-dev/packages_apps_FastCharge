/*
 * Copyright (C) 2020 YAAP
 * Copyright (C) 2023-2024 cyberknight777
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

package com.android.fastcharge.battery;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.UserHandle;
import androidx.preference.ListPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;
import com.android.settingslib.widget.SettingsBasePreferenceFragment;

import com.android.fastcharge.R;
import com.android.fastcharge.utils.FileUtils;

public class FastChargeFragment extends SettingsBasePreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private ListPreference mFastChargePreference;
    private FastChargeConfig mConfig;

    private final BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(mConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED)) {
                if (mFastChargePreference == null) return;

                final String mode = intent.getStringExtra(mConfig.EXTRA_FAST_CHARGE_STATE);
                if (mode != null) {
                    mFastChargePreference.setValue(mode);
                }
            }
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.fastcharge_settings, rootKey);
        mConfig = FastChargeConfig.getInstance(getContext());
        mFastChargePreference = (ListPreference) findPreference(mConfig.FASTCHARGE_KEY);

        if (FileUtils.fileExists(mConfig.getFastChargePath())) {
            mFastChargePreference.setEnabled(true);
            mFastChargePreference.setOnPreferenceChangeListener(this);
        } else {
            mFastChargePreference.setSummary(R.string.fast_charging_summary_not_supported);
            mFastChargePreference.setEnabled(false);
        }

        updateUI();

        // Registering observers
        IntentFilter filter = new IntentFilter();
        filter.addAction(mConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED);
        getContext().registerReceiver(mServiceStateReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    private void updateUI() {
        if (mFastChargePreference != null) {
            mFastChargePreference.setValue(mConfig.getCurrentValue(mConfig.getFastChargePath()));
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (mConfig.FASTCHARGE_KEY.equals(preference.getKey())) {
            String mode = (String) newValue;
            Context mContext = getContext();

            SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);

            // Write the selected mode (0, 1, or 2)
            FileUtils.writeLine(mConfig.getFastChargePath(), mode);

            sharedPrefs.edit().putString(mConfig.FASTCHARGE_KEY, mode).commit();

            Intent intent = new Intent(mConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED);
            intent.putExtra(mConfig.EXTRA_FAST_CHARGE_STATE, mode);
            intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
            mContext.sendBroadcastAsUser(intent, UserHandle.CURRENT);
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getContext().unregisterReceiver(mServiceStateReceiver);
    }
}
