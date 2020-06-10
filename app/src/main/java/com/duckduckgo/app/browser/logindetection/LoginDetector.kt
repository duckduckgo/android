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

package com.duckduckgo.app.browser.logindetection

import android.content.Context
import android.webkit.WebResourceRequest
import android.webkit.WebView
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.settings.db.SettingsDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

interface LoginDetector {
    fun addLoginDetection(webView: WebView, onLoginDetected: () -> Unit)
    suspend fun onEvent(event: WebNavigationEvent)
}

sealed class WebNavigationEvent {
    data class OnPageStarted(val webView: WebView) : WebNavigationEvent()
    data class ShouldInterceptRequest(val webView: WebView, val request: WebResourceRequest) : WebNavigationEvent()
}

class JSLoginDetector @Inject constructor(private val settingsDataStore: SettingsDataStore) : LoginDetector {

    companion object {
        const val HTTP_POST = "POST"
    }

    private val javaScriptDetector = JavaScriptDetector()
    private val loginPathRegex = Regex("login|sign-in|signin|sessions")

    override fun addLoginDetection(webView: WebView, onLoginDetected: () -> Unit) {
        webView.addJavascriptInterface(LoginDetectionJsInterface { onLoginDetected() }, LOGIN_DETECTION_JS_INTERFACE_NAME)
    }

    override suspend fun onEvent(event: WebNavigationEvent) {
        if (settingsDataStore.appLoginDetection) {
            when (event) {
                is WebNavigationEvent.OnPageStarted -> injectLoginFormDetectionJS(event.webView)
                is WebNavigationEvent.ShouldInterceptRequest -> {
                    if (evaluateIfLoginPostRequest(event.request)) {
                        scanForPasswordFields(event.webView)
                    }
                }
            }
        }
    }

    private fun evaluateIfLoginPostRequest(request: WebResourceRequest): Boolean {
        if (request.method == HTTP_POST) {
            Timber.i("LoginDetector: evaluate ${request.url}")
            if (request.url?.path?.contains(loginPathRegex) == true) {
                Timber.v("LoginDetector: post login DETECTED")
                return true
            }
        }
        return false
    }

    private suspend fun scanForPasswordFields(webView: WebView) {
        withContext(Dispatchers.Main) {
            webView.evaluateJavascript("javascript:${javaScriptDetector.loginFormDetector(webView.context)}", null)
        }
    }

    private fun injectLoginFormDetectionJS(webView: WebView) {
        webView.evaluateJavascript("javascript:${javaScriptDetector.loginFormEventsDetector(webView.context)}", null)
    }

    private class JavaScriptDetector {
        private lateinit var functions: String
        private lateinit var handlers: String

        private fun getFunctionsJS(context: Context): String {
            if (!this::functions.isInitialized) {
                functions = context.resources.openRawResource(R.raw.login_form_detection_functions).bufferedReader().use { it.readText() }
            }
            return functions
        }

        private fun getHandlersJS(context: Context): String {
            if (!this::handlers.isInitialized) {
                handlers = context.resources.openRawResource(R.raw.login_form_detection_handlers).bufferedReader().use { it.readText() }
            }
            return handlers
        }

        fun loginFormEventsDetector(context: Context): String {
            return getFunctionsJS(context) + getHandlersJS(context)
        }

        fun loginFormDetector(context: Context): String {
            return getFunctionsJS(context) + "scanForPasswordField();"
        }
    }
}