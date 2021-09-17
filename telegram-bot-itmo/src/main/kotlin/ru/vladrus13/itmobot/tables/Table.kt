package ru.vladrus13.itmobot.tables

import java.util.*

abstract class Table(val updateCoolDown: Long) {
    abstract val name: String
    abstract var nextUpdate: Date
    abstract fun reload(): ArrayList<ResultPair>
    abstract operator fun get(name: String): ResultPair?
    private var groups: HashMap<String, String> = HashMap()

    fun addGroup(name: String, nameOfTable: String) {
        groups[name] = nameOfTable
    }

    fun addGroup(names: List<Pair<String, String>>) {
        groups.putAll(names)
    }

    fun isDepends(name: String): Boolean {
        while (true) {
            try {
                return groups.contains(name)
            } catch (ignored: Exception) {
            }
        }
    }

    fun getGroups(): List<Pair<String, String>> {
        return groups.toList()
    }

}