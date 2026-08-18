package com.bracketcove.graphsudoku.domain

// in Kotlin and Java, enum classes are useful for creating a restricted set of values
// can greatly improve the legibility of the code
enum class Difficulty(val modifier: Double) {
    EASY(0.5),
    MEDIUM(0.44),
    HARD(0.38)
}