package ru.vladrus13.itmobot.bot

import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.objects.Update
import ru.vladrus13.itmobot.bean.User
import ru.vladrus13.itmobot.command.Command
import java.nio.file.Path

class StartCommand : Command() {
    override val name: String
        get() = "/start"
    override val help: String
        get() = "Команда старта"

    override fun onUpdate(update: Update, bot: TelegramLongPollingBot, user: User) {
        user.send(
            bot = bot,
            text = """
                        Привет!
                        Пока бот не работал, он успел получить обновление!
                        
                        Теперь бот быстрее и удобнее для пользователя!
                        
                        Немного о возможностях.
                        
                        1) Вы можете запрашивать у бота расписание пар. Для этого вам нужно будет выбрать группу в настройках.
                        2) Можно попинать старосту, чтобы тот написал @vladrus13. После этого ему будет дан доступ в табличку, которая даст доступ группе к командам ссылки и табличкам
                        3) Что значит к команде ссылки? Если у вас есть куча ссылок (например, табличка по ДМ), то вы можете добавить ее в табличку, чтобы можно было спросить у бота полный список ссылок. Это очень удобно.
                        4) Что значит к табличкам? В ваших табличках есть фамилии и баллы. Бот умеет вытаскивать их и говорить вам (а также сообщать об изменениях).
                        5) Плагины! Это разделы от пользователей, которые могут расширять возможности бота. Например, реализация API, для того, чтобы просить у бота их вызвать (очень удобно)
                    """.trimIndent()
        )
        user.send(
            bot = bot,
            text = """
                    НЕМНОГО О НАМЕРЕННОЙ ПОЛОМКЕ БОТА
                    
                    К сожалению, бот не рассчитан на то, что его будут ломать. Пожалуйста, пользуйтесь ботом по назначению.
                    Если окажется, что бот сломался из-за вас и вы намеренно его ломали, то нам придется отключить вас от возможности пользоваться ботом.
                    Не надо так делать(
                    """.trimIndent(),
            image = Path.of("../telegram-bot-itmo-data/images/memes/we_will_banned_you.png")
        )
        user.send(
            bot = bot,
            text = """
                    Как делать не надо. Если вы хотите потестировать бота, напишите об этом @vladrus13
                    """.trimIndent(),
            image = Path.of("../telegram-bot-itmo-data/images/memes/tester.jpg")
        )
        user.send(
            bot = bot,
            text = """
                    НЕМНОГО О ФИЧАХ И ОШИБКАХ В БОТЕ
                    
                    1) Если вы заметили в боте некорректное поведение, которое показалось вам необычным или какие либо ошибки, то можете смело писать @vladrus13. Он постарается закрыть все баги очень оперативно.
                    2) Вы можете писать @vladrus13 о предложениях по улучшению бота, добавлению возможностей и различных плагинов. https://github.com/vladrus13/telegram-bot-ct - здесь вы можете посмотреть все, что вам может потребоваться для написания плагинов. Если вы хотите что то реализовать сами, то такое только приветствуется
                    """.trimIndent(),
            replyKeyboard = user.path.myLast().getReplyKeyboard(user),
            image = Path.of("../telegram-bot-itmo-data/images/memes/mem_open_source.jpg")
        )
    }
}