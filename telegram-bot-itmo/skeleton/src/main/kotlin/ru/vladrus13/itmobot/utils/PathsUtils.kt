package ru.vladrus13.itmobot.utils

class PathsUtils {

    class MessageUno(val name: String, val arguments: List<String>) {
        constructor(s: String) : this(
            s.split(COMMAND_ARGUMENTS_SPLITTER, limit = 1)[0],
            s.split(COMMAND_ARGUMENTS_SPLITTER).stream().skip(1).toList()
        )
    }

    class MessagePath(val list: List<MessageUno>, val arguments: List<String>) {
        constructor(s: String) : this(
            s.split(COMMAND_PATH_SPLITTER_SPACE, limit = 1)[0].split(COMMAND_PATH_SPLITTER).map { MessageUno(it) }
                .toList(),
            s.split(COMMAND_PATH_SPLITTER_SPACE).stream().skip(1).toList()
        )
    }

    companion object {

        private const val FOLDER_USER_SPLITTER = '/'
        const val COMMAND_PATH_SPLITTER = '_'
        const val COMMAND_PATH_SPLITTER_SPACE = ' '
        const val COMMAND_ARGUMENTS_SPLITTER = '1'

        fun foldersSplit(s: String): List<String> {
            return s.split(FOLDER_USER_SPLITTER)
        }

        fun foldersChatSplit(s: String): List<String> {
            val position = s.indexOf(' ')
            return if (position != -1) {
                s.substring(1, position).split(COMMAND_PATH_SPLITTER)
            } else {
                s.substring(1).split(COMMAND_PATH_SPLITTER)
            }

        }

        fun indexFromFolderSplit(s: String): List<String> {
            return s.split(COMMAND_ARGUMENTS_SPLITTER)
        }
    }
}