package ru.vladrus13.itmobot.plugin.practice.tablemaker

import com.google.api.services.sheets.v4.model.Color

class ColorMaker {
    companion object {
        private fun getColor(blue: Float, green: Float, red: Float): Color = Color()
            .setBlue(blue)
            .setGreen(green)
            .setRed(red)

        fun getGreenCountTasksColor() = getColor(0.539f, 0.73f, 0.34f)
        fun getBlackColor() = getColor(0f, 0f, 0f)
        fun getWhiteColor() = getColor(1f, 1f, 1f)
    }
}