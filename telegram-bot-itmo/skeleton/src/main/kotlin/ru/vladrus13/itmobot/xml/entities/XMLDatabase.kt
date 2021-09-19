package ru.vladrus13.itmobot.xml.entities

import ru.vladrus13.itmobot.database.DataBaseEntity
import ru.vladrus13.itmobot.exceptions.XMLClassCastException
import kotlin.reflect.KClass

class XMLDatabase {

    val id: String
    val clazz: KClass<DataBaseEntity<*>>
    val subclass: KClass<*>

    constructor(id: String, clazz: String, subclass: String) {
        this.id = id
        try {
            // TODO fix bug with wrong instance
            this.clazz = Class.forName(clazz).kotlin as KClass<DataBaseEntity<*>>
        } catch (e: ClassCastException) {
            throw XMLClassCastException("KClass<DataBaseEntity<*>>", Class.forName(clazz).kotlin, "class")
        }
        this.subclass = Class.forName(subclass).kotlin
    }
}