package ru.vladrus13.itmobot.tables.parsers

import lombok.EqualsAndHashCode
import org.apache.logging.log4j.kotlin.Logging
import ru.vladrus13.itmobot.tables.TableModule
import ru.vladrus13.itmobot.google.GoogleTableResponse
import ru.vladrus13.itmobot.tables.ResultPair
import ru.vladrus13.itmobot.tables.Table
import java.lang.Integer.min
import java.util.*

@EqualsAndHashCode(of = arrayOf("name"))
class GoogleTwoColumnTable(
        override val name: String,
        val url: String,
        val sheetName: String,
        val names: String,
        val results: String,
        updateCoolDown: Long
) : Table(updateCoolDown), Logging {
    override var nextUpdate = Date(0)
    private var data: HashMap<String, ResultPair> = HashMap()

    override fun reload(): ArrayList<ResultPair> {
        val answer = ArrayList<ResultPair>()
        val namesList: ArrayList<ArrayList<String>>
        val resultList: ArrayList<ArrayList<String>>
        try {
            namesList = GoogleTableResponse.reload(url, sheetName, "$names:$names")
            resultList = GoogleTableResponse.reload(url, sheetName, "$results:$results")
        } catch (e: Exception) {
            logger.error("Something went wrong", e)
            return ArrayList()
        }
        val size = min(namesList.size, resultList.size)
        try {
            for (i in 0 until size) {
                if (namesList[i].isEmpty()) continue
                val name = namesList[i][0]
                if (name.isEmpty()) continue
                if (resultList[i].isEmpty()) continue
                val result = resultList[i][0]
                if (data.containsKey(name)) {
                    val resultPair = data[name]!!
                    if (resultPair.set(result)) {
                        answer.add(resultPair)
                    }
                } else {
                    val resultPair = ResultPair(this.name, name, result)
                    data[name] = resultPair
                    // answer.add(resultPair)
                }
            }
        } catch (e: Exception) {
            logger.error("Something went wrong", e)
            return ArrayList()
        }
        nextUpdate = Date(Date().time + updateCoolDown)
        return answer
    }

    override fun get(name: String): ResultPair? {
        for (it in data.entries) {
            if (it.key.contains(name, ignoreCase = true)) {
                return it.value
            }
        }
        return null
    }

    class GoogleTwoColumnTableFactory : TableModule.TableConstructor {
        override fun construct(list: ArrayList<String>, coolDown: Long): GoogleTwoColumnTable {
            return GoogleTwoColumnTable(
                    name = list[0],
                    url = list[2],
                    sheetName = list[3],
                    names = list[4],
                    results = list[5],
                    updateCoolDown = coolDown)
        }
    }

}