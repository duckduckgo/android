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
import com.duckduckgo.app.trackerdetection.TrackerDetectionClient.ClientName.EASYLIST
import com.duckduckgo.app.trackerdetection.TrackerDetectionClient.ClientName.EASYPRIVACY
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith


@RunWith(AndroidJUnit4::class)
class TrackerDetectorInstrumentationTest {

    companion object {
        private val documentUrl = "http://example.com"
    }

    private lateinit var testee: TrackerDetector

    @Before
    fun before() {
        var easylistAdblock = adblockClient(EASYLIST, "easylist_sample")
        var easyprivacyAdblock = adblockClient(EASYPRIVACY, "easyprivacy_sample")
        testee = TrackerDetector()
        testee.addClient(easyprivacyAdblock)
        testee.addClient(easylistAdblock)
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

    private fun adblockClient(name: TrackerDetectionClient.ClientName, dataFile: String): TrackerDetectionClient {
        val data = javaClass.classLoader.getResource(dataFile).readBytes()
        var initialAdBlock = AdBlockPlus(name)
        initialAdBlock.loadBasicData(data)
        var adblockWithProcessedData = AdBlockPlus(name)
        adblockWithProcessedData.loadProcessedData(initialAdBlock.getProcessedData())
        return adblockWithProcessedData
    }

}