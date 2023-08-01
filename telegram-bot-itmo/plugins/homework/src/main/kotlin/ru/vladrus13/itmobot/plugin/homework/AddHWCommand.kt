package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.and
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.plugin.homework.TeamRoleDatabase.*
import ru.vladrus13.itmobot.utils.Utils

class AddHWCommand : Menu(arrayOf()) {
    override val menuHelp = "Пункт добавления ДЗ"
    override val name = "Добавление ДЗ"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        if (user.path.getData("teamId") == null) {
            val list = Utils.splitBy(
                TeamRoleDatabase.getAllByFilter {
                    (TeamRoleTable.userId eq user.chatId) and
                            (TeamRoleTable.role neq 1)
                }
                    .mapNotNull { TeamDatabase.getById(it.teamId)?.name }
            )
            val backRow = KeyboardRow()
            backRow.add("<< Назад")
            list.add(backRow)
            replyKeyboardMarkup.keyboard = list
        } else {
            replyKeyboardMarkup.keyboard = listOf(KeyboardRow().apply {
                add("END")
                add("<< Назад")
            })
        }
        return replyKeyboardMarkup
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        if (user.path.getData("teamId") == null) {
            val name = update.message.text
            val team = TeamDatabase.getByName(name)
            if (team == null) {
                user.send(
                    bot = bot,
                    text = "Группа не найдена!"
                )
            } else {
                user.path.setData("teamId", team.id.toString())
                user.send(
                    bot = bot,
                    text = "Теперь отправляйте картинки или текст до тех пор, пока они не закончатся. Как только они закончатся, нажмите END",
                    replyKeyboard = getReplyKeyboard(user)
                )
            }
        } else {
            if (update.message.text == "END") {
                val teamId = user.path.getData("teamId")!!.toLong()
                val messages = user.path.getData("messages") ?: ""
                val listOfMessages = messages.split("|").filter { it.isNotBlank() }
                TeamTaskDatabase.put(teamId, "${user.chatId}#${listOfMessages.joinToString(separator = "|")}")
                val roles = TeamRoleDatabase.getByTeamId(teamId)
                roles.forEach { role ->
                    val sub = DataBase.get<User>(role.userId)
                    sub.send(
                        bot = bot,
                        text = "Новое домашнее задание!"
                    )
                    listOfMessages.forEach { taskId ->
                        val message = ForwardMessage()
                        message.chatId = sub.chatId.toString()
                        message.fromChatId = user.chatId.toString()
                        message.messageId = taskId.toInt()
                        bot.execute(message)
                    }
                }
                user.path.myRemoveFromPath()
                user.send(
                    bot = bot,
                    text = "Успешно завершено!",
                    replyKeyboard = user.path.myLast().getReplyKeyboard(user)
                )
            } else {
                val messages = user.path.getData("messages") ?: ""
                user.path.setData("messages", messages + "|${update.message.messageId}")
                user.path.setData("chatId", update.message.chatId.toString())
            }
        }
        return true
    }
}