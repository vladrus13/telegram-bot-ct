package ru.vladrus13.itmobot.plugin.practice.tablemaker

import org.junit.jupiter.api.Assertions.assertEquals
import kotlin.test.Test

class GridRequestMakerTest {
    private val testSample = GridRequestMaker.Companion

    @Test
    fun testGetTitleCellForSumS() {
        // arrange
        val name = "Д1"
        val columnIndex = 2
        val rowIndex = 5

        // act
        val result = testSample.getCellWithTitleAndNotChangingColumn(name, rowIndex, columnIndex)

        // assert
        assertEquals("'Д1'!\$C${rowIndex+ 1}", result)
    }

    @Test
    fun testNToAZ() {
        // arrange
        // act
        // assert
        assertEquals("A", testSample.nToAZ(0))
        assertEquals("B", testSample.nToAZ(1))
    }
}
