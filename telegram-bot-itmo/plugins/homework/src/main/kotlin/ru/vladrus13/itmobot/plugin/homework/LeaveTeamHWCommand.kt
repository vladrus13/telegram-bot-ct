package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.utils.Utils

class LeaveTeamHWCommand(override val parent: Menu) : Menu(parent) {
    override val childes: Array<Foldable> = arrayOf()

    override fun menuHelp(): String = "Позволяет покинуть команду"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val replyKeyboardMarkup = ReplyKeyboardMarkup()
        val rolesOfUser = HomeworkPlugin.TeamRoleDatabase.getAllByFilter { HomeworkPlugin.TeamRoleTable.userId eq user.chatId }.map { it.teamId }
        val list : MutableList<KeyboardRow> = Utils.splitBy(
                HomeworkPlugin.TeamDatabase.getAll().filter { rolesOfUser.contains(it.id) }.map { it.name }
            )
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        replyKeyboardMarkup.keyboard = list
        return replyKeyboardMarkup
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        if (standardCommand(update, bot, user)) return
        val teamName = update.message.text
        val team = HomeworkPlugin.TeamDatabase.getByName(teamName)
        if (team == null) {
            user.send(
                bot = bot,
                text = "Некорректное имя группы!"
            )
        } else {
            val role = HomeworkPlugin.TeamRoleDatabase.get(user.chatId, team.id)
            if (role == null) {
                user.send(
                    bot = bot,
                    text = "Выйти оттуда, куда не входили... Занятно..."
                )
            } else {
                if (role.role == 3) {
                    user.send(
                        bot = bot,
                        text = "Капитан покидает корабль последним!\nВы можете покинуть группу, только удалив ее"
                    )
                } else {
                    HomeworkPlugin.TeamRoleDatabase.deleteByTeamAndUser(user.chatId, team.id)
                    user.path.setPath(parent.path)
                    user.send(
                        bot = bot,
                        text = "Вы успешно покинули группу!",
                        replyKeyboard = parent.getReplyKeyboard(user)
                    )
                }
            }
        }
    }

    override val name: String = "Покинуть команду"
    override val systemName: String = "leaveTeam"

    override fun isAccept(update: Update): Boolean =
        update.message.text == name
}