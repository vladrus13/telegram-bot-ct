package ru.vladrus13.itmobot.plugin.practice.tablemaker

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getBlackColor

/**
 * first, last = [, )
 */
class GridRequestMaker(
    private val sheetId: Int,
    firstRow: Int, lastRow: Int,
    private val firstColumn: Int, private val lastColumn: Int
) {
    val range: GridRange = GridRange()
        .setSheetId(sheetId)
        .setStartRowIndex(firstRow)
        .setEndRowIndex(lastRow)
        .setStartColumnIndex(firstColumn)
        .setEndColumnIndex(lastColumn)

    fun colorizeBorders(): Request {
        val border = Border()
            .setColor(getBlackColor())
            .setWidth(1)
            .setStyle("SOLID")

        val updateBorders = UpdateBordersRequest()
            .setRange(range)
            .setBottom(border)
            .setTop(border)
            .setLeft(border)
            .setRight(border)

        return Request().setUpdateBorders(updateBorders)
    }

    fun setWidth(pixelSize: Int): Request {
        val range = DimensionRange()
            .setSheetId(sheetId)
            .setDimension("COLUMNS")
            .setStartIndex(firstColumn)
            .setEndIndex(lastColumn)
        val properties = DimensionProperties().setPixelSize(pixelSize)
        val fields = "pixelSize"
        val updateDimensionProperties = UpdateDimensionPropertiesRequest()
            .setRange(range)
            .setProperties(properties)
            .setFields(fields)

        return Request().setUpdateDimensionProperties(updateDimensionProperties)
    }

    fun formatCells(): Request {
        val padding = Padding()
            .setBottom(1)
            .setTop(1)
            .setLeft(1)
            .setRight(1)

        val textFormat = TextFormat()
            .setForegroundColor(getBlackColor())

        val format = CellFormat()
            .setHorizontalAlignment("CENTER")
            .setVerticalAlignment("MIDDLE")
            .setPadding(padding)
            .setTextFormat(textFormat)

        val data = CellData()
            .setUserEnteredFormat(format)

        val repeatCell = RepeatCellRequest()
            .setRange(range)
            .setCell(data)
            .setFields("userEnteredFormat(padding,textFormat,horizontalAlignment,verticalAlignment)")

        return Request().setRepeatCell(repeatCell)
    }

    companion object {
        private data class SheetTable(
            val serviceHash: Sheets,
            val id: String,
            val title: String
        )

        private val infoToId = mutableMapOf<SheetTable, Int>()

        private const val FARTHEST_COLUMN_INDEX = 1000

        fun getSheetIdFromTitle(service: Sheets, id: String, title: String): Int {
            val sheetTable = SheetTable(service, id, title)
            val result: Int = try {
                infoToId[sheetTable] ?: service.spreadsheets().get(id).execute().sheets
                    .map(Sheet::getProperties)
                    .first { properties -> properties.title == title }
                    .sheetId
            } catch (e: NoSuchElementException) {
                throw IllegalArgumentException("No sheet with current title")
            }
            infoToId[sheetTable] = result
            return result
        }

        fun createGridRequestMaker(
            sheetService: Sheets, id: String,
            sheetTitle: String,
            firstRow: Int, lastRow: Int,
            firstColumn: Int, lastColumn: Int
        ) = GridRequestMaker(
            getSheetIdFromTitle(sheetService, id, sheetTitle), firstRow, lastRow, firstColumn, lastColumn
        )

        fun createGridRequestMaker(
            service: Sheets, id: String,
            title: String,
            rectangle: Rectangle
        ) = createGridRequestMaker(
            service,
            id,
            title,
            rectangle.firstRow,
            rectangle.lastRow,
            rectangle.firstColumn,
            rectangle.lastColumn
        )

        /**
         * This function return correct range string
         *
         * @param title is $title
         * @param firstRow in [0, t)
         * @param lastRow in [0, t)
         * @param firstColumn in [0, t)
         * @param lastColumn in [0, t)
         * @return "$title![A-Z]+NUM:[A-Z]+NUM", where NUM is natural number in [1, +inf)
         */
        fun getTitlePrettyRange(title: String, firstRow: Int, lastRow: Int, firstColumn: Int, lastColumn: Int) =
            title + "!" + getPrettyRange(firstRow, lastRow, firstColumn, lastColumn)

        fun getPrettyRange(firstRow: Int, lastRow: Int, firstColumn: Int, lastColumn: Int) =
            "${nToAZ(firstColumn)}${firstRow + 1}:${nToAZ(lastColumn - 1)}$lastRow"

        fun getTitlePrettyOnlyRowRange(title: String, firstRow: Int, lastRow: Int) =
            "$title!${firstRow + 1}:$lastRow"

        fun getPrettyLongRowRange(firstRow: Int, lastRow: Int, firstColumn: Int) =
            getPrettyRange(firstRow, lastRow, firstColumn, FARTHEST_COLUMN_INDEX)

        fun getTitlePrettyLongRowRange(title: String, firstRow: Int, lastRow: Int, firstColumn: Int) =
            getTitlePrettyRange(title, firstRow, lastRow, firstColumn, FARTHEST_COLUMN_INDEX)

        fun getTitlePrettyCell(title: String, row: Int, column: Int) = "$title!${nToAZ(column)}${row + 1}"

        fun nToAZ(n: Int): String {
            if (n < 0) return ""
            return nToAZ(n / 26 - 1) + (65 + (n % 26)).toChar()
        }
    }
}