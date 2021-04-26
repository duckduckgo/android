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

package com.duckduckgo.app.bookmarks.service

import android.content.ContentResolver
import android.net.Uri
import com.duckduckgo.app.bookmarks.db.BookmarksDao
import com.duckduckgo.app.global.DefaultDispatcherProvider
import com.duckduckgo.app.global.DispatcherProvider
import kotlinx.coroutines.withContext
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException

interface BookmarksExporter {
    suspend fun export(uri: Uri): ExportBookmarksResult
}

sealed class ExportBookmarksResult {
    object Success : ExportBookmarksResult()
    data class Error(val exception: Exception) : ExportBookmarksResult()
    object NoBookmarksExported : ExportBookmarksResult()
}

class DuckDuckGoBookmarksExporter(
    private val contentResolver: ContentResolver,
    private val bookmarksDao: BookmarksDao,
    private val dispatcher: DispatcherProvider = DefaultDispatcherProvider()
) : BookmarksExporter {

    override suspend fun export(uri: Uri): ExportBookmarksResult {
        val bookmarks = withContext(dispatcher.io()){
            bookmarksDao.bookmarksSync()
        }

        if (bookmarks.isEmpty()) {
            return ExportBookmarksResult.NoBookmarksExported
        }

        val bookmarksHtml = buildString {
            appendLine("<!DOCTYPE NETSCAPE-Bookmark-file-1>")
            appendLine("<!--This is an automatically generated file.")
            appendLine("It will be read and overwritten.")
            appendLine("Do Not Edit! -->")
            appendLine("<META HTTP-EQUIV=\"Content-Type\" CONTENT=\"text/html; charset=UTF-8\">")
            appendLine("<Title>Bookmarks</Title>")
            appendLine("<H1>Bookmarks</H1>")
            appendLine("<DL><p>")
            appendLine("    <DT><H3 ADD_DATE=\"1618844074\" LAST_MODIFIED=\"1618844074\" PERSONAL_TOOLBAR_FOLDER=\"true\">DuckDuckGo</H3>")
            appendLine("    <DL><p>")
            bookmarks.forEach { entity ->
                appendLine("        <DT><A HREF=\"${entity.url}\" ADD_DATE=\"1618844074\" LAST_MODIFIED=\"1618844074\">${entity.title}</A>")
            }
            appendLine("    </DL><p>")
            appendLine("</DL><p>")
        }
        return writeFileContent(uri, bookmarksHtml)
    }

    private fun writeFileContent(uri: Uri, content: String): ExportBookmarksResult {
        return try {
            val file = contentResolver.openFileDescriptor(uri, "w")
            if (file != null) {
                val fileOutputStream = FileOutputStream(file.fileDescriptor)
                fileOutputStream.write(content.toByteArray())
                fileOutputStream.close()
                file.close()
                ExportBookmarksResult.Success
            } else {
                ExportBookmarksResult.NoBookmarksExported
            }
        } catch (e: FileNotFoundException) {
            ExportBookmarksResult.Error(e)
        } catch (e: IOException) {
            ExportBookmarksResult.Error(e)
        }
    }


}