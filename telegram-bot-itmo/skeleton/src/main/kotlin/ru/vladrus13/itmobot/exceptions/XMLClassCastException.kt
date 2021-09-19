package ru.vladrus13.itmobot.exceptions

import kotlin.reflect.KClass

class XMLClassCastException(excepted: String, val found: KClass<*>, val field: String) :
    ItmoBotException("class ${found.qualifiedName} cannot be cabt to class $excepted. Field: $field")