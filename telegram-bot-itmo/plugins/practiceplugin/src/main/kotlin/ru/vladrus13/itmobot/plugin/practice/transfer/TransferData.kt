package ru.vladrus13.itmobot.plugin.practice.transfer

import org.slf4j.LoggerFactory
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet.Companion.correct_letters
import ru.vladrus13.itmobot.properties.InitialProperties
import ru.vladrus13.itmobot.utils.Messager

class TransferData {
    companion object {
        private data class TupleFCSTaskResult(val fcs: String, val task: String, val result: String)

        /**
         * Contract:
         * Get this table on input
         * |         | 1 | 2 | 3 | 4 |
         * | Alexey  | 1 | T |   | 1 |
         * | Daniil  | T |   | P | T |
         *
         *  Return this table
         * |   |
         * | 1 | Daniil
         * | 2 | Alexey
         * | 3 |
         * | 4 | Daniil
         */
        fun List<List<String>>.transferStudentTableToTeacher(chatId: Long): List<List<String>> {
            if (this.isEmpty()) return listOf()

            val tasks = this.first()

            LOG.debug("chatId=${chatId}")

            val studentTableWithSkips = try {
                this
                    .asSequence()
                    .drop(1)
                    .map { studentResults ->
                        studentResults
                            .asSequence()
                            .mapIndexed { id, resultTask ->
                                TupleFCSTaskResult(
                                    studentResults[0],
                                    tasks[id],
                                    resultTask
                                )
                            }
                            .filterIndexed { id, tuple -> id > 0 && isT(tuple.result) }
                            .map { tuple -> Pair(tuple.task, tuple.fcs) }
                            .toList()
                    }
                    .flatten()
                    .sortedBy { pair -> pair.first.toInt() }
                    .map { pair -> listOf(pair.first, pair.second) }
                    .toList()
            } catch (e: Exception) {
                Messager.sendMessage(
                    bot = InitialProperties.bot,
                    chatId = chatId,
                    text = this.joinToString("\n") { it.joinToString(",") }
                )

                throw e
            }

            val existsNumbers = studentTableWithSkips
                .map { it.first() }
                .map(String::toInt)
                .sorted()

            LOG.debug("existNumbers={}", existsNumbers)

            return if (existsNumbers.isNotEmpty() && 1 <= existsNumbers.max()) {
                val notExistsNumbers = (1..existsNumbers.max())
                    .toList()
                    .filter { existsNumbers.binarySearch(it) < 0 }
                    .map { listOf(it.toString(), "") }
                studentTableWithSkips.plus(notExistsNumbers).sortedBy { it.first().toInt() }
            } else listOf()
        }

        // returns:
        // Alexey
        // Ivan
        // Andrey
        fun List<List<String>>.transferFCSToLastName(): List<List<String>> = this.map {
            listOf(it[1].takeWhile(Char::isLetter), "")
        }

        private fun isT(element: String) = correct_letters.any { it == element }

        private val LOG = LoggerFactory.getLogger(TransferData::class.java)
    }
}