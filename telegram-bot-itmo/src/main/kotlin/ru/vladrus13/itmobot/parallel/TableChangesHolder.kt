package ru.vladrus13.itmobot.parallel

import ru.vladrus13.itmobot.Launcher
import ru.vladrus13.itmobot.database.DataBase
import ru.vladrus13.itmobot.tables.TableGroupsHolder

class TableChangesHolder {
    companion object {

        private const val timeToChange: Long = Launcher.timeToReloadTable

        fun run() {
            ThreadHolder.executorService.submit {
                while (true) {
                    val users = DataBase.getUsersWithNotification()
                    for (it in TableGroupsHolder.changes) {
                        while (it.value.isNotEmpty()) {
                            val resultPair = it.value.pollFirst()!!
                            for (user in users) {
                                if (user.group == null || user.name == null) continue
                                if (TableGroupsHolder[it.key].isDepends(user.group!!)) {
                                    if (resultPair.name.contains(user.name!!)) {
                                        user.send(
                                            bot = Launcher.bot,
                                            text = "Произошло изменение в баллах!\n${it.key}: ${resultPair.callChangeToString()}"
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Thread.sleep(timeToChange)
                }
            }
        }
    }
}