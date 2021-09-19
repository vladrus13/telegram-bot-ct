package ru.vladrus13.itmobot.bean

class Settings(byte: Int = 0) {
    var isShowEnd = getBit(byte, 0)
    var isShowTeacher = getBit(byte, 1)
    var isCompact = getBit(byte, 2)

    private fun getBit(n: Int, i: Int): Boolean {
        return ((n shr i) and 1) == 1
    }

    private fun getBool(b: Boolean): Int {
        return if (b) 1 else 0
    }

    fun getByte(): Int {
        return getBool(isShowEnd) * 1 + getBool(isShowTeacher) * 2 + getBool(isCompact) * 4
    }

    fun getStatus(): Collection<String> {
        return listOf(
            "Показывать конец пары (${if (isShowEnd) "включено" else "выключено"})",
            "Показывать преподавателя (${if (isShowTeacher) "включено" else "выключено"})",
            "Показывать компактно (${if (isCompact) "включено" else "выключено"})"
        )
    }
}