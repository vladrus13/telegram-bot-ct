package ru.vladrus13.itmobot.command

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import ru.vladrus13.itmobot.bean.User

abstract class Menu(val children: Array<out Foldable>) : Foldable {
    abstract val menuHelp: String

    protected val childrenNames = children.flatMap { name }

    override val help: String

    init {
        val sb = StringBuilder()
        sb.append(menuHelp + "\nКоманды:\n\n")
        for (child in children) {
            sb.append(child.name + ": ")
            when (child) {
                is Menu -> sb.append(child.menuHelp)
                else -> sb.append(child.help)
            }
            sb.append("\n\n")
        }
        help = sb.toString()
    }

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        when (val text = update.message.text!!) {
            "<< Назад" -> onReturnButton(user, bot)
            "Помощь", "Help", "/help" -> user.send(bot, help)
            in childrenNames -> onTransition(text, user, bot, update)
            else -> {
                val customUpdateAccepted = onCustomUpdate(update, bot, user)
                if (!customUpdateAccepted) {
                    onUnknownCommand(user, bot)
                }
            }
        }
    }

    open fun onCustomUpdate(update: Update, bot: TelegramLongPollingBot, user: User) = false

    protected fun onUnknownCommand(
        user: User,
        bot: TelegramLongPollingBot
    ) {
        user.send(bot, "Неизвестная команда!")
    }

    protected fun onTransition(
        text: String,
        user: User,
        bot: TelegramLongPollingBot,
        update: Update
    ) {
        for (child in children) {
            if (text in child.name) {
                enterChild(child, user, bot, update)
                break
            }
        }
    }

    protected fun enterChild(
        child: Foldable,
        user: User,
        bot: TelegramLongPollingBot,
        update: Update
    ) {
        when (child) {
            is Menu -> {
                user.send(
                    bot = bot,
                    text = "Переходим в раздел...",
                    replyKeyboard = child.getReplyKeyboard(user)
                )
                child.onEnter(bot, user)
                user.path.addToPath(child)
            }

            else -> child.onUpdate(update, bot, user)
        }
    }

    protected fun onReturnButton(
        user: User,
        bot: TelegramLongPollingBot
    ) {
        if (user.path.canReturn()) {
            user.path.returnBack()
            user.send(
                bot = bot,
                text = "Возвращаемся...",
                replyKeyboard = user.path.last().getReplyKeyboard(user)
            )
        } else {
            user.send(bot, "Возвращаться некуда.")
        }
    }

    open fun getReplyKeyboard(user: User): ReplyKeyboard {
        val rows = ArrayList<KeyboardRow>()
        for (part in children.asList().chunked(2)) {
            val row = KeyboardRow()
            row.addAll(part.flatMap { name })
            rows.add(row)
        }
        val additionalRows = getAdditionalButtonsForReply(user)
            .chunked(2)
            .map { row -> KeyboardRow().also { it.addAll(row) } }
        rows.addAll(additionalRows)
        val backRow = KeyboardRow()
        backRow.add("Помощь")
        backRow.add("<< Назад")
        rows.add(backRow)
        return ReplyKeyboardMarkup(rows)
    }

    open fun getAdditionalButtonsForReply(user: User): List<String> = listOf()

    open fun onEnter(bot: TelegramLongPollingBot, user: User) = Unit
}
