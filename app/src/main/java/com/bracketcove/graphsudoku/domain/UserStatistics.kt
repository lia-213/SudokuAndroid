package com.bracketcove.graphsudoku.domain

/**
 * Data model for storing the best completion times for different board sizes and difficulties.
 * All times are in milliseconds. 0 indicates no record set.
 */
data class UserStatistics(
    val fourEasy: Long = 0,
    val fourMedium: Long = 0,
    val fourHard: Long = 0,
    val nineEasy: Long = 0,
    val nineMedium: Long = 0,
    val nineHard: Long = 0
)
