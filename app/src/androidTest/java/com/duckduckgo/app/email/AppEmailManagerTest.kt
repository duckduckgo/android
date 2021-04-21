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

package com.duckduckgo.app.email

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.duckduckgo.app.CoroutineTestRule
import com.duckduckgo.app.email.AppEmailManager.Companion.DUCK_EMAIL_DOMAIN
import com.duckduckgo.app.email.api.EmailAlias
import com.duckduckgo.app.email.api.EmailService
import com.duckduckgo.app.email.db.EmailDataStore
import com.duckduckgo.app.runBlocking
import com.nhaarman.mockitokotlin2.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.first
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@FlowPreview
@ExperimentalCoroutinesApi
class AppEmailManagerTest {

    @get:Rule
    var coroutineRule = CoroutineTestRule()

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val mockEmailService: EmailService = mock()
    private val mockEmailDataStore: EmailDataStore = mock()
    private val aliasChannel = Channel<String?>()
    lateinit var testee: AppEmailManager

    @Before
    fun setup() {
        whenever(mockEmailDataStore.nextAliasFlow()).thenReturn(aliasChannel.consumeAsFlow())
        testee = AppEmailManager(mockEmailService, mockEmailDataStore, coroutineRule.testDispatcherProvider)

        coroutineRule.runBlocking {
            testee.onAppForegrounded()
        }
    }

    @After
    fun after() {
        aliasChannel.close()
    }

    @Test
    fun whenFetchAliasFromServiceThenStoreAliasAddingDuckDomain() = coroutineRule.runBlocking {
        whenever(mockEmailDataStore.emailToken).thenReturn("token")
        whenever(mockEmailService.newAlias(any())).thenReturn(EmailAlias("test"))
        testee.getAlias()

        verify(mockEmailDataStore).nextAlias = "test$DUCK_EMAIL_DOMAIN"
    }

    @Test
    fun whenFetchAliasFromServiceAndTokenDoesNotExistThenDoNothing() = coroutineRule.runBlocking {
        whenever(mockEmailDataStore.emailToken).thenReturn(null)
        testee.getAlias()

        verify(mockEmailService, never()).newAlias(any())
    }

    @Test
    fun whenFetchAliasFromServiceAndAddressIsBlankThenStoreNullTwice() = coroutineRule.runBlocking {
        whenever(mockEmailDataStore.emailToken).thenReturn("token")
        whenever(mockEmailService.newAlias(any())).thenReturn(EmailAlias(""))
        testee.getAlias()

        verify(mockEmailDataStore, times(2)).nextAlias = null
    }

    @Test
    fun whenNewAliasReceivedThenNextAliasUpdated() = coroutineRule.runBlocking {
        aliasChannel.send("newAlias")

        assertEquals("newAlias", testee.nextAlias)
    }

    @Test
    fun whenGetAliasThenReturnNextAlias() = coroutineRule.runBlocking {
        givenNextAliasExists()

        assertEquals("alias", testee.getAlias())
    }

    @Test
    fun whenGetAliasIfNextAliasDoesNotExistThenReturnNull() {
        assertNull(testee.getAlias())
    }

    @Test
    fun whenGetAliasThenClearNextAlias() {
        testee.getAlias()

        verify(mockEmailDataStore).nextAlias = null
    }

    @Test
    fun whenIsSignedInAndTokenDoesNotExistThenReturnFalse() {
        whenever(mockEmailDataStore.emailUsername).thenReturn("username")
        whenever(mockEmailDataStore.nextAlias).thenReturn("alias")

        assertFalse(testee.isSignedIn())
    }

    @Test
    fun whenIsSignedInAndUsernameDoesNotExistThenReturnFalse() {
        whenever(mockEmailDataStore.emailToken).thenReturn("token")
        whenever(mockEmailDataStore.nextAlias).thenReturn("alias")

        assertFalse(testee.isSignedIn())
    }

    @Test
    fun whenIsSignedInAndNextAliasDoesNotExistThenReturnFalse() {
        whenever(mockEmailDataStore.emailToken).thenReturn("token")
        whenever(mockEmailDataStore.emailUsername).thenReturn("username")

        assertFalse(testee.isSignedIn())
    }

    @Test
    fun whenIsSignedInAndTokenAndUsernameExistAndNextAliasExistThenReturnTrue() {
        whenever(mockEmailDataStore.emailToken).thenReturn("token")
        whenever(mockEmailDataStore.emailUsername).thenReturn("username")
        whenever(mockEmailDataStore.nextAlias).thenReturn("alias")

        assertTrue(testee.isSignedIn())
    }

    @Test
    fun whenStoreCredentialsThenGenerateNewAlias() = coroutineRule.runBlocking {
        whenever(mockEmailDataStore.emailToken).thenReturn("token")
        whenever(mockEmailService.newAlias(any())).thenReturn(EmailAlias(""))

        testee.storeCredentials("token", "username")

        verify(mockEmailService).newAlias(any())
    }

    @Test
    fun whenStoreCredentialsThenCredentialsAreStoredInDataStore() {
        testee.storeCredentials("token", "username")

        verify(mockEmailDataStore).emailUsername = "username"
        verify(mockEmailDataStore).emailToken = "token"
    }

    @Test
    fun whenStoreCredentialsThenIsSignedInChannelSendsTrue() = coroutineRule.runBlocking {
        testee.storeCredentials("token", "username")

        assertTrue(testee.signedInFlow().first())
    }

    @Test
    fun whenSignedOutThenClearEmailDataAndNextAliasIsNull() {
        testee.signOut()

        verify(mockEmailDataStore).emailUsername = null
        verify(mockEmailDataStore).emailToken = null
        verify(mockEmailDataStore).nextAlias = null
        assertNull(testee.nextAlias)
    }

    @Test
    fun whenSignedOutThenIsSignedInChannelSendsFalse() = coroutineRule.runBlocking {
        testee.signOut()

        assertFalse(testee.signedInFlow().first())
    }

    @Test
    fun whenGetEmailAddressThenDuckEmailDomainIsAppended() {
        whenever(mockEmailDataStore.emailUsername).thenReturn("username")

        assertEquals("username$DUCK_EMAIL_DOMAIN", testee.getEmailAddress())
    }

    private suspend fun givenNextAliasExists() {
        aliasChannel.send("alias")
    }
}
