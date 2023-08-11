package ru.vladrus13.itmobot.plugin.practice.tablemaker

import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getBlackColor

/**
 * first, last = [, )
 */
class GridRequestMaker(
    sheetId: Int,
    firstRow: Int, lastRow: Int,
    firstColumn: Int, lastColumn: Int
) {
    val range: GridRange = GridRange()
        .setSheetId(sheetId)
        .setStartRowIndex(firstRow)
        .setEndRowIndex(lastRow)
        .setStartColumnIndex(firstColumn)
        .setEndColumnIndex(lastColumn)

    fun colorizeBordersAndFormatCells(): List<Request> = listOf(colorizeBorders(), formatCells())

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
        private fun getSheetIdFromTitle(sheetsService: Sheets, id: String, title: String): Int {
            for (sheet in sheetsService.spreadsheets().get(id).execute().sheets) {
                if (sheet.properties.title == title)
                    return sheet.properties.sheetId
            }
            throw IllegalArgumentException("No sheet with current title")
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

        fun getRequests(
            service: Sheets,
            id: String,
            title: String,
            vararg updateCells: Rectangle
        ): List<Request> =
            updateCells.map {
                rect -> rect.actions.map{ action -> action(createGridRequestMaker(service, id, title, rect)) }
            }.flatten()

        /**
         * This function return correct range string
         *
         * @param sheetTitle is $title
         * @param firstRow in [0, t)
         * @param lastRow in [0, t)
         * @param firstColumn in [0, t)
         * @param lastColumn in [0, t)
         * @return "$title![A-Z]+NUM:[A-Z]+NUM", where NUM is natural number in [1, +inf)
         */
        fun getPrettyRange(
            sheetTitle: String,
            firstRow: Int, lastRow: Int,
            firstColumn: Int, lastColumn: Int
        ): String = "$sheetTitle!${nToAZ(firstColumn)}${firstRow + 1}:${nToAZ(lastColumn - 1)}${lastRow}"

        fun nToAZ(n: Int): String {
            if (n < 0) return ""
            return nToAZ(n / 26 - 1) + (65 + (n % 26)).toChar()
        }
    }
}