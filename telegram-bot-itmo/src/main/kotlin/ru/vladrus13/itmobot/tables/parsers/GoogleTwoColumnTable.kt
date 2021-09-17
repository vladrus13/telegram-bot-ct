package ru.vladrus13.itmobot.tables.parsers

import ru.vladrus13.itmobot.Launcher.Companion.logger
import ru.vladrus13.itmobot.google.GoogleTableResponse
import ru.vladrus13.itmobot.tables.ResultPair
import ru.vladrus13.itmobot.tables.Table
import ru.vladrus13.itmobot.utils.Writer
import java.lang.Integer.min
import java.util.*

class GoogleTwoColumnTable(list: ArrayList<String>, updateCoolDown: Long) : Table(updateCoolDown) {

    override val name: String = list[0]
    private val url: String = list[2]
    private val sheetName: String = list[3]
    private val names: String = list[4]
    private val results: String = list[5]

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
            Writer.printStackTrace(logger = logger, e)
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
            Writer.printStackTrace(logger = logger, e)
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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as GoogleTwoColumnTable

        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }


}