package ru.vladrus13.itmobot.tables

import ru.vladrus13.itmobot.google.GoogleTableResponse
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.logger
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.timeToReloadJobs
import ru.vladrus13.itmobot.utils.Utils
import ru.vladrus13.itmobot.utils.Writer
import java.util.concurrent.ConcurrentHashMap

class MainTableHolder {
    companion object {
        private val link: String = InitialProperties.mainProperties.getProperty("MAIN_TABLE")

        private const val timeToChange: Long = timeToReloadJobs
        val links: ConcurrentHashMap<String, String> = ConcurrentHashMap()
        val groupsTables: ConcurrentHashMap<String, ArrayList<String>> = ConcurrentHashMap()
        private val tables: HashMap<String, ArrayList<String>> = HashMap()

        fun reload() {
            val names = GoogleTableResponse.getNames(link)
            for (name in names) {
                if (name == "tables") {
                    val array: ArrayList<ArrayList<String>>
                    try {
                        array = GoogleTableResponse.reload(link, name, "A2:Z")
                    } catch (e: Exception) {
                        Writer.printStackTrace(logger, e)
                        continue
                    }
                    for (row in array) {
                        continue
                        if (row.isEmpty()) continue
                        if (row.size < 3) {
                            logger.warning("Table with name ${row[0]} contain too low columns")
                            continue
                        }
                        if (!tables.containsKey(row[0])) {
                            logger.info("==== Add table with name ${row[0]}")
                            tables[row[0]] = row
                            TableGroupsHolder.set(TableFactory.getTable(row, timeToChange))
                        } else {
                            if (!Utils.equals(tables[row[0]]!!, row)) {
                                val groupSet = TableGroupsHolder[row[0]]?.getGroups()
                                if (groupSet == null) {
                                    logger.info("==== Can't add to table with name: ${row[0]}")
                                } else {
                                    logger.info("==== Changed table with name ${row[0]}")
                                    tables[row[0]] = row
                                    val table = TableFactory.getTable(row, timeToChange)
                                    table.addGroup(groupSet)
                                    TableGroupsHolder.set(table)
                                }
                            }
                        }
                    }
                } else {
                    var linksArray: ArrayList<ArrayList<String>>

                    try {
                        linksArray = GoogleTableResponse.reload(link, name, "D:E")
                    } catch (e: Exception) {
                        Writer.printStackTrace(logger, e)
                        continue
                    }
                    val tableNames = ArrayList<String>()
                    for (it in linksArray) {
                        if (it.size > 1) {
                            tableNames.add(it[0])
                            TableGroupsHolder[it[0]]?.addGroup(name, it[1])
                        }
                    }
                    groupsTables[name] = tableNames
                    try {
                        linksArray = GoogleTableResponse.reload(link, name, "A:B")
                    } catch (e: Exception) {
                        Writer.printStackTrace(logger, e)
                        continue
                    }
                    val parts: ArrayList<String> = ArrayList()
                    var currentName: String? = null
                    val listOfLinks: ArrayList<String> = ArrayList()
                    for (it in linksArray) {
                        when (it.size) {
                            0 -> {
                                continue
                            }
                            1 -> {
                                if (currentName != null) {
                                    parts.add("<i>$currentName</i>:\n${listOfLinks.joinToString(separator = " | ")}")
                                }
                                currentName = it[0]
                                listOfLinks.clear()
                            }
                            2 -> {
                                listOfLinks.add("<a href=\"${it[1]}\">${it[0]}</a>")
                            }
                        }
                    }
                    if (currentName != null) {
                        parts.add("<i>$currentName</i>:\n${listOfLinks.joinToString(separator = " | ")}")
                    }
                    val message: String = parts.joinToString(separator = "\n")
                    links[name] = message
                }
            }
        }
    }
}