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

import android.net.Uri
import android.view.View
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import timber.log.Timber
import javax.inject.Inject


class BrowserChromeClient @Inject constructor() : WebChromeClient() {

    var webViewClientListener: WebViewClientListener? = null

    private var customView: View? = null
    private var currentUrl: String? = null

    override fun onShowCustomView(view: View, callback: CustomViewCallback?) {
        Timber.d("on show custom view")

        if (customView != null) {
            callback?.onCustomViewHidden()
            return
        }

        customView = view
        webViewClientListener?.goFullScreen(view)
    }

    override fun onHideCustomView() {
        Timber.d("on hide custom view")

        webViewClientListener?.exitFullScreen()
        customView = null
    }

    override fun onProgressChanged(webView: WebView, newProgress: Int) {
        Timber.d("onProgressChanged - $newProgress - ${webView.url}")

        webViewClientListener?.progressChanged(newProgress)

        if (currentUrl != webView.url) {
            currentUrl = webView.url
            webViewClientListener?.urlChanged(currentUrl)
        }
    }

    override fun onReceivedTitle(view: WebView, title: String) {
        webViewClientListener?.titleReceived(title)
    }

    override fun onShowFileChooser(webView: WebView, filePathCallback: ValueCallback<Array<Uri>>, fileChooserParams: FileChooserParams): Boolean {
        webViewClientListener?.showFileChooser(filePathCallback, fileChooserParams)
        return true
    }
}