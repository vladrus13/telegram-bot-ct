package ru.vladrus13.itmobot.utils

import org.jsoup.nodes.Element
import org.jsoup.select.Elements
import ru.vladrus13.itmobot.tables.schedule.ScheduleTable

class TableUtils {
    companion object {
        fun getTableFromHTML(table: Element): MutableList<MutableList<String>> {
            val tableFull: MutableList<MutableList<String>>
            val skips = ArrayList<ScheduleTable.Skip>()
            tableFull = ArrayList()
            val trs: Elements = table.getElementsByTag("tr")
            var column = 0
            for ((row, tr) in trs.withIndex()) {
                tableFull.add(ArrayList())
                val tds: Elements = tr.getElementsByTag("td")
                for (td in tds) {
                    while (true) {
                        val finalColumn1 = column
                        val skip = skips.firstOrNull { element: ScheduleTable.Skip ->
                            element.startRow == row && element.startColumn == finalColumn1
                        } ?: break
                        for (i in 0 until skip.sizeColumn) {
                            tableFull[row].add(skip.text)
                            column++
                        }
                        skip.down()
                        if (skip.sizeRow == 0) {
                            skips.remove(skip)
                        }
                    }
                    val text: String = td.text()
                    var futureRow = 1
                    var futureColumn = 1
                    if (td.hasAttr("rowspan")) {
                        futureRow = td.attr("rowspan").toInt()
                    }
                    if (td.hasAttr("colspan")) {
                        futureColumn = td.attr("colspan").toInt()
                    }
                    if (futureRow == 1 && futureColumn == 1) {
                        tableFull[row].add(text)
                        column++
                    } else {
                        val skip = ScheduleTable.Skip(row, column, futureRow, futureColumn, text)
                        for (i in 0 until skip.sizeColumn) {
                            tableFull[row].add(skip.text)
                            column++
                        }
                        skip.down()
                        if (skip.sizeRow != 0) {
                            skips.add(skip)
                        }
                    }
                }
                // TODO backlog: copy-pasta
                while (true) {
                    val finalColumn1 = column
                    val skip = skips.firstOrNull { element: ScheduleTable.Skip ->
                        element.startRow == row && element.startColumn == finalColumn1
                    } ?: break
                    for (i in 0 until skip.sizeColumn) {
                        tableFull[row].add(skip.text)
                        column++
                    }
                    skip.down()
                    if (skip.sizeRow == 0) {
                        skips.remove(skip)
                    }
                }
                column = 0
            }
            return tableFull
        }
    }
}