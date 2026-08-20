package com.bracketcove.graphsudoku.common

import android.app.Activity
import android.widget.Toast
import com.bracketcove.graphsudoku.R
import com.bracketcove.graphsudoku.domain.Difficulty
import com.bracketcove.graphsudoku.domain.SudokuNode
import java.util.LinkedList


/**
 * Displays a toast message within an [Activity].
 *
 * @param message The text to display in the toast.
 */
internal fun Activity.makeToast(message:String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_LONG
    ).show()
}

/**
 * Extension property for [Long] representing seconds, converting it to a formatted time string "MM:SS".
 * If the time is 1 hour or more, it returns "+59:59".
 *
 * @return A formatted time string.
 */
internal fun Long.toTime(): String {
    // "this" refers to the Long object that we will be calling .toTime() on
    if (this >= 3600) return "+59:59"
    var minutes = ((this % 3600) / 60).toString()
    if (minutes.length == 1) minutes = "0$minutes"
    var seconds = (this % 60).toString()
    if (seconds.length == 1) seconds = "0$seconds"
    return String.format("$minutes:$seconds")
}

/**
 * Extension property for [Difficulty] to get the corresponding localized string resource ID.
 */
internal val Difficulty.toLocalizedResource: Int
    get() {
        return when (this) {
            Difficulty.EASY -> R.string.easy
            Difficulty.MEDIUM -> R.string.medium
            Difficulty.HARD -> R.string.hard
        }
    }

/**
 * Merges a list of [SudokuNode] into this [LinkedList], ensuring no duplicate nodes are added.
 *
 * @param other The list of nodes to merge.
 */
internal fun LinkedList<SudokuNode>.mergeWithoutRepeats(other: List<SudokuNode>) {
    other.forEach {
        if (!this.contains(it)) this.add(it)
    }
}

/**
 * Extension property for [Int] to calculate its integer square root.
 */
internal val Int.sqrt: Int
    get() = kotlin.math.sqrt(this.toDouble()).toInt()

