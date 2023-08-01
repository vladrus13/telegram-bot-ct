package ru.vladrus13.itmobot.plugin.homework

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Menu
import ru.vladrus13.itmobot.utils.Utils

class LeaveTeamCommand : Menu(arrayOf()) {
    override val menuHelp = "Позволяет покинуть команду"
    override val name = "Покинуть команду"

    override fun getReplyKeyboard(user: User): ReplyKeyboard {
        val teamsForUser = TeamRoleDatabase.getAllTeamsForUser(user.chatId)
        val list : MutableList<KeyboardRow> = Utils.splitBy(
            TeamDatabase.getAllTeamsWithIdFrom(teamsForUser)
        )
        val backRow = KeyboardRow()
        backRow.add("<< Назад")
        list.add(backRow)
        return ReplyKeyboardMarkup(list)
    }

    override fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User): Boolean {
        val teamName = update.message.text
        val team = TeamDatabase.getByName(teamName)
        if (team == null) {
            user.send(
                bot = bot,
                text = "Некорректное имя группы!"
            )
        } else {
            val role = TeamRoleDatabase.get(user.chatId, team.id)
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
                    TeamRoleDatabase.deleteByTeamAndUser(user.chatId, team.id)
                    user.path.myRemoveFromPath()
                    user.send(
                        bot = bot,
                        text = "Вы успешно покинули группу!",
                        replyKeyboard = user.path.myLast().getReplyKeyboard(user)
                    )
                }
            }
        }
        return true
    }
}