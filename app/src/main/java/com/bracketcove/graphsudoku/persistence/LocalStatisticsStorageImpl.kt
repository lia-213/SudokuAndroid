package com.bracketcove.graphsudoku.persistence

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.bracketcove.graphsudoku.GameSettings
import com.bracketcove.graphsudoku.Statistics
import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.IStatisticsRepository
import com.bracketcove.graphsudoku.domain.SettingsStorageResult
import com.bracketcove.graphsudoku.domain.UserStatistics
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/**
 * Implementation of [IStatisticsRepository] backed by a Proto DataStore, converting between
 * the generated [Statistics] proto and the domain [UserStatistics] model.
 */
class LocalStatisticsStorageImpl(
    private val dataStore: DataStore<Statistics>
) : IStatisticsRepository {
    /**
     * Reads the current [Statistics] from the DataStore and maps it to the domain
     * [UserStatistics].
     *
     * @param onSuccess Callback with the [UserStatistics].
     * @param onError Callback for errors.
     */
    override suspend fun getStatistics(
        onSuccess: (UserStatistics) -> Unit,
        onError: (Exception) -> Unit
    ) = withContext(Dispatchers.IO) {
        try {
            val stats = dataStore.data.first()

            onSuccess(
                stats.toUserStatistics
            )
        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Compares the given completion [time] against the stored best time for the matching
     * [diff]/[boundary] combination, and overwrites it in the DataStore if the new time is
     * a record (or no record exists yet).
     *
     * @param time The completion time in milliseconds.
     * @param diff The difficulty of the completed puzzle.
     * @param boundary The boundary of the completed puzzle.
     * @param onSuccess Callback indicating if a new record was set.
     * @param onError Callback for errors.
     */
    override suspend fun updateStatistic(
        time: Long,
        diff: Difficulty,
        boundary: Int,
        onSuccess: (isRecord: Boolean) -> Unit,
        onError: (Exception) -> Unit
    ) {
        try {
            val stats = dataStore.data.first()

            val oldTime = stats.findMatch(diff, boundary)

            if (oldTime > time || oldTime == 0L) {
                val userStats = stats.toUserStatistics.updateMatch(time, diff, boundary)
                dataStore.updateData { stats ->
                    stats.toBuilder()
                        .setFourEasy(userStats.fourEasy)
                        .setFourMedium(userStats.fourMedium)
                        .setFourHard(userStats.fourHard)
                        .setNineEasy(userStats.nineEasy)
                        .setNineMedium(userStats.nineMedium)
                        .setNineHard(userStats.nineHard)
                        .build()
                }

                onSuccess(true)
            } else {
                onSuccess(false)
            }

        } catch (e: Exception) {
            onError(e)
        }
    }

    /**
     * Converts a generated [Statistics] proto into the domain [UserStatistics] model.
     */
    private val Statistics.toUserStatistics: UserStatistics
        get() {
            return UserStatistics(
                fourEasy = this.fourEasy,
                fourMedium = this.fourMedium,
                fourHard = this.fourHard,
                nineEasy = this.nineEasy,
                nineMedium = this.nineMedium,
                nineHard = this.nineHard
            )
        }

    /**
     * Looks up the stored best time for the given difficulty/boundary combination.
     *
     * @param diff The difficulty to look up.
     * @param boundary The puzzle boundary to look up.
     * @return The stored best time in milliseconds.
     */
    private fun Statistics.findMatch(diff: Difficulty, boundary: Int): Long {
        return when {
            diff == Difficulty.EASY && boundary == 4 -> fourEasy
            diff == Difficulty.MEDIUM && boundary == 4 -> fourMedium
            diff == Difficulty.HARD && boundary == 4 -> fourHard

            diff == Difficulty.EASY && boundary == 9 -> nineEasy
            diff == Difficulty.MEDIUM && boundary == 9 -> nineMedium
            diff == Difficulty.HARD && boundary == 9 -> nineHard

            else -> throw IOException()
        }
    }

    /**
     * Returns a copy of this [UserStatistics] with the entry for the given difficulty/boundary
     * combination replaced by [time].
     *
     * @param time The new best time in milliseconds.
     * @param diff The difficulty to update.
     * @param boundary The puzzle boundary to update.
     * @return The updated [UserStatistics].
     */
    private fun UserStatistics.updateMatch(
        time: Long,
        diff: Difficulty,
        boundary: Int
    ): UserStatistics {
        return when {
            diff == Difficulty.EASY && boundary == 4 -> this.copy(fourEasy = time)
            diff == Difficulty.MEDIUM && boundary == 4 -> this.copy(fourMedium = time)
            diff == Difficulty.HARD && boundary == 4 -> this.copy(fourHard = time)

            diff == Difficulty.EASY && boundary == 9 -> this.copy(nineEasy = time)
            diff == Difficulty.MEDIUM && boundary == 9 -> this.copy(nineMedium = time)
            diff == Difficulty.HARD && boundary == 9 -> this.copy(nineHard = time)

            else -> throw IOException()
        }
    }
}