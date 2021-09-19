package ru.vladrus13.itmobot.tables

class ResultPair(
    val tableName: String,
    val name: String,
    var result: String
) {
    fun set(newResult: String): Boolean {
        if (result != newResult) {
            if (!isNew) {
                oldResult = result
                isNew = true
            }
            result = newResult
            return true
        }
        return false
    }

    fun callChangeToString(): String {
        isNew = false
        return if (oldResult == null) {
            "Новое значение: $result"
        } else {
            "${oldResult.toString()} -> $result"
        }
    }

    private var isNew = true
    private var oldResult: String? = null
}