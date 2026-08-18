/*
A node in a sudoku puzzle is denoted by:
- a value or colour, which is an integer denoted by the set of all numbers in the sudoku game
- a list of relative x and y values, where:
----- top left = x0, y0 (assuming 0 based indexing)
----- bottom right = xn-1, yn-1, where n is the largest number in allowed numbers
 */

// TODO: Serializable seems to allow us to read and write our sudokunodes, and also the
//  whole puzzle to a file. Since we are only storing one at a time at thi point.
//  will have to change this for a db!!
package com.example.multiplayersudoku.domain

import java.io.Serializable

data class SudokuNode(
    val x: Int,
    val y: Int,
    var color: Int = 0,
    var readOnly: Boolean = true,
): Serializable {
    override fun hashCode(): Int {
        return getHash(x, y)
    }
}

internal fun getHash(x: Int, y: Int): Int {
    // multiply x by 100 to mitigate for edge cases in larger boards than 9x9 (e.g. 16x16)
    // making the hashcode unique for every cell in the puzzle
    val newX = x*100
    return "$newX$y".toInt()
}
