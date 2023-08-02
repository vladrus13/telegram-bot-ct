package ru.vladrus13.itmobot.xml

import org.w3c.dom.Node
import ru.vladrus13.itmobot.xml.entities.XMLDatabase
import java.io.File
import javax.xml.XMLConstants
import javax.xml.parsers.DocumentBuilderFactory

class XMLParser {
    companion object {
        val datatableList: MutableList<XMLDatabase> = mutableListOf()

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
            val listDatatables = document.getElementsByTagName("datatable")
            for (i in 0 until listDatatables.length) {
                val item = listDatatables.item(i)
                datatableList.add(
                    XMLDatabase(
                        getAttribute(item, "id"),
                        getAttribute(item, "class"),
                        getAttribute(item, "subclass")
                    )
                )
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