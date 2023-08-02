package ru.vladrus13.itmobot.tables.parsers

import org.jsoup.Jsoup
import ru.vladrus13.itmobot.tables.ResultPair
import ru.vladrus13.itmobot.tables.Table
import ru.vladrus13.itmobot.tables.TableModule
import ru.vladrus13.itmobot.utils.TableUtils
import java.util.*

class GoshaHtmlTable(
    override val name: String,
    private val url: String,
    private val names: Int,
    private val results: Int,
    updateCoolDown: Long
) : Table(updateCoolDown) {
    override var nextUpdate: Date = Date(0)
    private val data: HashMap<String, ResultPair> = HashMap()

    override fun reload(): ArrayList<ResultPair> {
        val getting = Jsoup.connect(url)
            .timeout(0)
            .get()
            .getElementsByTag("table")[0]
            .getElementsByTag("tbody")[0]
        val table = TableUtils.getTableFromHTML(getting)
        val list = arrayListOf<ResultPair>()
        table.forEach {
            val name = it[names]
            val result = it[results]
            if (name.isNotBlank() && result.isNotBlank()) {
                if (data.containsKey(name)) {
                    val resultPair = data[name]!!
                    if (resultPair.set(result)) {
                        list.add(resultPair)
                    }
                } else {
                    val resultPair = ResultPair(this.name, name, result)
                    data[name] = resultPair
                    // list.add(resultPair)
                }
            }
        }
        nextUpdate = Date(Date().time + updateCoolDown)
        return list
    }

    override fun get(name: String): ResultPair? {
        for (it in data.entries) {
            if (it.key.contains(name, ignoreCase = true)) {
                return it.value
            }
        }
        return null
    }

    class GoshaHtmlTableConstructor : TableModule.TableConstructor {
        override fun construct(list: ArrayList<String>, coolDown: Long): Table {
            return GoshaHtmlTable(
                name = list[0],
                url = list[2],
                names = list[3].toInt(),
                results = list[4].toInt(),
                updateCoolDown = coolDown
            )
        }
    }
}