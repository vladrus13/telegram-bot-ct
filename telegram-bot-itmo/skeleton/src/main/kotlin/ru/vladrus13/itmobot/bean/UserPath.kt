package ru.vladrus13.itmobot.bean

import com.fasterxml.jackson.databind.ObjectMapper
import ru.vladrus13.itmobot.command.Menu

class UserPath(
    private var path: MutableList<Menu>,
    val data: MutableMap<String, String> = mutableMapOf()
) {

    fun addToPath(newPart: Menu) {
        path.add(newPart)
    }

    fun returnBack(): Menu {
        return path.removeLast()
    }

    fun canReturn() = path.size > 1

    fun last() = path.last()

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
        mainNode.put("objectPath", path.map { it.name }.joinToString { "/" })
        return objectWriter.writeValueAsString(mainNode)
    }
}