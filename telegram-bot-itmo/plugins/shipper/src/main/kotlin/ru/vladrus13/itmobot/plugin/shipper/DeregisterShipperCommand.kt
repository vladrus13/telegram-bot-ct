package ru.vladrus13.itmobot.plugin.shipper

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase

class DeregisterShipperCommand(override val parent: Menu) : Command() {
    override fun help(): String = "Удаление из шипера"

    override val name: String
        get() = "Дерегистрация из шипера"
    override val systemName: String
        get() = "deregister"

    override fun isAccept(update: Update): Boolean = update.message.text!! == name


    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Регистрация работает только в группах!"
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        val group = DataBase.get<ShipperGroup>(chat.chatId)
        if (!group.users.contains(user.chatId)) {
            chat.send(
                bot = bot,
                text = "Вы и так не участвуете("
            )
        } else {
            group.users.remove(user.chatId)
            DataBase.put(chat.chatId, group)
            chat.send(
                bot = bot,
                text = "Вы успешно удалены! ШиперБот вас больше не потревожит в этой группе!"
            )
        }
    }
}