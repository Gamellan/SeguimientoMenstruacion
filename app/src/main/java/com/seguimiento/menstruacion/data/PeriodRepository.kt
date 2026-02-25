package com.seguimiento.menstruacion.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

data class PeriodRecord(
    val id: Long,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val flowLevel: String,
    val symptoms: List<String>,
    val painLevel: Int,
    val notes: String
)

data class PeriodPredictions(
    val nextPeriodDate: LocalDate?,
    val ovulationDate: LocalDate?,
    val averageCycleLengthDays: Int
)

class PeriodRepository(
    private val dao: PeriodRecordDao
) {
    fun observeRecords(): Flow<List<PeriodRecord>> {
        return dao.observeAll().map { entities ->
            entities.map { entity ->
                PeriodRecord(
                    id = entity.id,
                    startDate = LocalDate.parse(entity.startDate),
                    endDate = LocalDate.parse(entity.endDate),
                    flowLevel = entity.flowLevel,
                    symptoms = entity.symptoms.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                    painLevel = entity.painLevel,
                    notes = entity.notes
                )
            }
        }
    }

    suspend fun addRecord(record: PeriodRecord) {
        dao.insert(
            PeriodRecordEntity(
                id = record.id,
                startDate = record.startDate.toString(),
                endDate = record.endDate.toString(),
                flowLevel = record.flowLevel,
                symptoms = record.symptoms.joinToString(","),
                painLevel = record.painLevel,
                notes = record.notes
            )
        )
    }

    fun buildPredictions(records: List<PeriodRecord>): PeriodPredictions {
        if (records.isEmpty()) {
            return PeriodPredictions(
                nextPeriodDate = null,
                ovulationDate = null,
                averageCycleLengthDays = 28
            )
        }

        val sorted = records.sortedBy { it.startDate }
        val averageCycle = if (sorted.size >= 2) {
            val deltas = sorted.zipWithNext { first, second ->
                ChronoUnit.DAYS.between(first.startDate, second.startDate).toInt().coerceAtLeast(1)
            }
            deltas.average().roundToInt().coerceIn(20, 45)
        } else {
            28
        }

        val lastStart = sorted.last().startDate
        val nextPeriod = lastStart.plusDays(averageCycle.toLong())
        val ovulation = nextPeriod.minusDays(14)

        return PeriodPredictions(
            nextPeriodDate = nextPeriod,
            ovulationDate = ovulation,
            averageCycleLengthDays = averageCycle
        )
    }
}
