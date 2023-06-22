package ru.vladrus13.itmobot.plugin.practice

class Utils {
    companion object {
        fun deleteBrackets(str: String) =
            if (str.length >= 2 && str.first() == '"' && str.last() == '"')
                str.substring(1, str.length - 1)
            else str    }
}