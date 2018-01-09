/*
 * Copyright (c) 2017 DuckDuckGo
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

package com.duckduckgo.app.di

import com.duckduckgo.app.privacymonitor.model.DatabaseNetworkLeaderboard
import com.duckduckgo.app.privacymonitor.model.NetworkLeaderboard
import com.duckduckgo.app.privacymonitor.store.PrivacySettingsSharedPreferences
import com.duckduckgo.app.privacymonitor.store.PrivacySettingsStore
import com.duckduckgo.app.privacymonitor.store.TermsOfServiceRawStore
import com.duckduckgo.app.privacymonitor.store.TermsOfServiceStore
import dagger.Binds
import dagger.Module


@Module
abstract class PrivacyModule {

    @Binds
    abstract fun bindPrivacySettingsStore(privacySettingsStore: PrivacySettingsSharedPreferences): PrivacySettingsStore

    @Binds
    abstract fun bindTermsOfServiceStore(termsOfServiceStore: TermsOfServiceRawStore): TermsOfServiceStore

    @Binds
    abstract fun bindNetworkLeaderboard(networkLeaderboard: DatabaseNetworkLeaderboard): NetworkLeaderboard

}