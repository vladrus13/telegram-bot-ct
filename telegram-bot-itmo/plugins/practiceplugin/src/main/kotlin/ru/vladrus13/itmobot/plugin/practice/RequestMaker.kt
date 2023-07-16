package ru.vladrus13.itmobot.plugin.practice

import com.google.api.services.sheets.v4.model.Border
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.GridRange
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.UpdateBordersRequest

class RequestMaker {
    companion object {
        /**
         * first, last = [, )
         */
        fun getRange(sheetId: Int,
                     firstRow: Int, lastRow: Int,
                     firstColumn: Int, lastColumn: Int): GridRange {
            val range = GridRange()
            range.setSheetId(sheetId)
            range.setStartRowIndex(firstRow)
            range.setEndRowIndex(lastRow)
            range.setStartColumnIndex(firstColumn)
            range.setEndColumnIndex(lastColumn)
            return range
        }

        fun getColor(blue: Float, green: Float, red: Float): Color = Color()
            .setBlue(blue)
            .setGreen(green)
            .setRed(red)

        fun getBlackColor() = getColor(0f, 0f, 0f)

        fun updateBorders(sheetId: Int,
                          firstRow: Int, lastRow: Int,
                          firstColumn: Int, lastColumn: Int): Request {
            val border = Border()
                .setColor(getBlackColor())
                .setWidth(1)
                .setStyle("SOLID")

            val updateBorders = UpdateBordersRequest()
                .setRange(getRange(sheetId, firstRow, lastRow, firstColumn, lastColumn))
                .setBottom(border)
                .setTop(border)
                .setLeft(border)
                .setRight(border)

            return Request().setUpdateBorders(updateBorders)
        }
    }
}