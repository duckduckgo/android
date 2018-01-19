/*
 * Copyright (c) 2018 DuckDuckGo
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

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build.VERSION.SDK_INT
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.PopupWindow

class BrowserPopupMenu : PopupWindow {

    constructor(layoutInflater: LayoutInflater, view: View = BrowserPopupMenu.inflate(layoutInflater))
            : super(view, WRAP_CONTENT, WRAP_CONTENT, true) {

        if (SDK_INT > 21) {
            elevation = 6.toFloat()
        } else {
            setBackgroundDrawable(ColorDrawable(Color.WHITE))
        }

        animationStyle = android.R.style.Animation_Dialog
    }

    fun show(rootView: View, anchorView: View) {
        val anchorLocation = IntArray(2)
        anchorView.getLocationOnScreen(anchorLocation)
        val x = margin
        val y = anchorLocation[1] + margin

        if (SDK_INT > 21) {
            showAtLocation(rootView, Gravity.TOP or Gravity.END, x, y)
        } else {
            showAtLocation(rootView, Gravity.TOP or Gravity.END, 0, 0)
        }
    }

    companion object {

        val margin = 30

        fun inflate(layoutInflater: LayoutInflater): View {
            return layoutInflater.inflate(R.layout.popup_window_brower_menu, null)
        }

    }
}

