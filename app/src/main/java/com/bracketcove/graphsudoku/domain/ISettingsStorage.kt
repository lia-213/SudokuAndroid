package com.bracketcove.graphsudoku.domain

/**
 * Interface for persisting and retrieving user settings.
 */
interface ISettingsStorage {
    /**
     * Retrieves the saved user settings.
     *
     * @return A [SettingsStorageResult] containing the settings or an error.
     */
    suspend fun getSettings(): SettingsStorageResult

    /**
     * Updates the saved user settings.
     *
     * @param settings The new [Settings] to save.
     * @return A [SettingsStorageResult] indicating success or failure.
     */
    suspend fun updateSettings(settings: Settings): SettingsStorageResult
}

/**
 * Sealed class representing the result of a settings storage operation.
 */
sealed class SettingsStorageResult {
    data class OnSuccess(val settings: Settings) : SettingsStorageResult()
    data class OnError(val exception: Exception) : SettingsStorageResult()
    object OnComplete : SettingsStorageResult()
}
