package com.bracketcove.graphsudoku.persistence

import android.util.Log
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import com.bracketcove.graphsudoku.GameSettings
import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.SettingsStorageResult
import com.bracketcove.graphsudoku.domain.Settings
import com.bracketcove.graphsudoku.domain.ISettingsStorage
import com.google.protobuf.InvalidProtocolBufferException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream

/**
 * Implementation of [ISettingsStorage] backed by a Proto DataStore, converting between the
 * generated [GameSettings] proto and the domain [Settings] model.
 *
 * Note: GameSettings is a class generated for Proto Datastore
 */
class LocalSettingsStorageImpl(
    private val dataStore: DataStore<GameSettings>
) : ISettingsStorage {
    /**
     * Reads the current [GameSettings] from the DataStore and maps it to the domain [Settings].
     *
     * @return A [SettingsStorageResult] containing the settings or an error.
     */
    override suspend fun getSettings(): SettingsStorageResult =
        withContext(Dispatchers.IO) {
            try {
                val gameSettings = dataStore.data.first()
                SettingsStorageResult.OnSuccess(gameSettings.toSettings)
            } catch (e: Exception) {
                SettingsStorageResult.OnError(e)
            }
        }


    /**
     * Writes the given [Settings] to the DataStore by updating the boundary and difficulty
     * fields of the stored [GameSettings] proto.
     *
     * @param settings The new [Settings] to save.
     * @return A [SettingsStorageResult] indicating success or failure.
     */
    override suspend fun updateSettings(settings: Settings): SettingsStorageResult =
        withContext(Dispatchers.IO) {
            try {
                dataStore.updateData { gameSettings ->
                    gameSettings.toBuilder()
                        .setBoundary(settings.boundary)
                        .setDifficulty(settings.difficulty.toProto)
                        .build()
                }

                SettingsStorageResult.OnComplete

            } catch (e: Exception) {
                SettingsStorageResult.OnError(e)
            }
        }
}

/**
 * Converts a generated [GameSettings] proto into the domain [Settings] model.
 */
private val GameSettings.toSettings: Settings
    get() = Settings(
        this.difficulty.toDomain,
        this.boundary.verify()
    )

/**
 * Maps a [GameSettings.ProtoDifficulty] proto enum value to the domain [Difficulty].
 */
private val GameSettings.ProtoDifficulty.toDomain: Difficulty
    get() = when (this.number) {
        1 -> Difficulty.EASY
        2 -> Difficulty.MEDIUM
        3 -> Difficulty.HARD
        else -> Difficulty.MEDIUM
    }

/**
 * Maps a domain [Difficulty] to its [GameSettings.ProtoDifficulty] proto enum value.
 */
private val Difficulty.toProto: GameSettings.ProtoDifficulty
    get() =  when (this) {
        Difficulty.EASY -> GameSettings.ProtoDifficulty.EASY
        Difficulty.MEDIUM -> GameSettings.ProtoDifficulty.MEDIUM
        Difficulty.HARD -> GameSettings.ProtoDifficulty.HARD
    }


/**
 * Ensures a boundary value is one of the supported puzzle sizes (4, 9, 16), defaulting to 4
 * if the stored value is unrecognized.
 *
 * @return The verified boundary value.
 */
private fun Int.verify(): Int {
    return when (this) {
        4 -> this
        9 -> this
        16 -> this
        else -> 4
    }
}