package ru.vladrus13.itmobot.plugin.practice.transfer

import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet.Companion.ENGLISH_T
import ru.vladrus13.itmobot.plugin.practice.googleapi.GoogleSheet.Companion.RUSSIAN_T

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
        fun List<List<String>>.transferStudentTableToTeacher(): List<List<String>> {
            if (this.isEmpty()) return listOf()

            val tasks = this.first()
            val studentTableWithSkips = this
                .asSequence()
                .drop(1)
                .map { studentResults ->
                    studentResults
                        .asSequence()
                        .mapIndexed { id, resultTask -> TupleFCSTaskResult(studentResults[0], tasks[id], resultTask) }
                        .filterIndexed { id, tuple -> id > 0 && isT(tuple.result) }
                        .map { tuple -> Pair(tuple.task, tuple.fcs) }
                        .toList()
                }
                .flatten()
                .sortedBy { pair -> pair.first.toInt() }
                .map { pair -> listOf(pair.first, pair.second) }
                .toList()

            val existsNumbers = studentTableWithSkips
                .map { it.first() }
                .map(String::toInt)
                .sorted()
            val notExistsNumbers = (1..existsNumbers.max())
                .toList()
                .filter { existsNumbers.binarySearch(it) < 0 }
                .map { listOf(it.toString(), "") }
            return studentTableWithSkips.plus(notExistsNumbers).sortedBy { it.first().toInt() }
        }

        fun List<List<String>>.transferFCSToLastName(): List<List<String>> =
            this.map {
                it.mapIndexed { index, s ->
                    if (index == 1) s.takeWhile(Char::isLetter)
                    else s
                }
            }


        private fun isT(element: String) = RUSSIAN_T == element || ENGLISH_T == element
    }
}