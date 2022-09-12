package ru.vladrus13.itmobot.bean

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import ru.vladrus13.itmobot.database.DataBase

class User(
    chatId: Long,
    val role: Int = 0,
    group: String? = null,
    var name: String? = null,
    var username: String? = null,
    settings: Settings = Settings(3),
    var path: UserPath = UserPath("main"),
    var notification: Boolean = true,
    private var usersSubjects: UsersSubjects? = null,
    private var usersPlugins: UserPlugins? = null,
    updateBotUser: UpdateBotUser? = null
) : Chatted(chatId, group, settings, updateBotUser) {

    class UserPath(private var path: String, val data: MutableMap<String, String> = mutableMapOf()) {

        fun getPath() : String = path
        fun setPath(s : String) {
            path = s
            data.clear()
        }

        fun setData(key : String, data : String) {
            this.data[key] = data
        }

        fun getData(key: String): String? {
            return this.data[key]
        }

        override fun toString(): String {
            val objectMapper = ObjectMapper()
            val objectWriter = objectMapper.writer()
            val mainNode = objectMapper.createObjectNode()
            val dataNode = objectMapper.createArrayNode()
            data.forEach { (key, value) -> dataNode.add(
                objectMapper.createObjectNode().apply {
                    put("key", key)
                    put("value", value)
                }) }
            mainNode.put("path", path)
            mainNode.replace("data", dataNode)
            return objectWriter.writeValueAsString(mainNode)
        }

        companion object {
            fun fromString(s : String) : UserPath {
                return try {
                    val objectMapper = ObjectMapper()
                    val node = objectMapper.readTree(s)
                    val path = node["path"].asText()!!
                    val dataNode = node["data"] as ArrayNode
                    val data = dataNode.map { subNode ->
                        subNode as ObjectNode
                        Pair(subNode["key"].asText()!!, subNode["value"].asText()!!)
                    }.associate { it.first to it.second }.toMutableMap()
                    UserPath(path, data)
                } catch (e : JsonParseException) {
                    UserPath(s)
                }
            }
        }
    }

    fun getSubjects(forced: Boolean = false): UsersSubjects {
        if (forced || usersSubjects == null) {
            usersSubjects = DataBase.get(chatId)
        }
        return usersSubjects!!
    }

    fun getPlugins(forced: Boolean = false): UserPlugins {
        if (forced || usersPlugins == null) {
            usersPlugins = DataBase.get(chatId)
        }
        return usersPlugins!!
    }
}