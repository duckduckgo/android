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

package com.duckduckgo.app.privacymonitor.ui

import com.duckduckgo.app.trackerdetection.model.TrackingEvent
import org.junit.Assert.assertEquals
import org.junit.Test

class NetworkTrackersAdapterTest {

    private val testee = NetworkTrackersAdapter()

    @Test
    fun whenInitializedThenCountIsZero() {
        assertEquals(0, testee.itemCount)
    }

    @Test
    fun whenDataContainsEntriesThenCountIncludesOneForEachHeaderAndRow() {
        testee.updateData(data())
        assertEquals(6, testee.itemCount)
    }

    @Test
    fun whenDataContainsEntriesThenCorrectElementsAreCreated() {
        testee.updateData(data())
        assertEquals(NetworkTrackersAdapter.HEADER, testee.getItemViewType(0))
        assertEquals(NetworkTrackersAdapter.ROW, testee.getItemViewType(1))
        assertEquals(NetworkTrackersAdapter.ROW, testee.getItemViewType(2))
        assertEquals(NetworkTrackersAdapter.HEADER, testee.getItemViewType(3))
        assertEquals(NetworkTrackersAdapter.ROW, testee.getItemViewType(4))
        assertEquals(NetworkTrackersAdapter.ROW, testee.getItemViewType(5))
    }

    private fun data(): HashMap<String, ArrayList<TrackingEvent>> {
        val trackingEvent = TrackingEvent("", "", null, true)
        val listA = arrayListOf(trackingEvent, trackingEvent)
        val listB = arrayListOf(trackingEvent, trackingEvent)
        val data = hashMapOf("Label A" to listA, "Label B" to listB)
        return data
    }
}