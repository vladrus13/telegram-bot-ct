package ru.vladrus13.itmobot.xml.entities

import ru.vladrus13.itmobot.exceptions.XMLClassCastException
import ru.vladrus13.itmobot.plugins.Plugin
import kotlin.reflect.KClass

class XMLPlugin {
    val id: String
    val clazz: KClass<Plugin>

    constructor(id: String, clazz: String) {
        this.id = id
        try {
            // TODO fix bug with wrong instance
            this.clazz = Class.forName(clazz).kotlin as KClass<Plugin>
        } catch (e: ClassCastException) {
            throw XMLClassCastException("KClass<Plugin>", Class.forName(clazz).kotlin, "class")
        }
    }
}