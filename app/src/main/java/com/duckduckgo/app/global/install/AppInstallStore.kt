/*
 * Copyright (c) 2018 DuckDuckGo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.duckduckgo.app.global.install

import android.content.Context
import android.content.SharedPreferences
import android.support.annotation.VisibleForTesting
import androidx.core.content.edit
import java.util.concurrent.TimeUnit
import javax.inject.Inject


interface AppInstallStore {
    var installTimestamp: Long

    fun hasInstallTimestampRecorded(): Boolean
}

fun AppInstallStore.daysSinceInstallation(): Long {
    return TimeUnit.MILLISECONDS.toDays(System.currentTimeMillis() - installTimestamp) + 1
}

class AppInstallSharedPreferences @Inject constructor(private val context: Context) : AppInstallStore {
    override var installTimestamp: Long
        get() = preferences.getLong(KEY_TIMESTAMP_UTC, 0L)
        set(timestamp) = preferences.edit { putLong(KEY_TIMESTAMP_UTC, timestamp) }

    override fun hasInstallTimestampRecorded(): Boolean = preferences.contains(KEY_TIMESTAMP_UTC)

    private val preferences: SharedPreferences
        get() = context.getSharedPreferences(FILENAME, Context.MODE_PRIVATE)

    companion object {

        @VisibleForTesting
        const val FILENAME = "com.duckduckgo.app.install.settings"
        const val KEY_TIMESTAMP_UTC = "INSTALL_TIMESTAMP_UTC"
    }
}

