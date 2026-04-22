/*
 * Copyright (C) 2023 Yet Another AOSP Project
 *               2023-2024 cyberknight777
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

import android.database.Cursor
import android.database.MatrixCursor
import android.provider.SearchIndexablesContract.*
import android.provider.SearchIndexablesProvider
import com.android.fastcharge.battery.FastChargeActivity

class SettingsSearchIndexablesProvider : SearchIndexablesProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun queryXmlResources(projection: Array<out String>?): Cursor {
        return MatrixCursor(INDEXABLES_XML_RES_COLUMNS)
    }

    override fun queryRawData(projection: Array<out String>?): Cursor {
        val cursor = MatrixCursor(INDEXABLES_RAW_COLUMNS)
        val ref = arrayOfNulls<Any>(INDEXABLES_RAW_COLUMNS.size)

        ref[COLUMN_INDEX_RAW_TITLE] = context?.getString(R.string.fast_charging_title)
        ref[COLUMN_INDEX_RAW_SUMMARY_ON] = context?.getString(R.string.fast_charge_summary)
        ref[COLUMN_INDEX_RAW_INTENT_ACTION] = "com.android.settings.action.IA_SETTINGS"
        ref[COLUMN_INDEX_RAW_INTENT_TARGET_PACKAGE] = "com.android.fastcharge"
        ref[COLUMN_INDEX_RAW_INTENT_TARGET_CLASS] = FastChargeActivity::class.java.name

        cursor.addRow(ref)
        return cursor
    }

    override fun queryNonIndexableKeys(projection: Array<out String>?): Cursor {
        return MatrixCursor(NON_INDEXABLES_KEYS_COLUMNS)
    }
}
