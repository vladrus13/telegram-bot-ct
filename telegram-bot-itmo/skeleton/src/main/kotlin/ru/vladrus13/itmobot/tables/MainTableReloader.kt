package ru.vladrus13.itmobot.tables

import org.apache.logging.log4j.kotlin.Logging
import ru.vladrus13.itmobot.google.GoogleTableResponse
import ru.vladrus13.itmobot.parallel.ThreadHolder
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.utils.SafeRunnable
import java.util.concurrent.TimeUnit

class MainTableReloader(
    val pointTablesRegistry: PointTablesRegistry,
    val tableConstructorsMap: Map<String, TableModule.TableConstructor>
) : Logging {
    private val link: String = InitialProperties.mainProperties.getProperty("MAIN_TABLE")

    init {
        reload()
        ThreadHolder.scheduledExecutorService.scheduleAtFixedRate(
            SafeRunnable(::reload),
            InitialProperties.timeToReloadMainTable,
            InitialProperties.timeToReloadMainTable,
            TimeUnit.MILLISECONDS
        )
    }

    fun reload() {
        var startMillis = System.currentTimeMillis()
        logger.info("=== Main table reload start")
        val names = GoogleTableResponse.getNames(link)
        val pointsTablesByName = HashMap<String, Table>()
        val pointsTablesNamesByGroup = HashMap<String, ArrayList<String>>()
        val linksByGroup = HashMap<String, String>()
        for (tableName in names) {
            if (tableName == "tables") {
                processMainTable(tableName, pointsTablesByName)
            } else {
                processPointTablesForGroup(tableName, pointsTablesByName, pointsTablesNamesByGroup)
                processLinksTable(tableName, linksByGroup)
            }
        }
        pointTablesRegistry.set(pointsTablesByName, pointsTablesNamesByGroup, linksByGroup)
        logger.info("=== Main table reload finished. Time = ${System.currentTimeMillis() - startMillis} ms")
    }

    private fun processMainTable(name: String, tables: HashMap<String, Table>) {
        val rowsMainTable: ArrayList<ArrayList<String>>
        try {
            rowsMainTable = GoogleTableResponse.reload(link, name, "A2:Z")
        } catch (e: Exception) {
            logger.error("Something went wrong", e)
            return
        }
        val rows = HashMap<String, ArrayList<String>>()
        for (row in rowsMainTable) {
            if (row.isEmpty()) continue
            val tableName = row[0]
            if (row.size < 3) {
                logger.warn("Table with name $tableName contain too low columns")
                continue
            }
            if (rows.containsKey(tableName)) {
                logger.warn("Duplicate table name $tableName, will be taking first occurence")
                continue
            }
            logger.info("==== Add table with name $tableName")
            rows[tableName] = row
            tables[tableName] = getTable(row, InitialProperties.timeToReloadPointsTable)
        }
    }

    fun getTable(list: ArrayList<String>, coolDown: Long): Table {
        return tableConstructorsMap[list[1]]?.construct(list, coolDown)
            ?: throw IllegalStateException("Can't find table with trigger ${list[1]}")
    }

    private fun processPointTablesForGroup(
        groupName: String,
        pointsTablesByNames: HashMap<String, Table>,
        pointsTablesNamesByGroup: HashMap<String, ArrayList<String>>
    ) {
        var linksArray: ArrayList<ArrayList<String>>
        try {
            linksArray = GoogleTableResponse.reload(link, groupName, "D:E")
        } catch (e: Exception) {
            logger.error("Something went wrong", e)
            return
        }
        for (linksForGroupRow in linksArray) {
            if (linksForGroupRow.size > 1) {
                val pointsTableName = linksForGroupRow[0]
                val pointsTableDescription = linksForGroupRow[1]
                pointsTablesNamesByGroup.computeIfAbsent(groupName) { ArrayList() }
                    .add(pointsTableName)
                pointsTablesByNames[pointsTableName]?.addGroup(groupName, pointsTableDescription)
            }
        }
    }

    private fun processLinksTable(groupName: String, linksByGroup: HashMap<String, String>) {
        var linksArray: ArrayList<ArrayList<String>>
        try {
            linksArray = GoogleTableResponse.reload(link, groupName, "A:B")
        } catch (e: Exception) {
            logger.error("Something went wrong", e)
            return
        }
        val parts: ArrayList<String> = ArrayList()
        val iterator = linksArray.listIterator()
        while (iterator.hasNext()) {
            var currentLine = iterator.next()
            if (currentLine.size != 1) continue
            val sectionName = currentLine[0]
            val sectionLinks = ArrayList<String>()
            while (iterator.hasNext() && iterator.next().size == 2) {
                currentLine = iterator.previous()
                sectionLinks.add("<a href=\"${currentLine[1]}\">${currentLine[0]}</a>")
            }
            parts.add("<i>$sectionName</i>:\n${sectionLinks.joinToString(separator = " | ")}")
        }
        linksByGroup[groupName] = parts.joinToString(separator = "\n")
    }
}