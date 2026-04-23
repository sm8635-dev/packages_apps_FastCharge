/*
 * Copyright 2016 The CyanogenMod Project
 *           2023-2024 cyberknight777
 *           2026 zenin1504
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

package com.android.fastcharge.utils

import android.util.Log
import java.io.File
import java.io.IOException

object FileUtils {
    private const val TAG = "FileUtils"

    fun readOneLine(fileName: String): String? {
        return try {
            File(fileName).bufferedReader().use { it.readLine() }
        } catch (e: IOException) {
            Log.e(TAG, "Could not read from file $fileName", e)
            null
        }
    }

    fun writeLine(fileName: String, value: String): Boolean {
        return try {
            File(fileName).bufferedWriter().use { it.write(value) }
            true
        } catch (e: IOException) {
            Log.e(TAG, "Could not write to file $fileName", e)
            false
        }
    }

    fun fileExists(fileName: String): Boolean = File(fileName).exists()

    fun isFileReadable(fileName: String): Boolean {
        val file = File(fileName)
        return file.exists() && file.canRead()
    }

    fun isFileWritable(fileName: String): Boolean {
        val file = File(fileName)
        return file.exists() && file.canWrite()
    }

    fun delete(fileName: String): Boolean {
        return try {
            File(fileName).delete()
        } catch (e: SecurityException) {
            Log.w(TAG, "SecurityException trying to delete $fileName", e)
            false
        }
    }

    fun rename(srcPath: String, dstPath: String): Boolean {
        return try {
            File(srcPath).renameTo(File(dstPath))
        } catch (e: Exception) {
            Log.e(TAG, "Error trying to rename $srcPath to $dstPath", e)
            false
        }
    }
}
