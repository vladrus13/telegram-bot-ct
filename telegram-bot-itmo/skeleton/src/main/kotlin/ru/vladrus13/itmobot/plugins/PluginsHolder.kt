package ru.vladrus13.itmobot.plugins

import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.properties.InitialProperties.Companion.logger
import ru.vladrus13.itmobot.xml.XMLParser
import kotlin.reflect.full.primaryConstructor

class PluginsHolder {
    companion object {
        val plugins
            get() = XMLParser.pluginList.map { it.clazz.primaryConstructor!!.call() }.toTypedArray()

        fun getPluginByName(name: String): Plugin? {
            for (plugin in plugins) {
                if (plugin.name == name) {
                    return plugin
                }
            }
            return null
        }

        fun init() {
            for (plugin in plugins) {
                logger.info("=== Initialize plugin ${plugin.systemName}")
                plugin.init()
            }
        }

        fun getPluginBySystemName(name: String): Plugin? {
            for (plugin in plugins) {
                if (plugin.systemName == name) {
                    return plugin
                }
            }
            return null
        }

        fun getFoldable(o: Foldable): ArrayList<Pair<Plugin, Foldable>> {
            val list = ArrayList<Pair<Plugin, Foldable>>()
            for (plugin in plugins) {
                list.addAll(plugin.addFoldable(o))
            }
            return list
        }
    }
}