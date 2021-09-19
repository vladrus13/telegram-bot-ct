package ru.vladrus13.itmobot.xml.entities

import ru.vladrus13.itmobot.exceptions.XMLClassCastException
import ru.vladrus13.itmobot.tables.Table
import kotlin.reflect.KClass

class XMLTable {
    val id: String
    val clazz: KClass<Table>
    val trigger: String

    constructor(id: String, clazz: String, trigger: String) {
        this.id = id
        try {
            this.clazz = Class.forName(clazz).kotlin as KClass<Table>
        } catch (e: ClassCastException) {
            throw XMLClassCastException("KClass<Table>", Class.forName(clazz).kotlin, "class")
        }
        this.trigger = trigger
    }
}