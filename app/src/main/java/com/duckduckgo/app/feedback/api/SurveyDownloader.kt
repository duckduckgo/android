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

package com.duckduckgo.app.feedback.api

import com.duckduckgo.app.feedback.api.SurveyGroup.SurveyOption
import com.duckduckgo.app.feedback.db.SurveyDao
import com.duckduckgo.app.feedback.model.Survey
import io.reactivex.Completable
import timber.log.Timber
import java.io.IOException
import java.util.*
import javax.inject.Inject

class SurveyDownloader @Inject constructor(
    private val service: SurveyService,
    private val surveyDao: SurveyDao
) {

    fun download(): Completable {

        return Completable.fromAction {

            Timber.d("Downloading use survey data")

            val call = service.survey()
            val response = call.execute()

            Timber.d("Response received, success=${response.isSuccessful}")

            if (!response.isSuccessful) {
                throw IOException("Status: ${response.code()} - ${response.errorBody()?.string()}")
            }

            val surveyGroup = response.body()
            if (surveyGroup == null || surveyGroup.id == null) {
                Timber.d("No survey received, canceling any old scheduled surveys")
                surveyDao.cancelScheduledSurveys()
                return@fromAction
            }

            if (surveyDao.exists(surveyGroup.id)) {
                Timber.d("Survey received is already in db, ignoring")
                return@fromAction
            }

            Timber.d("New survey received, canceling any old scheduled surveys")
            surveyDao.cancelScheduledSurveys()
            val surveyOption = determineOption(surveyGroup.surveyOptions) ?: return@fromAction
            val newSurvey = Survey(surveyGroup.id, surveyOption.url, surveyOption.installationDay, Survey.Status.SCHEDULED)
            surveyDao.insert(newSurvey)
        }
    }

    private fun determineOption(options: List<SurveyOption>): SurveyOption? {
        var current = 0.0
        val randomAllocation = Random().nextDouble()

        for (option: SurveyOption in options) {
            current += option.ratioOfUsersToShow
            if (randomAllocation <= current) {
                return option
            }
        }
        return null
    }
}
