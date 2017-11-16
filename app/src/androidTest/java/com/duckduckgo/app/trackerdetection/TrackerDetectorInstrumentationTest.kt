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

package com.duckduckgo.app.trackerdetection


import android.support.test.InstrumentationRegistry
import android.support.test.runner.AndroidJUnit4
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.trackerdetection.TrackerDetectionClient.ClientName.EASYLIST
import com.duckduckgo.app.trackerdetection.TrackerDetectionClient.ClientName.EASYPRIVACY
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.BeforeClass
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TrackerDetectorInstrumentationTest {

    private val documentUrl = "http://example.com"

    companion object {

        private lateinit var testee: TrackerDetector

        @BeforeClass
        @JvmStatic
        fun beforeClass() {
            val appContext = InstrumentationRegistry.getTargetContext()

            val easylistData = appContext.resources.openRawResource(R.raw.easylist).use { it.readBytes() }
            var basicEasylistAdblock = AdBlockPlus(EASYLIST)
            basicEasylistAdblock.loadBasicData(easylistData)
            var easylistAdblock = AdBlockPlus(EASYLIST)
            easylistAdblock.loadProcessedData(basicEasylistAdblock.getProcessedData())

            val easyprivacyData = appContext.resources.openRawResource(R.raw.easyprivacy).use { it.readBytes() }
            var basicEasyprivacyAdblock = AdBlockPlus(EASYPRIVACY)
            basicEasyprivacyAdblock.loadBasicData(easyprivacyData)
            var easyprivacyAdblock = AdBlockPlus(EASYPRIVACY)
            easyprivacyAdblock.loadProcessedData(basicEasyprivacyAdblock.getProcessedData())

            testee = TrackerDetector()
            testee.addClient(easyprivacyAdblock)
            testee.addClient(easylistAdblock)
        }
    }

    @Test
    fun whenUrlIsInEasyListThenShouldBlockIsTrue() {
        val url = "http://imasdk.googleapis.com/js/sdkloader/ima3.js"
        assertTrue(testee.shouldBlock(url, documentUrl))
    }

    @Test
    fun whenUrlIsInEasyPrivacyListThenShouldBlockIsTrue() {
        val url = "http://cdn.tagcommander.com/1705/tc_catalog.css"
        assertTrue(testee.shouldBlock(url, documentUrl))
    }

    @Test
    fun whenUrlIsNotInAnyTrackerListsThenShouldBlockIsFalse() {
        val url = "https://duckduckgo.com/index.html"
        assertFalse(testee.shouldBlock(url, documentUrl))
    }

}