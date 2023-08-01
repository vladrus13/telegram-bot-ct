package ru.vladrus13.itmobot.bean

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vladrus13.itmobot.command.Menu

class UserPath(
    private var myPath: MutableList<Menu>,
    val data: MutableMap<String, String> = mutableMapOf()
) {

    fun myAddToPath(newPart: Menu) {
        myPath.add(newPart)
    }

    fun myRemoveFromPath(): Menu {
        return myPath.removeLast()
    }

    fun myCanReturn() = myPath.size > 1

    fun myLast() = myPath.last()

    fun setData(key: String, data: String) {
        this.data[key] = data
    }

    fun getData(key: String): String? {
        return this.data[key]
    }

    override fun toString(): String {
        val objectMapper = ObjectMapper()
        val objectWriter = objectMapper.writer()
        val mainNode = objectMapper.createObjectNode()
        mainNode.put("objectPath", myPath.map { it.name }.joinToString { "/" })
        return objectWriter.writeValueAsString(mainNode)
    }
}