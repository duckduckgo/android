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

package com.duckduckgo.app.analyticsSurrogates

import android.support.annotation.WorkerThread
import com.duckduckgo.app.analyticsSurrogates.store.AnalyticsSurrogatesDataStore
import timber.log.Timber
import java.io.ByteArrayInputStream
import javax.inject.Inject

@WorkerThread
class AnalyticsSurrogatesLoader @Inject constructor(
    private val analyticsSurrogates: AnalyticsSurrogates,
    private val surrogatesDataStore: AnalyticsSurrogatesDataStore
) {

    fun loadData() {
        if (surrogatesDataStore.hasData()) {
            val bytes = surrogatesDataStore.loadData()
            analyticsSurrogates.loadSurrogates(convertBytes(bytes))
        }
    }

    fun convertBytes(bytes: ByteArray): List<SurrogateResponse> {
        val surrogates = mutableListOf<SurrogateResponse>()

        val reader = ByteArrayInputStream(bytes).bufferedReader()
        val existingLines = reader.readLines().toMutableList()

        if(existingLines.isNotEmpty() && existingLines.last().isNotBlank()) {
            existingLines.add("")
        }

        var nextLineIsNewRule = true

        var ruleName = ""
        var mimeType = ""
        val functionBuilder = StringBuilder()

        existingLines.forEach {

            if (it.startsWith("#")) {
                return@forEach
            }

            if (nextLineIsNewRule) {

                with(it.split(" ")) {
                    ruleName = this[0]
                    mimeType = this[1]
                }
                Timber.d("Found new surrogate rule: %s - %s", ruleName, mimeType)
                nextLineIsNewRule = false
                return@forEach
            }

            if (it.isBlank()) {
                surrogates.add(
                    SurrogateResponse(
                        name = ruleName,
                        mimeType = mimeType,
                        jsFunction = functionBuilder.toString()
                    )
                )

                functionBuilder.setLength(0)

                nextLineIsNewRule = true
                return@forEach
            }

            functionBuilder.append(it)
            functionBuilder.append("\n")
        }

        Timber.d("Processed %d surrogates", surrogates.size)
        return surrogates
    }
}