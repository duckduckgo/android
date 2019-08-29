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

package com.duckduckgo.app.onboarding.ui.page

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.duckduckgo.app.browser.R
import com.duckduckgo.app.browser.defaultbrowsing.DefaultBrowserDetector
import com.duckduckgo.app.browser.defaultbrowsing.DefaultBrowserSystemSettings
import com.duckduckgo.app.global.ViewModelFactory
import com.duckduckgo.app.global.install.AppInstallStore
import com.duckduckgo.app.statistics.pixels.Pixel
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.content_onboarding_default_browser.continueButton
import kotlinx.android.synthetic.main.content_onboarding_default_browser.launchSettingsButton
import kotlinx.android.synthetic.main.content_onboarding_default_browser_experiment.*
import kotlinx.android.synthetic.main.content_onboarding_default_browser_experiment.defaultBrowserImage
import timber.log.Timber
import javax.inject.Inject

class DefaultBrowserPageExperiment : OnboardingPageFragment() {
    override fun layoutResource(): Int = R.layout.content_onboarding_default_browser_experiment

    @Inject
    lateinit var pixel: Pixel

    @Inject
    lateinit var installStore: AppInstallStore

    @Inject
    lateinit var viewModelFactory: ViewModelFactory

    @Inject
    lateinit var defaultBrowserDetector: DefaultBrowserDetector

    private var userTriedToSetDDGAsDefault = false
    private var toast: Toast? = null

    private val viewModel: DefaultBrowserPageExperimentViewModel by lazy {
        ViewModelProviders.of(this, viewModelFactory).get(DefaultBrowserPageExperimentViewModel::class.java)
    }


    override fun onAttach(context: Context) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        if (savedInstanceState != null) {
            userTriedToSetDDGAsDefault = savedInstanceState.getBoolean(SAVED_STATE_LAUNCHED_DEFAULT)
        }

        observeViewModel()

        extractContinueButtonTextResourceId()?.let { continueButton.setText(it) }

        launchSettingsButton.setOnClickListener {
            viewModel.onDefaultBrowserClicked()
        }
        continueButton.setOnClickListener {
            if (!userTriedToSetDDGAsDefault) {
                pixel.fire(Pixel.PixelName.ONBOARDING_DEFAULT_BROWSER_SKIPPED)
            }
            onContinuePressed()
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.reloadInstructions()
    }

    private fun observeViewModel() {
        viewModel.viewState.observe(this, Observer<DefaultBrowserPageExperimentViewModel.ViewState> { viewState ->
            viewState?.let {
                if (it.showSettingsUI) setUIForSettings() else setUIForDialog()
                if (it.showInstructionsCard) showCard() else hideCard()
                setOnlyContinue(it.showOnlyContinue)
            }
        })

        viewModel.command.observe(this, Observer {
            when (it) {
                is DefaultBrowserPageExperimentViewModel.Command.OpenDialog -> onLaunchDefaultBrowserWithDialogClicked(it.timesOpened)
                is DefaultBrowserPageExperimentViewModel.Command.OpenSettings -> onLaunchDefaultBrowserSettingsClicked()
                is DefaultBrowserPageExperimentViewModel.Command.ContinueToBrowser -> onContinuePressed()
            }
        })
    }

    private fun setOnlyContinue(visible: Boolean) {
        if (visible) {
            defaultBrowserImage.setImageResource(R.drawable.set_as_default_browser_illustration_experiment)
            launchSettingsButton.visibility = View.GONE
            browserProtectionSubtitle.text = "You are now using DuckDuckGo as your default browser"
        } else {
            launchSettingsButton.visibility = View.VISIBLE
        }
    }

    private fun setUIForDialog() {
        defaultBrowserImage.setImageResource(R.drawable.set_as_default_browser_illustration_experiment)
        browserProtectionSubtitle.text = getString(R.string.defaultBrowserDescriptionNoDefault)
    }

    private fun setUIForSettings() {
        defaultBrowserImage.setImageResource(R.drawable.set_as_default_browser_illustration)
        browserProtectionSubtitle.text = getString(R.string.onboardingDefaultBrowserDescription)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putBoolean(SAVED_STATE_LAUNCHED_DEFAULT, userTriedToSetDDGAsDefault)
    }

    @SuppressLint("InflateParams")
    private fun showCard() {
        toast?.cancel()
        defaultCard.visibility = View.VISIBLE
        defaultCard.alpha = 1f
        val inflater = LayoutInflater.from(requireContext())
        val inflatedView = inflater.inflate(R.layout.content_onboarding_default_browser_card, null)
        toast = Toast(requireContext()).apply {
            view = inflatedView
            setGravity(Gravity.TOP or Gravity.FILL_HORIZONTAL, 0, 0)
            duration = Toast.LENGTH_LONG
        }
        toast?.show()
    }

    private fun hideCard() {
        toast?.cancel()
        defaultCard.animate()
            .alpha(0f)
            .setDuration(100)
            .start()
    }

    private fun onLaunchDefaultBrowserWithDialogClicked(value: Int) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(DEFAULT_URL))
        intent.putExtra(TIMES_OPENED, value)
        startActivityForResult(intent, DEFAULT_BROWSER_REQUEST_CODE_DIALOG)
    }

    private fun onLaunchDefaultBrowserSettingsClicked() {
        userTriedToSetDDGAsDefault = true
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val intent = DefaultBrowserSystemSettings.intent()
            try {
                startActivityForResult(intent, DEFAULT_BROWSER_REQUEST_CODE_SETTINGS)
            } catch (e: ActivityNotFoundException) {
                Timber.w(e, getString(R.string.cannotLaunchDefaultAppSettings))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            DEFAULT_BROWSER_REQUEST_CODE_SETTINGS -> {
                viewModel.handleResult(DefaultBrowserPageExperimentViewModel.Origin.Settings)
            }
            DEFAULT_BROWSER_REQUEST_CODE_DIALOG -> {
                val timesOpened = data?.getIntExtra(TIMES_OPENED, -1)
                val origin = if (timesOpened != null && timesOpened != -1) {
                    DefaultBrowserPageExperimentViewModel.Origin.InternalBrowser(timesOpened)
                } else {
                    DefaultBrowserPageExperimentViewModel.Origin.ExternalBrowser
                }
                viewModel.handleResult(origin)
            }
            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val DEFAULT_BROWSER_REQUEST_CODE_DIALOG = 101
        private const val DEFAULT_URL = "https://donttrack.us"
        private const val DEFAULT_BROWSER_REQUEST_CODE_SETTINGS = 100
        private const val SAVED_STATE_LAUNCHED_DEFAULT = "SAVED_STATE_LAUNCHED_DEFAULT"
        const val TIMES_OPENED = "timesOpened"
    }
}