package ru.vladrus13.itmobot.plugins

import ru.vladrus13.itmobot.command.Foldable
import ru.vladrus13.itmobot.plugins.scheduletimer.ScheduleTimerPlugin
import ru.vladrus13.itmobot.plugins.shipperPlugin.ShipperPlugin
import ru.vladrus13.itmobot.plugins.simplePlugin.SimplePlugin

class PluginsHolder {
    companion object {
        val plugins = arrayOf(
            SimplePlugin(),
            ScheduleTimerPlugin(),
            ShipperPlugin()
        )

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