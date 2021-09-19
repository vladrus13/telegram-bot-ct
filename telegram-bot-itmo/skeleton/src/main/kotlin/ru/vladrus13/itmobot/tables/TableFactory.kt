package ru.vladrus13.itmobot.tables

import ru.vladrus13.itmobot.xml.XMLParser
import kotlin.reflect.full.primaryConstructor

class TableFactory {
    companion object {
        fun getTable(list: ArrayList<String>, coolDown: Long): Table {
            // TODO do something with constructor list and cooldown
            try {
                return XMLParser.tableList.first { it.trigger == list[1] }.clazz.primaryConstructor!!.call(
                    list,
                    coolDown
                )
            } catch (e: NoSuchElementException) {
                throw IllegalStateException("Can't find table with trigger ${list[1]}")
            }
        }
    }
}