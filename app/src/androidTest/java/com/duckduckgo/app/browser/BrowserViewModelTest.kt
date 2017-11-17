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

package com.duckduckgo.app.browser

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import android.content.Context
import android.net.Uri
import com.duckduckgo.app.browser.omnibar.OmnibarEntryConverter
import com.duckduckgo.app.trackerdetection.TrackerDetectionClient
import com.duckduckgo.app.trackerdetection.TrackerDetectionClient.ClientName
import com.duckduckgo.app.trackerdetection.TrackerDetector
import com.duckduckgo.app.trackerdetection.api.TrackerListService
import com.duckduckgo.app.trackerdetection.store.TrackerDataProvider
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.whenever
import mock
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers
import org.mockito.Mock
import org.mockito.Mockito.never
import org.mockito.Mockito.verify

class BrowserViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private val observer: Observer<String> = mock()

    @Mock
    private val mockContext: Context = mock()

    @Mock
    private val mockTrackerService: TrackerListService = mock()


    private val testOmnibarConverter: OmnibarEntryConverter = object : OmnibarEntryConverter {
        override fun convertUri(input: String): String = "duckduckgo.com"
        override fun isWebUrl(inputQuery: String): Boolean = true
        override fun convertQueryToUri(inputQuery: String): Uri = Uri.parse("duckduckgo.com")
    }

    private lateinit var testee: BrowserViewModel

    @Before
    fun before() {
        testee = BrowserViewModel(testOmnibarConverter, TrackerDataProvider(mockContext), testTrackerDetector(), mockTrackerService)
    }

    @Test
    fun whenEmptyInputQueryThenNoQueryMadeAvailableToActivity() {
        testee.query.observeForever(observer)
        testee.onQueryEntered("")
        verify(observer, never()).onChanged(ArgumentMatchers.anyString())
    }

    @Test
    fun whenBlankInputQueryThenNoQueryMadeAvailableToActivity() {
        testee.query.observeForever(observer)
        testee.onQueryEntered("     ")
        verify(observer, never()).onChanged(ArgumentMatchers.anyString())
    }

    @Test
    fun whenNonEmptyInputThenQueryMadeAvailableToActivity() {
        testee.query.observeForever(observer)
        testee.onQueryEntered("foo")
        verify(observer).onChanged(ArgumentMatchers.anyString())
    }

    private fun testTrackerDetector(): TrackerDetector {
        val trackerDetector = TrackerDetector()
        trackerDetector.addClient(clientMock(ClientName.EASYLIST))
        trackerDetector.addClient(clientMock(ClientName.EASYPRIVACY))
        return trackerDetector
    }

    private fun clientMock(name: ClientName): TrackerDetectionClient {
        val client: TrackerDetectionClient = mock()
        whenever(client.name).thenReturn(name)
        whenever(client.matches(ArgumentMatchers.anyString(), ArgumentMatchers.anyString(), any())).thenReturn(false)
        return client
    }
}
