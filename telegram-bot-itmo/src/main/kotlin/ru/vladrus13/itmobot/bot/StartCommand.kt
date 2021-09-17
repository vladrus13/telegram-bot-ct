package ru.vladrus13.itmobot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.Chat
import ru.vladrus13.itmobot.bean.Chatted
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import ru.vladrus13.itmobot.command.Menu
import java.nio.file.Path

class StartCommand(override val parent: Menu) : Command() {

    override fun help(): String {
        return "Команда старта"
    }

    override val name: String
        get() = "Старт"
    override val systemName: String
        get() = "start"

    override fun isAccept(update: Update): Boolean {
        return update.message.text!! == "/start" || update.message.text!! == "Старт"
    }

    private fun get(bot: TelegramLongPollingBot, user: User, chatted: Chatted, isKeyboard: Boolean) {
        // TODO paths to normal
        chatted.send(
            bot = bot,
            text = """
                        Привет!
                        Пока бот не работал, он успел получить обновление!
                        
                        Теперь бот быстрее и удобнее для пользователя!
                        
                        Немного о возможностях.
                        
                        1) Вы можете запрашивать у бота расписание пар. Для этого вам нужно будет выбрать группу в настройках.
                        2) Можно попинать старосту, что бы тот написал @vladrus13. После этого ему будет дан доступ в табличку, которая даст доступ группе к командам ссылки и табличкам
                        3) Что значит к команде ссылки? Если у вас есть куча ссылок (например, табличка по ДМ), то вы можете добавить ее в табличку, что бы можно было спросить у бота полный список ссылок. Это очень удобно.
                        4) Что значит к табличкам? В ваших табличках есть фамилии и баллы. Бот умеет вытаскивать их и говорить вам (а так же сообщать об изменениях).
                        5) Плагины! Это разделы от пользователей, которые могут расширять возможности бота. Например, реализация API, для того, что бы просить у бота их вызвать (очень удобно)
                    """.trimIndent()
        )
        chatted.send(
            bot = bot,
            text = """
                    НЕМНОГО О НАМЕРЕННОЙ ПОЛОМКЕ БОТА
                    
                    К сожалению, бот не рассчитан на то, что его будут ломать. Пожалуйста, пользуйтесь ботом по назначению.
                    Если окажется, что бот сломался из-за вас и вы намеренно его ломали, то нам придется отключить вас от возможности пользоваться ботом.
                    Не надо так делать(
                    """.trimIndent(),
            image = Path.of("../telegram-bot-itmo-data/images/memes/we_will_banned_you.png")
        )
        chatted.send(
            bot = bot,
            text = """
                    Как делать не надо. Если вы хотите потестировать бота, напишите об этом @vladrus13
                    """.trimIndent(),
            image = Path.of("../telegram-bot-itmo-data/images/memes/tester.jpg")
        )
        chatted.send(
            bot = bot,
            text = """
                    НЕМНОГО О ФИЧАХ И ОШИБКАХ В БОТЕ
                    
                    1) Если вы заметили в боте некорректное поведение, которое показалось вам необычным или какие либо ошибки, то можете смело писать @vladrus13. Он постарается закрыть все баги очень оперативно.
                    2) Вы можете писать @vladrus13 о предложениях по улучшению бота, добавлению возможностей и различных плагинов. <ТУТ ДОЛЖНА БЫТЬ ССЫЛКА НА ГИТХАБ С БОТОМ> - здесь вы можете посмотреть все, что вам может потребоваться для написания плагинов. Если вы хотите что то реализовать сами, то такое только приветствуется
                    """.trimIndent(),
            replyKeyboard = if (isKeyboard) parent.getReplyKeyboard(user) else null,
            image = Path.of("../telegram-bot-itmo-data/images/memes/mem_open_source.jpg")
        )
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User) {
        get(bot, user, user, true)
    }

    override fun get(update: Update, bot: TelegramLongPollingBot, user: User, chat: Chat) {
        get(bot, user, chat, false)
    }
}