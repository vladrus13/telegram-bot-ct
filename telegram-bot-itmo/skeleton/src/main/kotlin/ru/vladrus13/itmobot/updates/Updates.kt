package ru.vladrus13.itmobot.updates

import org.telegram.telegrambots.meta.api.methods.send.SendMessage

class Updates {

    companion object {
        // TODO sendMessage to Message
        private val pairs: List<Pair<String, SendMessage.() -> Unit>> = listOf(
            Pair("1.0") {
                text =
                    "05.09.21. Обновление! Теперь для каждой таблицы будет подписано время обновления! Вы всегда сможете посмотреть, насколько стара таблица"
            },
            Pair("1.1") {
                text =
                    "12.09.21. Обновление! Теперь при выборе группы вы автоматически будете выходить из папки выбора групп! Идея: @staszw"
            },
            Pair("1.2") {
                text =
                    "21.09.21. Ошибки русского языка были исправлены (спасибо @donRumata03). Теперь код выложен на https://github.com/vladrus13/telegram-bot-ct и вы сможете писать плагины для ботов! Скоро там появится документация"
            }
        )

        fun getUpdates(last: String): List<Pair<String, SendMessage.() -> Unit>> {
            pairs.forEachIndexed { i, pair ->
                if (last == pair.first) {
                    return pairs.subList(i + 1, pairs.size)
                }
            }
            return emptyList()
        }

        fun getLast(): String {
            return pairs.last().first
        }
    }
}