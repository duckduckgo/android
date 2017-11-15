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

import android.annotation.SuppressLint
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import com.duckduckgo.app.global.DuckDuckGoActivity
import com.duckduckgo.app.global.ViewModelFactory
import kotlinx.android.synthetic.main.activity_browser.*
import kotlinx.android.synthetic.main.content_browser.*
import timber.log.Timber
import javax.inject.Inject

class BrowserActivity : DuckDuckGoActivity() {

    @Inject lateinit var webViewClient: BrowserWebViewClient
    @Inject lateinit var webChromeClient: BrowserChromeClient
    @Inject lateinit var viewModelFactory: ViewModelFactory

    private val viewModel: BrowserViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(BrowserViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_browser)

        viewModel.viewState.observe(this, Observer<BrowserViewModel.ViewState> {
            it!!
            when (it.loadingData) {
                true -> pageLoadingIndicator.visibility = View.VISIBLE
                false -> pageLoadingIndicator.visibility = View.INVISIBLE
            }

            it.url?.let { urlInput.setText(it) }

            pageLoadingIndicator.progress = it.progress
        })

        viewModel.query.observe(this, Observer {
            it?.let { Timber.w("Here"); webView.loadUrl(it) }
        })

        swipeToRefreshContainer.setOnRefreshListener {
            swipeToRefreshContainer.isRefreshing = false
        }

        configureToolbar()
        configureWebView()
    }

    private fun configureToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = null
    }

    private fun userEnteredQuery() {
        viewModel.onQueryEntered(urlInput.text.toString())
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun configureWebView() {
        webView.webViewClient = webViewClient
        webView.webChromeClient = webChromeClient
        webView.settings.javaScriptEnabled = true

        viewModel.registerWebViewListener(webViewClient, webChromeClient)

        urlInput.setOnEditorActionListener(TextView.OnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                userEnteredQuery()
                return@OnEditorActionListener true
            }
            false
        })
    }

    override fun onSaveInstanceState(bundle: Bundle?) {
        super.onSaveInstanceState(bundle)
        webView.saveState(bundle)
    }

    override fun onRestoreInstanceState(bundle: Bundle?) {
        super.onRestoreInstanceState(bundle)
        webView.restoreState(bundle)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_browser_activity, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.refresh_menu_item -> {
                webView.reload()
                return true
            }
            R.id.back_menu_item -> {
                webView.goBack()
                return true
            }
            R.id.forward_menu_item -> {
                webView.goForward()
                return true
            }
        }
        return false
    }
}
