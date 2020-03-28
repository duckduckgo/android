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

package com.duckduckgo.app.onboarding.ui

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.duckduckgo.app.CoroutineTestRule
import com.duckduckgo.app.onboarding.store.AppStage
import com.duckduckgo.app.onboarding.store.UserStageStore
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Rule
import org.junit.Test


@Suppress("EXPERIMENTAL_API_USAGE")
class OnboardingViewModelTest {

    @get:Rule
    @Suppress("unused")
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private var userStageStore: UserStageStore = mock()

    private val pageLayout: OnboardingPageManager = mock()

    private val testee: OnboardingViewModel by lazy {
        OnboardingViewModel(userStageStore, pageLayout)
    }

    @Test
    fun whenOnboardingDoneThenCompleteStage() = runBlockingTest {
        testee.onOnboardingDone()
        verify(userStageStore).stageCompleted(AppStage.NEW)
    }
}