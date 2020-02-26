/*
 * Copyright (c) 2020 DuckDuckGo
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

package com.duckduckgo.app.systemsearch

import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import androidx.annotation.WorkerThread
import kotlin.text.RegexOption.IGNORE_CASE

data class DeviceApp(
    val shortName: String,
    val packageName: String,
    val launchIntent: Intent
)

interface DeviceAppLookup {
    @WorkerThread
    fun query(query: String): List<DeviceApp>
}

class InstalledDeviceAppLookup(private val appListProvider: DeviceAppListProvider) : DeviceAppLookup {

    private val apps by lazy { appListProvider.get() }

    @WorkerThread
    override fun query(query: String): List<DeviceApp> {
        if (query.isBlank()) return emptyList()
        val wordPrefixMatchingRegex = ".*\\b${query}.*".toRegex(IGNORE_CASE)

        return apps.filter {
            it.shortName.matches(wordPrefixMatchingRegex)
        }
    }
}

interface DeviceAppListProvider {
    @WorkerThread
    fun get(): List<DeviceApp>
}

class InstalledDeviceAppListProvider(private val packageManager: PackageManager) : DeviceAppListProvider {

    @WorkerThread
    override fun get(): List<DeviceApp> {

        val appsInfo = packageManager.getInstalledApplications(GET_META_DATA)

        return appsInfo.map {
            val packageName = it.packageName
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName) ?: return@map null
            val shortName = it.loadLabel(packageManager).toString()
            return@map DeviceApp(shortName, packageName, launchIntent)
        }.filterNotNull()
    }
}