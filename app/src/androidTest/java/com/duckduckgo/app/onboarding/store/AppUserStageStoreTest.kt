/*
 * Copyright (c) 2020 DuckDuckGo
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

package com.duckduckgo.app.onboarding.store

import com.duckduckgo.app.CoroutineTestRule
import com.duckduckgo.app.browser.addtohome.AddToHomeCapabilityDetector
import com.duckduckgo.app.runBlocking
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AppUserStageStoreTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    private val userStageDao = mock<UserStageDao>()
    private val mockAddToHomeCapabilityDetector = mock<AddToHomeCapabilityDetector>()

    private val testee = AppUserStageStore(userStageDao, coroutineRule.testDispatcherProvider, mockAddToHomeCapabilityDetector)

    @Test
    fun whenGetUserAppStageThenRetunCurrentStage() = coroutineRule.runBlocking {
        givenCurrentStage(AppStage.DAX_ONBOARDING)

        val userAppStage = testee.getUserAppStage()

        assertEquals(AppStage.DAX_ONBOARDING, userAppStage)
    }

    @Test
    fun whenStageNewCompletedThenStageDaxOnboardingReturned() = coroutineRule.runBlocking {
        givenCurrentStage(AppStage.NEW)

        val nextStage = testee.stageCompleted(AppStage.NEW)

        assertEquals(AppStage.DAX_ONBOARDING, nextStage)
    }

    @Test
    fun whenStageDaxOnboardingCompletedAndNotAbleToAddShortcutsThenStageEstablishedReturned() = coroutineRule.runBlocking {
        givenCurrentStage(AppStage.DAX_ONBOARDING)

        val nextStage = testee.stageCompleted(AppStage.DAX_ONBOARDING)

        assertEquals(AppStage.ESTABLISHED, nextStage)
    }

    @Test
    fun whenStageDaxOnboardingCompletedAndAbleToAddShortcutsThenStageUseOurAppNotificationReturned() = coroutineRule.runBlocking {
        givenCurrentStage(AppStage.DAX_ONBOARDING)
        whenever(mockAddToHomeCapabilityDetector.isAddToHomeSupported()).thenReturn(true)
        val nextStage = testee.stageCompleted(AppStage.DAX_ONBOARDING)

        assertEquals(AppStage.USE_OUR_APP_NOTIFICATION, nextStage)
    }

    @Test
    fun whenStageUseOurAppNotificationCompletedThenStageEstablishedReturned() = coroutineRule.runBlocking {
        givenCurrentStage(AppStage.USE_OUR_APP_NOTIFICATION)

        val nextStage = testee.stageCompleted(AppStage.USE_OUR_APP_NOTIFICATION)

        assertEquals(AppStage.ESTABLISHED, nextStage)
    }

    @Test
    fun whenStageUseOurAppOnboardingCompletedThenStageEstablishedReturned() = coroutineRule.runBlocking {
        givenCurrentStage(AppStage.USE_OUR_APP_ONBOARDING)

        val nextStage = testee.stageCompleted(AppStage.USE_OUR_APP_ONBOARDING)

        assertEquals(AppStage.ESTABLISHED, nextStage)
    }

    @Test
    fun whenStageEstablishedCompletedThenStageEstablishedReturned() = coroutineRule.runBlocking {
        givenCurrentStage(AppStage.ESTABLISHED)

        val nextStage = testee.stageCompleted(AppStage.ESTABLISHED)

        assertEquals(AppStage.ESTABLISHED, nextStage)
    }

    @Test
    fun whenRegisterInStageThenUpdateUserStageInDao() = coroutineRule.runBlocking {
        testee.registerInStage(AppStage.USE_OUR_APP_ONBOARDING)
        verify(userStageDao).updateUserStage(AppStage.USE_OUR_APP_ONBOARDING)
    }

    private suspend fun givenCurrentStage(appStage: AppStage) {
        whenever(userStageDao.currentUserAppStage()).thenReturn(UserStage(appStage = appStage))
    }
}
