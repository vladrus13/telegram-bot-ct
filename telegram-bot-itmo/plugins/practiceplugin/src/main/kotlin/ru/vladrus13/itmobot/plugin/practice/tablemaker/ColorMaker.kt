package ru.vladrus13.itmobot.plugin.practice.tablemaker

import com.google.api.services.sheets.v4.model.Color

class ColorMaker {
    companion object {
        fun getColor(blue: Float, green: Float, red: Float): Color = Color()
            .setBlue(blue)
            .setGreen(green)
            .setRed(red)

        fun getBlackColor() = getColor(0f, 0f, 0f)
    }
}