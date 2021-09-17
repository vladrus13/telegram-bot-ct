package ru.vladrus13.itmobot.plugins.shipperPlugin

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase

class RegisterShipperCommand(override val parent: Menu) : Command() {
    override fun help(): String = "Регистрация в шипербота"

    override val name: String
        get() = "Регистрация"
    override val systemName: String
        get() = "register"

    override fun isAccept(update: Update): Boolean = update.message.text!! == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Регистрация работает только в группах!"
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        val group = DataBase.get<ShipperGroup>(chat.chatId)
        if (group.users.contains(user.chatId)) {
            chat.send(
                bot = bot,
                text = "Вы уже зарегистрированы! Или же... вы настолько хотите найти себе пару? O Kawaii Koto..."
            )
        } else {
            group.users.add(user.chatId)
            DataBase.put(chat.chatId, group)
            chat.send(
                bot = bot,
                text = "Вы успешно зарегистрированы! Ожидайте пары!"
            )
        }
    }
}