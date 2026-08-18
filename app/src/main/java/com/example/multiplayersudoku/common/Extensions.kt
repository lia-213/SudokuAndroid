package com.example.multiplayersudoku.common

import android.app.Activity
import android.widget.Toast
import com.example.multiplayersudoku.domain.Difficulty
import com.example.multiplayersudoku.domain.SudokuNode
import java.util.LinkedList


internal fun Activity.makeToast(message:String) {
    Toast.makeText(
        this,
        message,
        Toast.LENGTH_LONG
    ).show()
}

internal fun Long.toTime(): String {
    // "this" refers to the Long object that we will be calling .toTime() on
    if (this >= 3600) return "+59:59"
    var minutes = ((this % 3600) / 60).toString()
    if (minutes.length == 1) minutes = "0$minutes"
    var seconds = (this % 60).toString()
    if (seconds.length == 1) seconds = "0$seconds"
    return String.format("$minutes:$seconds")
}

internal val Difficulty.toLocalizedResource: Int
    get() {
        return when (this) {
            Difficulty.EASY -> com.example.multiplayersudoku.R.string.easy
            Difficulty.MEDIUM -> com.example.multiplayersudoku.R.string.medium
            Difficulty.HARD -> com.example.multiplayersudoku.R.string.hard
        }
    }

internal fun LinkedList<SudokuNode>.mergeWithoutRepeats(other: LinkedList<SudokuNode>) {
    other.forEach {
        if (!this.contains(it)) this.add(it)
    }
}
