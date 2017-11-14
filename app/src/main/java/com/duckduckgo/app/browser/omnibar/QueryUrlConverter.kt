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

package com.duckduckgo.app.browser.omnibar

import android.net.Uri
import android.support.v4.util.PatternsCompat
import com.duckduckgo.app.browser.DuckDuckGoRequestRewriter
import javax.inject.Inject

class QueryUrlConverter @Inject constructor(private val requestRewriter: DuckDuckGoRequestRewriter) : OmnibarEntryConverter {

    companion object {
        private const val https = "https"
        private const val http = "http"
        private const val baseUrl = "duckduckgo.com"
        private const val localhost = "localhost"
        private const val space = " "
        private val webUrlRegex = PatternsCompat.WEB_URL.toRegex()
    }

    override fun isWebUrl(inputQuery: String): Boolean {
        val uri = Uri.parse(inputQuery)
        if (uri.scheme == null) return isWebUrl("$https://$inputQuery")
        if (uri.scheme != http && uri.scheme != https) return false
        if (uri.userInfo != null) return false
        if (uri.host == null) return false
        if (uri.path.contains(space)) return false

        return isValidHost(uri.host)
    }

    private fun isValidHost(host: String): Boolean {
        if (host == localhost) return true
        if (host.contains(space)) return false
        if (host.contains("!")) return false

        if (webUrlRegex.containsMatchIn(host)) return true
        return false
    }

    override fun convertQueryToUri(inputQuery: String): Uri {
        val uriBuilder = Uri.Builder()
                .scheme(https)
                .appendQueryParameter("q", inputQuery)
                .authority(baseUrl)

        requestRewriter.addCustomQueryParams(uriBuilder)

        return uriBuilder.build()
    }

    override fun convertUri(input: String): String {
        var uri = Uri.parse(input)

        if(uri.scheme == null) {
            uri = Uri.parse("$http://$input")
        }

        if (uri.host == baseUrl) {
            return requestRewriter.rewriteRequestWithCustomQueryParams(uri).toString()
        }
        return uri.toString()
    }

}