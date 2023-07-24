package ru.vladrus13.itmobot.database

import org.apache.logging.log4j.kotlin.Logging
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import org.jetbrains.exposed.sql.transactions.transaction
import ru.vladrus13.itmobot.plugins.PluginsHolder
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.xml.XMLParser
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

class DataBaseParser {
    companion object : Logging {

        val connection = Database.connect(
            url = InitialProperties.databaseProperties.getProperty("url"),
            driver = InitialProperties.databaseProperties.getProperty("driver"),
            user = InitialProperties.databaseProperties.getProperty("user"),
            password = InitialProperties.databaseProperties.getProperty("password")
        )

        val parsers: MutableMap<KClass<*>, DataBaseEntity<*>> = hashMapOf()

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T> getParser(): DataBaseEntity<T> {
            if (parsers.containsKey(T::class)) {
                return parsers[T::class] as DataBaseEntity<T>
            } else {
                throw IllegalStateException("Don't have parser type: ${T::class.simpleName}")
            }
        }

        inline fun <reified T> set(chatId: Long, o: T) {
            val parser = getParser<T>()
            parser.put(o, chatId)
        }

        inline fun <reified T> get(chatId: Long): T {
            val parser = getParser<T>()
            return parser.getByChatId(chatId)
        }

        inline fun <reified T> get(noinline filter: SqlExpressionBuilder.() -> Op<Boolean>): List<T> {
            val parser = getParser<T>()
            return parser.getAllByFilter(filter)
        }

        inline fun <reified T> delete(chatId: Long) {
            val parser = getParser<T>()
            parser.deleteByChatId(chatId)
        }

        @Throws(IllegalArgumentException::class)
        fun init() {
            logger.info("=== Start load databases from XML")
            for (it in XMLParser.datatableList) {
                parsers[it.subclass] = it.clazz.createInstance()
            }
            logger.info("=== Finish load databases from XML")
            logger.info("=== Start initialize databases")
            transaction(connection) {
                addLogger(StdOutSqlLogger)
                for (plugin in PluginsHolder.plugins) {
                    for (table in plugin.getDataBases()) {
                        parsers[table.first] = table.second
                    }
                }
                for (parser in parsers.values) {
                    logger.info("==== Initialize database ${parser::class.simpleName}")
                    parser.init()
                }
            }
            logger.info("=== Finish initialize databases")
        }
    }
}

abstract class DataBaseEntity<T> {

    abstract val name: String

    abstract val table: Table
    abstract val columnId: Column<Long>

    abstract fun getNew(chatId: Long): T

    abstract fun set(o: T, it: UpdateBuilder<Number>)

    abstract fun get(result: ResultRow): T

    fun init() {
        SchemaUtils.create(table)
    }

    fun deleteByChatId(chatId: Long) {
        transaction(DataBaseParser.connection) {
            table.deleteWhere { columnId eq chatId }
        }
    }

    fun getByChatId(chatId: Long): T {
        var returned = getNew(chatId)
        transaction(DataBaseParser.connection) {
            val entities = table.select { columnId eq chatId }
            returned = when (entities.count()) {
                0 -> getNew(chatId)
                1 -> {
                    get(entities.single())
                }
                else -> throw IllegalStateException("More than 1 entities with id: $chatId")
            }
        }
        return returned
    }

    fun getAllByFilter(filter: SqlExpressionBuilder.() -> Op<Boolean>): List<T> {
        val list = mutableListOf<T>()
        transaction(DataBaseParser.connection) {
            val answer = table.select(filter)
            for (it in answer) {
                list.add(get(result = it))
            }
        }
        return list
    }

    fun put(o: T, chatId: Long) {
        transaction(DataBaseParser.connection) {
            val entities = table.select { columnId eq chatId }
            if (entities.count() != 0) {
                table.update({ columnId eq chatId }) {
                    set(o, it)
                }
            } else {
                table.insert {
                    it[columnId] = chatId
                    set(o, it)
                }
            }
        }
    }
}