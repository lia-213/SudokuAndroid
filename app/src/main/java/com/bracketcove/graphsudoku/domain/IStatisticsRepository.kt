package com.bracketcove.graphsudoku.domain

/**
 * Repository interface for managing user statistics, such as best completion times.
 */
interface IStatisticsRepository {
    /**
     * Retrieves the current user statistics.
     *
     * @param onSuccess Callback with the [UserStatistics].
     * @param onError Callback for errors.
     */
    suspend fun getStatistics(
        onSuccess: (UserStatistics) -> Unit,
        onError: (Exception) -> Unit
    )

    /**
     * Updates a specific statistic if the new time is a record (shorter than previous).
     *
     * @param time The completion time in milliseconds.
     * @param diff The difficulty of the completed puzzle.
     * @param boundary The boundary of the completed puzzle.
     * @param onSuccess Callback indicating if a new record was set.
     * @param onError Callback for errors.
     */
    suspend fun updateStatistic(
        time: Long,
        diff: Difficulty,
        boundary: Int,
        onSuccess: (isRecord: Boolean) -> Unit,
        onError: (Exception) -> Unit
    )
}