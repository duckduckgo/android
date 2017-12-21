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

package com.duckduckgo.app.privacymonitor.ui

import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import com.duckduckgo.app.privacymonitor.PrivacyMonitor
import com.duckduckgo.app.privacymonitor.model.TermsOfService

class PrivacyPracticesViewModel : ViewModel() {

    data class ViewState(
            val domain: String,
            val practices: TermsOfService.Practices,
            val goodTerms: List<String>,
            val badTerms: List<String>
    )

    val viewState: MutableLiveData<PrivacyPracticesViewModel.ViewState> = MutableLiveData()

    init {
        resetViewState()
    }

    private fun resetViewState() {
        viewState.value = PrivacyPracticesViewModel.ViewState(
                domain = "",
                practices = TermsOfService.Practices.UNKNOWN,
                goodTerms = ArrayList(),
                badTerms = ArrayList()
        )
    }

    fun onPrivacyMonitorChanged(monitor: PrivacyMonitor?) {
        if (monitor == null) {
            resetViewState()
            return
        }
        viewState.value = PrivacyPracticesViewModel.ViewState(
                domain = monitor.uri?.host ?: "",
                practices = monitor.termsOfService.practices,
                goodTerms = monitor.termsOfService.goodPrivacyTerms,
                badTerms = monitor.termsOfService.badPrivacyTerms
        )
    }


}