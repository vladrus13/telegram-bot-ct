package ru.vladrus13.itmobot.tables

import org.apache.logging.log4j.kotlin.Logging
import java.util.concurrent.ConcurrentHashMap

class PointTablesRegistry : Logging {
    var pointsTablesByName = ConcurrentHashMap<String, Table>()
    var pointsTableNamesByGroup = ConcurrentHashMap<String, ArrayList<String>>()
    var linksByGroup = ConcurrentHashMap<String, String>()

    fun set(table: Table) {
        pointsTablesByName[table.name] = table
    }

    operator fun get(name: String): Table? {
        return pointsTablesByName[name]
    }

    fun set(
        pointsTablesByName: Map<String, Table>,
        pointsTableNamesByGroup: Map<String, ArrayList<String>>,
        linksByGroup: Map<String, String>
    ) {
        this.pointsTablesByName = ConcurrentHashMap(pointsTablesByName)
        this.pointsTableNamesByGroup = ConcurrentHashMap(pointsTableNamesByGroup)
        this.linksByGroup = ConcurrentHashMap(linksByGroup)
    }
}