package ru.vladrus13.itmobot.plugin.practice.tablemaker

import com.google.api.services.sheets.v4.model.*
import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getBlackColor

/**
 * first, last = [, )
 */
class GridRequestMaker(sheetId: Int,
                       firstRow: Int, lastRow: Int,
                       firstColumn: Int, lastColumn: Int) {
    val range: GridRange = GridRange()
        .setSheetId(sheetId)
        .setStartRowIndex(firstRow)
        .setEndRowIndex(lastRow)
        .setStartColumnIndex(firstColumn)
        .setEndColumnIndex(lastColumn)


        fun updateBorders(): Request {
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

        fun updateCells(): Request {
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
}