package ru.vladrus13.itmobot.utils

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

class Utils {
    companion object {
        fun equals(a: ArrayList<String>, b: ArrayList<String>): Boolean {
            if (a.size != b.size) return false
            for (i in a.indices) {
                if (a[i] != b[i]) return false
            }
            return true
        }

        fun splitBy(values: Collection<String>, splitter: Int = 2): ArrayList<KeyboardRow> {
            val returned = ArrayList<KeyboardRow>()
            val iterator = values.iterator()
            while (iterator.hasNext()) {
                val keyboardRow = KeyboardRow()
                for (i in 0 until splitter) {
                    if (iterator.hasNext()) {
                        keyboardRow.add(iterator.next())
                    } else {
                        break
                    }
                }
                returned.add(keyboardRow)
            }
            return returned
        }
    }
}