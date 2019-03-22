/*
 * Copyright (c) 2019 DuckDuckGo
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

package com.duckduckgo.app.browser.ui

import androidx.appcompat.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.webkit.HttpAuthHandler
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.global.view.hideKeyboard
import com.duckduckgo.app.global.view.showKeyboard
import org.jetbrains.anko.find

class HttpAuthenticationDialogFragment : DialogFragment() {

    var listener: HttpAuthenticationListener? = null
    lateinit var handler: HttpAuthHandler

    interface HttpAuthenticationListener {
        fun handleAuthentication(handler: HttpAuthHandler, username: String, password: String)
        fun cancelAuthentication()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rootView = View.inflate(activity, R.layout.http_authentication, null)
        val usernameInput = rootView.find<EditText>(R.id.usernameInput)
        val passwordInput = rootView.find<EditText>(R.id.passwordInput)
        val informationText = rootView.find<TextView>(R.id.httpAuthInformationText)


        validateBundleArguments()

        val url = arguments!!.getString(KEY_TARGET_URL)

        informationText.text = getString(R.string.authenticationDialogMessage, url)

        val alertBuilder = AlertDialog.Builder(activity!!)
            .setView(rootView)
            .setPositiveButton(R.string.authenticationDialogPositiveButton) { _, _ ->
                listener?.handleAuthentication(handler, usernameInput.text.toString(), passwordInput.text.toString())

            }.setNegativeButton(R.string.authenticationDialogNegativeButton) { _, _ ->
                rootView.hideKeyboard()
                listener?.cancelAuthentication()
            }
            .setTitle(R.string.authenticationDialogTitle)

        val alert = alertBuilder.create()
        showKeyboard(usernameInput, alert)

        return alert

    }

    private fun validateBundleArguments() {
        if (arguments == null) throw IllegalArgumentException("Missing arguments bundle")
        val args = arguments!!
        if (!args.containsKey(KEY_TARGET_URL)) {
            throw IllegalArgumentException("Bundle arguments required [KEY_TARGET_URL]")
        }
    }

    private fun showKeyboard(titleInput: EditText, alert: androidx.appcompat.app.AlertDialog) {
        titleInput.setSelection(titleInput.text.length)
        titleInput.showKeyboard()
        alert.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE)
    }

    companion object {
        private const val KEY_TARGET_URL = "KEY_TARGET_URL"

        fun createHttpAuthenticationDialog(url: String) : HttpAuthenticationDialogFragment {
            val dialog = HttpAuthenticationDialogFragment()
            val bundle = Bundle()
            bundle.putString(KEY_TARGET_URL, url)
            dialog.arguments = bundle
            return dialog
        }
    }
}