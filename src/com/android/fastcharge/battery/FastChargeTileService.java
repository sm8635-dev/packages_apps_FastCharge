/*
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
import android.os.UserHandle;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.preference.PreferenceManager;

import com.android.fastcharge.R;
import com.android.fastcharge.utils.FileUtils;

public class FastChargeTileService extends TileService {

    private FastChargeConfig mConfig;

    private final BroadcastReceiver mServiceStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI();
        }
    };

    private void updateUI() {
        final Tile tile = getQsTile();
        String mode = mConfig.getCurrentValue(mConfig.getFastChargePath());

        // State logic: 0 = Slow (Inactive), 1 = Normal (Inactive), 2 = Fast (Active)
        if (mode.equals("2")) {
            tile.setState(Tile.STATE_ACTIVE);
        } else {
            tile.setState(Tile.STATE_INACTIVE);
        }

        tile.updateTile();
    }

    @Override
    public void onStartListening() {
        super.onStartListening();
        mConfig = FastChargeConfig.getInstance(this);

        updateUI();

        IntentFilter filter = new IntentFilter(mConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED);
        registerReceiver(mServiceStateReceiver, filter, Context.RECEIVER_EXPORTED);
    }

    @Override
    public void onStopListening() {
        super.onStopListening();
        unregisterReceiver(mServiceStateReceiver);
    }

    @Override
    public void onClick() {
        super.onClick();

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String currentMode = mConfig.getCurrentValue(mConfig.getFastChargePath());
        String nextMode;

        // Cycle through 0 -> 1 -> 2 -> 0
        switch (currentMode) {
            case "0":
                nextMode = "1";
                break;
            case "1":
                nextMode = "2";
                break;
            case "2":
            default:
                nextMode = "0";
                break;
        }

        FileUtils.writeLine(mConfig.getFastChargePath(), nextMode);
        sharedPrefs.edit().putString(mConfig.FASTCHARGE_KEY, nextMode).commit();

        Intent intent = new Intent(mConfig.ACTION_FAST_CHARGE_SERVICE_CHANGED);
        intent.putExtra(mConfig.EXTRA_FAST_CHARGE_STATE, nextMode);
        intent.setFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);
        this.sendBroadcastAsUser(intent, UserHandle.CURRENT);

        updateUI();
    }
}
