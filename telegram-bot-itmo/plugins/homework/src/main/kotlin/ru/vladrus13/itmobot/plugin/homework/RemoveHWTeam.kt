package ru.vladrus13.itmobot.plugin.homework

import org.jetbrains.exposed.sql.and
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.utils.Utils
import java.util.*
import javax.xml.crypto.Data

class RemoveHWTeam(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp(): String = "Удаление команд"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val list = if (user.path.getData("selectedTeam") == null) {
            Utils.splitBy(
                HomeworkPlugin.TeamRoleDatabase.getAllByFilter {
                    (HomeworkPlugin.TeamRoleTable.userId eq user.chatId) and
                            (HomeworkPlugin.TeamRoleTable.role eq 3)
                }
                    .mapNotNull { HomeworkPlugin.TeamDatabase.getById(it.teamId)?.name }
            )
        } else {
            val team = HomeworkPlugin.TeamDatabase.getById(user.path.getData("selectedTeam")!!.toLong())!!
            val teamName = team.name
            val letters = teamName.split("").toMutableList()
            letters.shuffle()
            val newName = letters.joinToString(separator = "")
            if (newName == teamName) {
                arrayListOf()
            } else {
                arrayListOf(KeyboardRow().apply { add(newName) })
            }
        }
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        if (user.path.getData("selectedTeam") != null) {
            val team = HomeworkPlugin.TeamDatabase.getById(user.path.getData("selectedTeam")!!.toLong())!!
            if (update.message.text == team.name) {
                val users = HomeworkPlugin.TeamRoleDatabase.getByTeamId(team.id)
                HomeworkPlugin.TeamRoleDatabase.deleteByTeamId(team.id)
                HomeworkPlugin.TeamDatabase.deleteByTeamId(team.id)
                HomeworkPlugin.TeamTaskDatabase.deleteByTeamId(team.id)
                user.path.setPath(parent.path)
                user.send(
                    bot = bot,
                    text = "Успешно! Происходит отправка о роспуске всем участникам...",
                    replyKeyboard = parent.getReplyKeyboard(user)
                )
                users.forEach { role ->
                    DataBase.get<User>(role.userId).send(
                        bot = bot,
                        text = "Ваша команда под названием \"${team.name}\" распущена!"
                    )
                }
            } else {
                user.path.setPath(parent.path)
                user.send(
                    bot = bot,
                    text = "Неверно!",
                    replyKeyboard = parent.getReplyKeyboard(user)
                )
            }
        } else {
            val teamName = update.message.text
            val team = HomeworkPlugin.TeamDatabase.getByName(teamName)
            if (team == null) {
                user.send(
                    bot = bot,
                    text = "НЕКОрректное имя группы"
                )
            } else {
                user.path.setData("selectedTeam", team.id.toString())
                user.send(
                    bot = bot,
                    text = "ВНИМАНИЕ! Это действие удалит группу, всех ее участников из нее, и все домашние задания из нее. Данное действие отменить будет невозможно!"
                )
                user.send(
                    bot = bot,
                    text = "Если хотите продолжить, введите имя группы еще раз",
                    replyKeyboard = getReplyKeyboard(user)
                )
            }
        }
    }

    override val name: String = "Роспуск команды"
    override val systemName: String = "removeHW"

    override fun isAccept(update: Update): Boolean =
        update.message.text == name
}