package com.liuflow.app.data.export

import android.content.Context
import android.net.Uri
import com.liuflow.app.data.db.SessionEntity
import com.liuflow.app.util.DateUtils
import com.opencsv.CSVWriter
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object DataExporter {

    @Serializable
    private data class ExportPayload(
        val version: Int = 1,
        val exportedAt: Long,
        val sessions: List<ExportSession>,
    )

    @Serializable
    private data class ExportSession(
        val id: String,
        val task: String,
        val category: String?,
        val plannedDuration: Int,
        val actualDuration: Int,
        val status: String,
        val startedAt: Long,
        val endedAt: Long,
        val date: String,
        val hour: Int,
        val weekday: Int,
    )

    fun writeJson(context: Context, uri: Uri, sessions: List<SessionEntity>) {
        val payload = ExportPayload(
            exportedAt = System.currentTimeMillis(),
            sessions = sessions.map { it.toExport() },
        )
        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            OutputStreamWriter(out, StandardCharsets.UTF_8).use { w ->
                w.write(Json { prettyPrint = true }.encodeToString(payload))
            }
        }
    }

    fun writeCsv(context: Context, uri: Uri, sessions: List<SessionEntity>) {
        context.contentResolver.openOutputStream(uri, "wt")?.use { out ->
            // BOM for Excel UTF-8 detection
            out.write(byteArrayOf(0xEF.toByte(), 0xBB.toByte(), 0xBF.toByte()))
            OutputStreamWriter(out, StandardCharsets.UTF_8).use { writer ->
                CSVWriter(writer).use { csv ->
                    csv.writeNext(arrayOf(
                        "id", "task", "category", "planned_minutes", "actual_minutes",
                        "status", "started_at", "ended_at", "date", "hour", "weekday",
                    ))
                    sessions.forEach { s ->
                        csv.writeNext(arrayOf(
                            s.id,
                            s.task,
                            s.category ?: "",
                            s.plannedDuration.toString(),
                            s.actualDuration.toString(),
                            s.status,
                            DateUtils.formatTime(s.startedAt),
                            DateUtils.formatTime(s.endedAt),
                            s.date,
                            s.hour.toString(),
                            s.weekday.toString(),
                        ))
                    }
                }
            }
        }
    }

    private fun SessionEntity.toExport() = ExportSession(
        id = id,
        task = task,
        category = category,
        plannedDuration = plannedDuration,
        actualDuration = actualDuration,
        status = status,
        startedAt = startedAt,
        endedAt = endedAt,
        date = date,
        hour = hour,
        weekday = weekday,
    )
}
