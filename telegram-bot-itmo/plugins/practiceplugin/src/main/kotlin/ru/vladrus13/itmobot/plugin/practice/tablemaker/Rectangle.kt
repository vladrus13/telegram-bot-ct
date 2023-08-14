package ru.vladrus13.itmobot.plugin.practice.tablemaker

import com.google.api.services.sheets.v4.model.Request

data class Rectangle(
    val firstRow: Int,
    val lastRow: Int,
    val firstColumn: Int,
    val lastColumn: Int,
)

data class FormattedRectangle(
    val rectangle: Rectangle,
    val actions: List<(GridRequestMaker) -> Request>
)
