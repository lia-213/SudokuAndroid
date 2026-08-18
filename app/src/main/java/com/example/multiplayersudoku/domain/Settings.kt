package com.example.multiplayersudoku.domain

//a data model (poko - plain old kotlin object)
//containing both difficult and boundary of size of the sudoku puzzle
//4x4 has boundary of 4

//data keyword (before class)generates some helper methods: e.g. equals, hashcode, copy
data class Settings(
    val difficulty: Difficulty,
    val boundary: Int
)
