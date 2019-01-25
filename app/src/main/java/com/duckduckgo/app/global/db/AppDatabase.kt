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

package com.duckduckgo.app.global.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.duckduckgo.app.bookmarks.db.BookmarkEntity
import com.duckduckgo.app.bookmarks.db.BookmarksDao
import com.duckduckgo.app.cta.db.DismissedCtaDao
import com.duckduckgo.app.cta.model.DismissedCta
import com.duckduckgo.app.entities.db.EntityListDao
import com.duckduckgo.app.entities.db.EntityListEntity
import com.duckduckgo.app.feedback.db.SurveyDao
import com.duckduckgo.app.feedback.model.Survey
import com.duckduckgo.app.httpsupgrade.db.HttpsBloomFilterSpecDao
import com.duckduckgo.app.httpsupgrade.db.HttpsWhitelistDao
import com.duckduckgo.app.httpsupgrade.model.HttpsBloomFilterSpec
import com.duckduckgo.app.httpsupgrade.model.HttpsWhitelistedDomain
import com.duckduckgo.app.privacy.db.NetworkLeaderboardDao
import com.duckduckgo.app.privacy.db.NetworkLeaderboardEntry
import com.duckduckgo.app.privacy.db.SiteVisitedEntity
import com.duckduckgo.app.tabs.db.TabsDao
import com.duckduckgo.app.tabs.model.TabEntity
import com.duckduckgo.app.tabs.model.TabSelectionEntity
import com.duckduckgo.app.trackerdetection.db.TrackerDataDao
import com.duckduckgo.app.trackerdetection.model.DisconnectTracker
import com.duckduckgo.app.usage.app.AppDaysUsedDao
import com.duckduckgo.app.usage.app.AppDaysUsedEntity
import com.duckduckgo.app.usage.search.SearchCountDao
import com.duckduckgo.app.usage.search.SearchCountEntity

@Database(
    exportSchema = true, version = 9, entities = [
        DisconnectTracker::class,
        HttpsBloomFilterSpec::class,
        HttpsWhitelistedDomain::class,
        NetworkLeaderboardEntry::class,
        SiteVisitedEntity::class,
        AppConfigurationEntity::class,
        TabEntity::class,
        TabSelectionEntity::class,
        BookmarkEntity::class,
        EntityListEntity::class,
        Survey::class,
        DismissedCta::class,
        SearchCountEntity::class,
        AppDaysUsedEntity::class
    ]
)

@TypeConverters(
    Survey.StatusTypeConverter::class,
    DismissedCta.IdTypeConverter::class
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun trackerDataDao(): TrackerDataDao
    abstract fun httpsWhitelistedDao(): HttpsWhitelistDao
    abstract fun httpsBloomFilterSpecDao(): HttpsBloomFilterSpecDao
    abstract fun networkLeaderboardDao(): NetworkLeaderboardDao
    abstract fun appConfigurationDao(): AppConfigurationDao
    abstract fun tabsDao(): TabsDao
    abstract fun bookmarksDao(): BookmarksDao
    abstract fun networkEntityDao(): EntityListDao
    abstract fun surveyDao(): SurveyDao
    abstract fun dismissedCtaDao(): DismissedCtaDao
    abstract fun searchCountDao(): SearchCountDao
    abstract fun appsDaysUsedDao(): AppDaysUsedDao

    companion object {
        val MIGRATION_1_TO_2: Migration = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `tabs` (`tabId` TEXT NOT NULL, `url` TEXT, `title` TEXT, PRIMARY KEY(`tabId`))")
                database.execSQL("CREATE INDEX `index_tabs_tabId` on `tabs` (tabId)")
                database.execSQL("CREATE TABLE `tab_selection` (`id` INTEGER NOT NULL, `tabId` TEXT, PRIMARY KEY(`id`), FOREIGN KEY(`tabId`) REFERENCES `tabs`(`tabId`) ON UPDATE NO ACTION ON DELETE SET NULL)")
                database.execSQL("CREATE INDEX `index_tab_selection_tabId` on `tab_selection` (tabId)")
            }
        }

        val MIGRATION_2_TO_3: Migration = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE `site_visited` (`domain` TEXT NOT NULL, PRIMARY KEY(`domain`))")
                database.execSQL("DELETE FROM `network_leaderboard`")
            }
        }

        val MIGRATION_3_TO_4: Migration = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE `https_upgrade_domain`")
                database.execSQL("CREATE TABLE `https_bloom_filter_spec` (`id` INTEGER NOT NULL, `errorRate` REAL NOT NULL, `totalEntries` INTEGER NOT NULL, `sha256` TEXT NOT NULL, PRIMARY KEY(`id`))")
                database.execSQL("CREATE TABLE `https_whitelisted_domain` (`domain` TEXT NOT NULL, PRIMARY KEY(`domain`))")
            }
        }

        val MIGRATION_4_TO_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("ALTER TABLE `tabs` ADD COLUMN `viewed` INTEGER NOT NULL DEFAULT 1")
                database.execSQL("ALTER TABLE `tabs` ADD COLUMN `position` INTEGER NOT NULL DEFAULT 0")

                database.query("SELECT `tabId` from `tabs`").use {
                    if (it.moveToFirst()) {
                        var index = 0
                        do {
                            val tabId = it.getString(it.getColumnIndex("tabId"))
                            database.execSQL("UPDATE `tabs` SET position=$index where `tabId` = \"$tabId\"")
                            index += 1

                        } while (it.moveToNext())
                    }
                }
            }
        }

        val MIGRATION_5_TO_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `entity_list` (`entityName` TEXT NOT NULL, `domainName` TEXT NOT NULL, PRIMARY KEY(`domainName`))")
            }
        }

        val MIGRATION_6_TO_7: Migration = object : Migration(6, 7) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `survey` (`surveyId` TEXT NOT NULL, `url` TEXT, `daysInstalled` INTEGER, `status` TEXT NOT NULL, PRIMARY KEY(`surveyId`))")
            }
        }

        val MIGRATION_7_TO_8: Migration = object : Migration(7, 8) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `dismissed_cta` (`ctaId` TEXT NOT NULL, PRIMARY KEY(`ctaId`))")
            }
        }

        val MIGRATION_8_TO_9: Migration = object : Migration(8, 9) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE TABLE IF NOT EXISTS `search_count` (`key` TEXT NOT NULL, `numberSearchesMade` INTEGER NOT NULL, PRIMARY KEY(`key`))")
                database.execSQL("CREATE TABLE IF NOT EXISTS `app_days_used` (`date` TEXT NOT NULL, PRIMARY KEY(`date`))")
            }
        }
    }
}