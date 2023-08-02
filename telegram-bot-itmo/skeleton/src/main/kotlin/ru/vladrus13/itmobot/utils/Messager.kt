package ru.vladrus13.itmobot.utils

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import java.nio.file.Path

class Messager {
    companion object {

        fun sendMessage(
            bot: TelegramLongPollingBot,
            chatId: Long,
            text: String,
            replyKeyboardMarkup: ReplyKeyboard? = null,
            other: ((SendMessage) -> Unit)? = null
        ) {
            val sendMessage = SendMessage()
            sendMessage.chatId = chatId.toString()
            sendMessage.text = text
            if (replyKeyboardMarkup != null) {
                sendMessage.replyMarkup = replyKeyboardMarkup
            }
            if (other != null) {
                other(sendMessage)
            }
            bot.execute(sendMessage)
        }

        fun sendPhoto(
            bot: TelegramLongPollingBot,
            chatId: Long,
            image: Path,
            text: String? = null,
            replyKeyboardMarkup: ReplyKeyboard? = null,
            other: ((SendPhoto) -> Unit)? = null
        ) {
            val sendMessage = SendPhoto()
            sendMessage.chatId = chatId.toString()
            if (text != null) sendMessage.caption = text
            if (replyKeyboardMarkup != null) sendMessage.replyMarkup = replyKeyboardMarkup
            sendMessage.photo = InputFile(image.toFile())
            if (other != null) {
                other(sendMessage)
            }
            bot.execute(sendMessage)
        }

        fun getMessage(chatId: Long, text: String, replyKeyboardMarkup: ReplyKeyboard? = null): SendMessage {
            val sendMessage = SendMessage()
            sendMessage.chatId = chatId.toString()
            sendMessage.text = text
            if (replyKeyboardMarkup != null) {
                sendMessage.replyMarkup = replyKeyboardMarkup
            }
            return sendMessage
        }
    }
}