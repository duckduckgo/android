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

package com.duckduckgo.app.privacydashboard

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import android.support.test.InstrumentationRegistry
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.privacymonitor.HttpsStatus
import com.duckduckgo.app.privacymonitor.PrivacyMonitor
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.whenever
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock

class PrivacyDashboardViewModelTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @Mock
    private val viewStateObserver: Observer<PrivacyDashboardViewModel.ViewState> = mock()

    @Mock
    private val monitor: PrivacyMonitor = mock()

    private lateinit var testee: PrivacyDashboardViewModel

    @Before
    fun before() {
        testee = PrivacyDashboardViewModel(InstrumentationRegistry.getTargetContext())
        testee.viewState.observeForever(viewStateObserver)
        whenever(monitor.https).thenReturn(HttpsStatus.SECURE)
    }

    @After
    fun after() {
        testee.viewState.removeObserver(viewStateObserver)
    }

    @Test
    fun whenHttpsStatusIsSecureThenTextAndIconReflectSame() {
        whenever(monitor.https).thenReturn(HttpsStatus.SECURE)
        testee.updatePrivacyMonitor(monitor)
        assertEquals("Encrypted connection", testee.viewState.value?.httpsText)
        assertEquals(R.drawable.dashboard_https_good, testee.viewState.value?.httpsIcon)
    }

    @Test
    fun whenHttpsStatusIsMixedThenTextAndIconReflectsSame() {
        whenever(monitor.https).thenReturn(HttpsStatus.MIXED)
        testee.updatePrivacyMonitor(monitor)
        assertEquals("Mixed encryption connection", testee.viewState.value?.httpsText)
        assertEquals(R.drawable.dashboard_https_neutral, testee.viewState.value?.httpsIcon)
    }

    @Test
    fun whenHttpsStatusIsNoneThenTextAndIconReflectsSame() {
        whenever(monitor.https).thenReturn(HttpsStatus.NONE)
        testee.updatePrivacyMonitor(monitor)
        assertEquals("Unencrypted connection", testee.viewState.value?.httpsText)
        assertEquals(R.drawable.dashboard_https_bad, testee.viewState.value?.httpsIcon)
    }

    @Test
    fun whenNoTrackersNetworksThenTrackerNetworkTextShowsZero() {
        whenever(monitor.trackerNetworkCount).thenReturn(0)
        testee.updatePrivacyMonitor(monitor)
        assertEquals("0 Tracker Networks Blocked", testee.viewState.value?.trackerNetworksText)
    }

    @Test
    fun whenTenTrackersNetworksThenTrackerNetworkTextShowsTen() {
        whenever(monitor.trackerNetworkCount).thenReturn(10)
        testee.updatePrivacyMonitor(monitor)
        assertEquals("10 Tracker Networks Blocked", testee.viewState.value?.trackerNetworksText)
    }
}