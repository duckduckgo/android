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

package com.duckduckgo.app.job

import android.app.job.JobScheduler
import android.content.Context
import com.duckduckgo.app.global.job.JobBuilder
import timber.log.Timber
import javax.inject.Inject

class AppConfigurationSyncer @Inject constructor(
        private val jobBuilder: JobBuilder,
        private val jobScheduler: JobScheduler) {

    fun scheduleRegularSync(context: Context) {
        val jobInfo = jobBuilder.appConfigurationJob(context)

        val schedulingRequired = jobScheduler.allPendingJobs
                .filter { jobInfo.id == it.id }
                .count() == 0

        if (schedulingRequired) {
            Timber.i("Scheduling of background sync job, successful = %s", jobScheduler.schedule(jobInfo))
        } else {
            Timber.i("Job already scheduled; no need to schedule again")
        }
    }
}