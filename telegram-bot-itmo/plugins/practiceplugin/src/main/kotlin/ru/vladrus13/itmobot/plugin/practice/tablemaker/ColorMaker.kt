package ru.vladrus13.itmobot.plugin.practice.tablemaker

import com.google.api.services.sheets.v4.model.Color

class ColorMaker {
    companion object {
        private fun getColor(red: Float, green: Float, blue: Float): Color = Color()
            .setRed(red)
            .setGreen(green)
            .setBlue(blue)

        fun getGreenCountTasksColor() = getColor(0.34f, 0.73f, 0.539f)
        fun getGreenAcceptedTask() = getColor(0.416f, 0.659f, 0.31f)
        fun getYellowDeclinedTask() = getColor(0.945f, 0.761f, 0.196f)
        fun getBlackColor() = getColor(0f, 0f, 0f)
        fun getWhiteColor() = getColor(1f, 1f, 1f)
    }
}