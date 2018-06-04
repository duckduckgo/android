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

package com.duckduckgo.app.httpsupgrade.di

import android.content.Context
import com.duckduckgo.app.httpsupgrade.HttpsUpgrader
import com.duckduckgo.app.httpsupgrade.HttpsUpgraderImpl
import com.duckduckgo.app.httpsupgrade.db.HttpsUpgradeDbWriteStatusSharedPreferences
import com.duckduckgo.app.httpsupgrade.db.HttpsUpgradeDbWriteStatusStore
import com.duckduckgo.app.httpsupgrade.db.HttpsUpgradeDomainDao
import dagger.Module
import dagger.Provides
import javax.inject.Singleton

@Module
class HttpsUpgraderModule {

    @Provides
    fun httpsUpgrader(dao: HttpsUpgradeDomainDao): HttpsUpgrader {
        return HttpsUpgraderImpl(dao)
    }

    @Provides
    @Singleton
    fun httpsUpgradeDbWriteStatusStore(context: Context): HttpsUpgradeDbWriteStatusStore {
        return HttpsUpgradeDbWriteStatusSharedPreferences(context)
    }
}