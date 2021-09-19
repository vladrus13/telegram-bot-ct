package ru.vladrus13.itmobot.plugin.shipper

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import kotlin.random.Random

class ShipperShipperCommand(override val parent: Menu) : Command() {
    override fun help(): String = "Вызов команды шипа"

    val table = ShipperPairParser()

    override val name: String = "Шиппер!"
    override val systemName: String = "ship"

    override fun isAccept(update: Update): Boolean = update.message.text!! == name

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = "Шипперинг работает только в группах!"
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        val group = DataBase.get<ShipperGroup>(chat.chatId)
        if (group.users.size == 0) {
            chat.send(
                bot = bot,
                text = "Тут нет никого, кто бы хотел оказаться в паре("
            )
            return
        }
        if (group.users.size == 1) {
            chat.send(
                bot = bot,
                text = "Есть лишь один человек, который хочет быть в паре!"
            )
            return
        }
        val random = Random(System.currentTimeMillis())
        val firstIndex = random.nextInt(group.users.size)
        var secondIndex = random.nextInt(group.users.size - 1)
        if (secondIndex >= firstIndex) secondIndex++
        val first = DataBase.get<User>(group.users[firstIndex])
        val second = DataBase.get<User>(group.users[secondIndex])
        table.put(ShipperPair(chat.chatId, first.chatId, second.chatId))
        val firstIndicator = if (first.username == null) {
            "chatId:${first.chatId}"
        } else {
            "@${first.username}"
        }
        val secondIndicator = if (second.username == null) {
            "chatId:${second.chatId}"
        } else {
            "@${second.username}"
        }
        chat.send(
            bot = bot,
            text = "Тестовая пара этого дня: $firstIndicator и $secondIndicator"
        )
    }
}