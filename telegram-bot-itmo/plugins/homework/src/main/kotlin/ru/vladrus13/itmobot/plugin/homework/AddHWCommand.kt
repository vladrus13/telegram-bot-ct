package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.and
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.ForwardMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.utils.Utils

class AddHWCommand(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp(): String = "Пункт добавления ДЗ"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        if (user.path.getData("teamId") == null) {
            val list = Utils.splitBy(
                HomeworkPlugin.TeamRoleDatabase.getAllByFilter {
                    (HomeworkPlugin.TeamRoleTable.userId eq user.chatId) and
                            (HomeworkPlugin.TeamRoleTable.role neq 1)
                }
                    .mapNotNull { HomeworkPlugin.TeamDatabase.getById(it.teamId)?.name }
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

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (user.path.getData("teamId") == null) {
            val name = update.message.text
            val team = HomeworkPlugin.TeamDatabase.getByName(name)
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
                HomeworkPlugin.TeamTaskDatabase.put(teamId, "${user.chatId}#${listOfMessages.joinToString(separator = "|")}")
                val roles = HomeworkPlugin.TeamRoleDatabase.getByTeamId(teamId)
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
                user.path.setPath(parent.path)
                user.send(
                    bot = bot,
                    text = "Успешно завершено!",
                    replyKeyboard = parent.getReplyKeyboard(user)
                )
            } else {
                val messages = user.path.getData("messages") ?: ""
                user.path.setData("messages", messages + "|${update.message.messageId}")
                user.path.setData("chatId", update.message.chatId.toString())
            }
        }
    }

    override val name: String = "Добавление ДЗ"
    override val systemName: String = "addHW"

    override fun isAccept(update: Update): Boolean =
        update.message.text == name

}