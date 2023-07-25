package ru.vladrus13.itmobot.xml

import org.w3c.dom.Node
import ru.vladrus13.itmobot.xml.entities.XMLDatabase
import ru.vladrus13.itmobot.xml.entities.XMLPlugin
import ru.vladrus13.itmobot.xml.entities.XMLTable
import java.io.File
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class XMLParser {
    companion object {
        val datatableList: MutableList<XMLDatabase> = mutableListOf()
        val tableList: MutableList<XMLTable> = mutableListOf()
        val pluginList: MutableList<XMLPlugin> = mutableListOf()

        fun init() {
            val documentBuilderFactory = DocumentBuilderFactory.newInstance()
            documentBuilderFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true)
            val db = documentBuilderFactory.newDocumentBuilder()
            val file = File("src/main/resources/xml/service.xml")
            if (!file.exists()) {
                throw NoSuchFileException(file)
            }
            val document = db.parse(file)
            document.documentElement.normalize()
            val listTables = document.getElementsByTagName("table")
            for (i in 0 until listTables.length) {
                val item = listTables.item(i)
                tableList.add(XMLTable(
                        getAttribute(item, "id"),
                        getAttribute(item, "class"),
                        getAttribute(item, "trigger")))
            }
            val listDatatables = document.getElementsByTagName("datatable")
            for (i in 0 until listDatatables.length) {
                val item = listDatatables.item(i)
                datatableList.add(XMLDatabase(
                        getAttribute(item, "id"),
                        getAttribute(item, "class"),
                        getAttribute(item, "subclass")))
            }
            val listPlugins = document.getElementsByTagName("plugin")
            for (i in 0 until listPlugins.length) {
                val item = listPlugins.item(i)
                pluginList.add(XMLPlugin(
                        getAttribute(item, "id"),
                        getAttribute(item, "class")))
            }
        }

        private fun getAttribute(node: Node, clazz: String): String {
            val attributes = node.attributes
            val real =
                    attributes.getNamedItem(clazz)
                            ?: throw NullPointerException("Node with name $clazz can't be null")
            real.normalize()
            return real.nodeValue
        }
    }
}