/*
 * Copyright (c) 2021 DuckDuckGo
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

package com.duckduckgo.app.email.waitlist

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.email.db.EmailDataStore
import com.google.android.material.textview.MaterialTextView
import dagger.android.support.AndroidSupportInjection
import javax.inject.Inject

class WaitlistNotificationDialog : DialogFragment() {

    @Inject
    lateinit var emailDataStore: EmailDataStore

    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val rootView = View.inflate(activity, R.layout.email_dialog_fragment, null)
        val message = rootView.findViewById<MaterialTextView>(R.id.emailDialogText)
        message.text = getString(R.string.waitlistNotificationDialogDescription)

        val alertBuilder = AlertDialog.Builder(requireActivity())
            .setView(rootView)
            .setNegativeButton(R.string.waitlistNotificationDialogNoThanks) { _, _ ->
                dismiss()
            }
            .setPositiveButton(R.string.waitlistNotificationDialogNotifyMe) { _, _ ->
                emailDataStore.sendNotification = true
            }

        return alertBuilder.create()
    }

    companion object {
        fun create(): WaitlistNotificationDialog = WaitlistNotificationDialog()
    }

}
