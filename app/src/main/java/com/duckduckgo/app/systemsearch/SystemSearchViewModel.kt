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

package com.duckduckgo.app.systemsearch

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.duckduckgo.app.autocomplete.api.AutoComplete
import com.duckduckgo.app.autocomplete.api.AutoComplete.AutoCompleteResult
import com.duckduckgo.app.global.DefaultDispatcherProvider
import com.duckduckgo.app.global.DispatcherProvider
import com.duckduckgo.app.global.SingleLiveEvent
import com.duckduckgo.app.onboarding.store.OnboardingStore
import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import timber.log.Timber
import java.util.concurrent.TimeUnit

class SystemSearchViewModel(
    private var onboardingStore: OnboardingStore,
    private val autoComplete: AutoComplete,
    private val deviceAppLookup: DeviceAppLookup,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider()
) : ViewModel() {

    data class OnboardingViewState(
        val visibile: Boolean,
        val expanded: Boolean = false
    )

    data class SystemSearchResultsViewState(
        val queryText: String = "",
        val autocompleteResults: AutoCompleteResult = AutoCompleteResult("", emptyList()),
        val appResults: List<DeviceApp> = emptyList()
    )

    sealed class Command {
        object LaunchDuckDuckGo : Command()
        data class LaunchBrowser(val query: String) : Command()
        data class LaunchDeviceApplication(val deviceApp: DeviceApp) : Command()
        data class ShowAppNotFoundMessage(val appName: String) : Command()
        object DismissKeyboard : Command()
    }

    val onboardingViewState: MutableLiveData<OnboardingViewState> = MutableLiveData()
    val resutlsViewState: MutableLiveData<SystemSearchResultsViewState> = MutableLiveData()
    val command: SingleLiveEvent<Command> = SingleLiveEvent()

    private val autoCompletePublishSubject = PublishRelay.create<String>()
    private var autocompleteResults: AutoCompleteResult = AutoCompleteResult("", emptyList())
    private var autoCompleteDisposable: Disposable? = null

    private var appsJob: Job? = null
    private var appResults: List<DeviceApp> = emptyList()

    init {
        resetViewState()
        configureAutoComplete()
    }

    private fun currentOnboardingState(): OnboardingViewState = onboardingViewState.value!!
    private fun currentResultsState(): SystemSearchResultsViewState = resutlsViewState.value!!

    fun resetViewState() {
        resetOnboardingState()
        resetResultsState()
    }

    private fun resetOnboardingState() {
        onboardingViewState.value = OnboardingViewState(visibile = onboardingStore.shouldShow)
    }

    private fun resetResultsState() {
        autocompleteResults = AutoCompleteResult("", emptyList())
        appsJob?.cancel()
        appResults = emptyList()
        resutlsViewState.value = SystemSearchResultsViewState()
    }

    private fun configureAutoComplete() {
        autoCompleteDisposable = autoCompletePublishSubject
            .debounce(DEBOUNCE_TIME_MS, TimeUnit.MILLISECONDS)
            .switchMap { autoComplete.autoComplete(it) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ result ->
                updateAutocompleteResult(result)
            }, { t: Throwable? -> Timber.w(t, "Failed to get search results") })
    }

    fun userTappedOnboardingToggle() {
        onboardingViewState.value = currentOnboardingState().copy(expanded = !currentOnboardingState().expanded)
        if (currentOnboardingState().expanded) {
            command.value = Command.DismissKeyboard
        }
    }

    fun userDismissedOnboarding() {
        onboardingViewState.value = currentOnboardingState().copy(visibile = false)
        onboardingStore.onboardingShown()
    }

    fun userUpdatedQuery(query: String) {

        appsJob?.cancel()

        if (query == currentResultsState().queryText) {
            return
        }

        if (query.isBlank()) {
            userClearedQuery()
            return
        }

        resutlsViewState.value = currentResultsState().copy(queryText = query)

        val trimmedQuery = query.trim()
        autoCompletePublishSubject.accept(trimmedQuery)
        appsJob = viewModelScope.launch(dispatchers.io()) {
            updateAppResults(deviceAppLookup.query(trimmedQuery))
        }
    }

    private fun updateAppResults(results: List<DeviceApp>) {
        appResults = results
        refreshResultsViewState()
    }

    private fun updateAutocompleteResult(results: AutoCompleteResult) {
        autocompleteResults = results
        refreshResultsViewState()
    }

    private fun refreshResultsViewState() {
        val hasMultiResults = autocompleteResults.suggestions.isNotEmpty() && appResults.isNotEmpty()
        val fullSuggestions = autocompleteResults.suggestions
        val updatedSuggestions = if (hasMultiResults) fullSuggestions.take(RESULTS_MAX_RESULTS_PER_GROUP) else fullSuggestions
        val updatedApps = if (hasMultiResults) appResults.take(RESULTS_MAX_RESULTS_PER_GROUP) else appResults

        resutlsViewState.postValue(
            currentResultsState().copy(
                autocompleteResults = AutoCompleteResult(autocompleteResults.query, updatedSuggestions),
                appResults = updatedApps
            )
        )
    }

    fun userTappedDax() {
        command.value = Command.LaunchDuckDuckGo
    }

    fun userClearedQuery() {
        autoCompletePublishSubject.accept("")
        resetResultsState()
    }

    fun userSubmittedQuery(query: String) {
        command.value = Command.LaunchBrowser(query)
    }

    fun userSubmittedAutocompleteResult(query: String) {
        command.value = Command.LaunchBrowser(query)
    }

    fun userSelectedApp(app: DeviceApp) {
        command.value = Command.LaunchDeviceApplication(app)
    }

    fun appNotFound(app: DeviceApp) {
        command.value = Command.ShowAppNotFoundMessage(app.shortName)
        deviceAppLookup.refreshAppList()
    }

    override fun onCleared() {
        autoCompleteDisposable?.dispose()
        autoCompleteDisposable = null
        super.onCleared()
    }

    companion object {
        private const val DEBOUNCE_TIME_MS = 200L
        private const val RESULTS_MAX_RESULTS_PER_GROUP = 4
    }
}
