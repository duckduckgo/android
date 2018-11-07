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

package com.duckduckgo.app.onboarding.ui

import android.content.Context
import androidx.annotation.ColorInt
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.core.content.ContextCompat
import com.duckduckgo.app.browser.R

class PagerAdapter(fragmentManager: androidx.fragment.app.FragmentManager, private val viewModel: OnboardingViewModel) : androidx.fragment.app.FragmentPagerAdapter(fragmentManager) {

    override fun getCount(): Int {
        return viewModel.pageCount()
    }

    override fun getItem(position: Int): OnboardingPageFragment? {
        return viewModel.getItem(position)
    }

    @ColorInt
    fun color(context: Context, currentPage: Int): Int {
        val color = getItem(currentPage)?.backgroundColor() ?: R.color.lightOliveGreen
        return ContextCompat.getColor(context, color)
    }
}