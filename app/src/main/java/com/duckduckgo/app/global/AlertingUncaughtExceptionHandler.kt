/*
 * Copyright (c) 2019 DuckDuckGo
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

package com.duckduckgo.app.global

import com.duckduckgo.app.global.exception.RecordedThrowable
import com.duckduckgo.app.global.exception.UncaughtExceptionRepository
import com.duckduckgo.app.global.exception.UncaughtExceptionSource
import com.duckduckgo.app.statistics.store.OfflinePixelDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import timber.log.Timber

class AlertingUncaughtExceptionHandler(
    private val originalHandler: Thread.UncaughtExceptionHandler,
    private val offlinePixelDataStore: OfflinePixelDataStore,
    private val uncaughtExceptionRepository: UncaughtExceptionRepository
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(t: Thread?, originalException: Throwable?) {
        GlobalScope.launch(Dispatchers.IO) {

            offlinePixelDataStore.applicationCrashCount += 1

            val exceptionToThrow = if (originalException is RecordedThrowable) {
                Timber.i("This exception was already recorded to the DB")
                originalException.cause
            } else {
                uncaughtExceptionRepository.recordUncaughtException(originalException, UncaughtExceptionSource.GLOBAL)
            }

            originalHandler.uncaughtException(t, exceptionToThrow)
        }
    }
}