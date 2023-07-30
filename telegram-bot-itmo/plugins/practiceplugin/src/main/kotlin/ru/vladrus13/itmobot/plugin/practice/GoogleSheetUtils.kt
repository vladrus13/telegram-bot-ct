package ru.vladrus13.itmobot.plugin.practice

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getGreenCountTasksColor
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getWhiteColor
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.createGridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getPrettyRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.nToAZ
import java.util.function.Function

class GoogleSheetUtils {
    companion object {
        enum class WHO_ENTERED {
            USER_ENTERED,
        }

        private const val INTERPOLATION_POINT_TYPE_MIN = "MIN"
        private const val INTERPOLATION_POINT_TYPE_MAX = "MAX"

        private const val COUNT_A_FORMULA = "COUNTA"
        private const val COUNT_IF_FORMULA = "COUNTIF"
        private const val SUM_FORMULA = "SUM"

        private const val MAIN_LIST_NAME = "Results"
        private const val FCS_COLUMN_NAME = "ФИО"
        private const val TOTAL_SCORES_COLUMN_NAME = "Total"
        private const val ONE_PRACTICE_TASKS_COLUMN_NAME = "S"
        private const val SCORES_FOR_DISCRETE_MATH_TASK: Int = 5

        private const val MIN_STUDENT_ROW_INDEX = 1
        private const val TASKS_NAMES_ROW_INDEX = 0
        private const val TASKS_NAMES_MAIN_LIST_ROW_INDEX = 0
        private const val MIN_STUDENT_ROW_NUMBER = MIN_STUDENT_ROW_INDEX + 1
        private const val TASKS_NAMES_ROW_NUMBER = TASKS_NAMES_ROW_INDEX + 1
        private const val TASKS_NAMES_MAIN_LIST_ROW_NUMBER = TASKS_NAMES_MAIN_LIST_ROW_INDEX + 1

        private const val FCS_COLUMN_INDEX = 0
        private const val TOTAL_SCORES_COLUMN_INDEX = 1
        private const val TASK_COUNTER_COLUMN_INDEX = 2
        private const val TASK_FIRST_COLUMN_INDEX = 3
        private const val FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_INDEX = 2
        private const val FCS_COLUMN_NUMBER = FCS_COLUMN_INDEX + 1
        private const val TOTAL_SCORES_COLUMN_NUMBER = TOTAL_SCORES_COLUMN_INDEX + 1
        private const val TASK_COUNTER_COLUMN_NUMBER = TASK_COUNTER_COLUMN_INDEX + 1
        private const val TASK_FIRST_COLUMN_NUMBER = TASK_FIRST_COLUMN_INDEX + 1
        private const val FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_NUMBER = FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_INDEX + 1

        private val FCS_COLUMN_CHAR = nToAZ(FCS_COLUMN_INDEX)
        private val TOTAL_SCORES_COLUMN_CHAR = nToAZ(TOTAL_SCORES_COLUMN_INDEX)
        private val TASK_COUNTER_COLUMN_CHAR = nToAZ(TASK_COUNTER_COLUMN_INDEX)
        private val TASK_FIRST_COLUMN_CHAR = nToAZ(TASK_FIRST_COLUMN_INDEX)
        private val FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_CHAR =
            nToAZ(FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_INDEX)

        private fun getValueRange(sheetsService: Sheets, id: String, range: String) = sheetsService
            .spreadsheets()
            .values()
            .get(id, range)
            .execute()

        fun getTasksList(sheetsService: Sheets, id: String): List<List<String>> {
            val tasksList = mutableListOf<List<String>>()
            for (i in 1..30) {
                val result: ValueRange
                try {
                    result = getValueRange(sheetsService, id, "Д$i!$TASKS_NAMES_ROW_NUMBER:$TASKS_NAMES_ROW_NUMBER")
                } catch (e: GoogleJsonResponseException) {
                    break
                }

                val currentTasksNames = result.getValues().first().map(Any::toString)
                tasksList.add(currentTasksNames.subList(TASK_FIRST_COLUMN_INDEX, currentTasksNames.size))
            }
            return tasksList
        }

        fun generateSheet(sheetsService: Sheets, id: String, students: List<String>, tasks: List<String>) {
            val maxStudentRowIndex = students.size
            val maxStudentRowNumber = maxStudentRowIndex + 1
            val lastRowIndex = students.size + 1
            val lastRowNumber = lastRowIndex + 1

            // make new list
            val homeworkCount = sheetsService.spreadsheets().get(id).execute().sheets.size
            val title = "Д$homeworkCount"
            val properties = SheetProperties().setTitle(title)
            executeRequestsSequence(
                sheetsService, id, listOf(
                    Request().setAddSheet(AddSheetRequest().setProperties(properties)),

                    )
            )
            executeRequestsSequence(
                sheetsService, id, listOf(
                    Request().setAddConditionalFormatRule(
                        AddConditionalFormatRuleRequest().setRule(
                            ConditionalFormatRule()
                                .setRanges(
                                    listOf(
                                        createGridRequestMaker(
                                            sheetsService,
                                            id,
                                            title,
                                            MIN_STUDENT_ROW_INDEX,
                                            maxStudentRowNumber,
                                            TASK_COUNTER_COLUMN_INDEX,
                                            TASK_COUNTER_COLUMN_NUMBER
                                        ).range
                                    )
                                )
                                .setGradientRule(
                                    GradientRule()
                                        .setMinpoint(
                                            InterpolationPoint()
                                                .setType(INTERPOLATION_POINT_TYPE_MIN)
                                                .setColor(getWhiteColor())
                                        )
                                        .setMaxpoint(
                                            InterpolationPoint()
                                                .setType(INTERPOLATION_POINT_TYPE_MAX)
                                                .setColor(getGreenCountTasksColor())
                                        )
                                )
                        )
                    ),
                )
            )

            fillInStudents(
                sheetsService,
                id,
                students,
                title
            ) { ind -> "=$MAIN_LIST_NAME!$TOTAL_SCORES_COLUMN_CHAR$ind" }

            val listBody = mutableListOf(mutableListOf(ONE_PRACTICE_TASKS_COLUMN_NAME) + tasks)
            for (rowNumber in MIN_STUDENT_ROW_NUMBER..maxStudentRowNumber) {
                listBody.add(mutableListOf("=$COUNT_A_FORMULA($TASK_FIRST_COLUMN_CHAR$rowNumber:$rowNumber)"))
            }

            val lastRow =
                mutableListOf("=$COUNT_IF_FORMULA($TASK_FIRST_COLUMN_CHAR$lastRowNumber:$lastRowNumber; \">0\")")
            for (taskIndex in tasks.indices) {
                val columnName = nToAZ(taskIndex + TASK_FIRST_COLUMN_INDEX)
                lastRow.add("=$COUNT_A_FORMULA($columnName$MIN_STUDENT_ROW_NUMBER:$columnName$maxStudentRowNumber)")
            }
            listBody.add(lastRow)

            val body = ValueRange().setValues(listBody.toList())
            sheetsService.spreadsheets().values()
                .update(
                    id,
                    getPrettyRange(
                        title,
                        TASKS_NAMES_ROW_INDEX,
                        lastRowNumber,
                        TASK_COUNTER_COLUMN_INDEX,
                        TASK_COUNTER_COLUMN_INDEX + listBody[0].size
                    ),
                    body
                )
                .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
                .execute()

            addNewMainListColumn(sheetsService, id, students, title)
        }

        private fun executeRequestsSequence(service: Sheets, id: String, listRequests: List<Request>) {
            listRequests.forEach { request: Request ->
                val req = BatchUpdateSpreadsheetRequest().setRequests(listOf(request))
                service.spreadsheets().batchUpdate(id, req).execute()
            }
        }

        private fun addNewMainListColumn(
            sheetsService: Sheets,
            id: String,
            students: List<String>,
            titleSheet: String
        ) {
            val maxStudentRowIndex = students.size
            val maxStudentRowNumber = maxStudentRowIndex + 1

            val width: Int
            try {
                val result = getValueRange(
                    sheetsService,
                    id,
                    "$MAIN_LIST_NAME!$TASKS_NAMES_MAIN_LIST_ROW_NUMBER:$TASKS_NAMES_MAIN_LIST_ROW_NUMBER"
                )
                width = result.getValues()[0].size
            } catch (e: GoogleJsonResponseException) {
                return
            } catch (e: IndexOutOfBoundsException) {
                return
            }

            val listBody = mutableListOf(mutableListOf(titleSheet))
            for (studentRowNumber in students.indices.map { index -> MIN_STUDENT_ROW_NUMBER + index }) {
                val range =
                    "$titleSheet!$TASK_FIRST_COLUMN_CHAR$studentRowNumber:$studentRowNumber"
                // There are russian 'Т' and English 'T'.
                listBody.add(mutableListOf("=${getCountIf(range, 'T')} + ${getCountIf(range, 'Т')}"))
            }

            val body = ValueRange().setValues(listBody.toList())
            sheetsService.spreadsheets().values()
                .update(
                    id,
                    getPrettyRange(
                        MAIN_LIST_NAME,
                        TASKS_NAMES_MAIN_LIST_ROW_INDEX,
                        maxStudentRowNumber,
                        width,
                        width + 1
                    ),
                    body
                )
                .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
                .execute()
        }

        private fun getCountIf(range: String, char: Char) = "$COUNT_IF_FORMULA($range;\"$char\")"

        fun generateMainSheet(sheetsService: Sheets, id: String, students: List<String>) {
            // rename list to $MAIN_LIST_NAME
            val properties = SheetProperties().setIndex(0).setTitle(MAIN_LIST_NAME)
            val update = UpdateSheetPropertiesRequest()
                .setProperties(properties)
                .setFields("title")
            val requests = listOf(Request().setUpdateSheetProperties(update))
            val req = BatchUpdateSpreadsheetRequest()
                .setRequests(requests)
            sheetsService.spreadsheets().batchUpdate(
                id,
                req
            ).execute()

            fillInStudents(
                sheetsService,
                id,
                students,
                MAIN_LIST_NAME
            ) { ind -> "=$SUM_FORMULA($FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_CHAR$ind:$ind)*$SCORES_FOR_DISCRETE_MATH_TASK" }
        }

        /**
         * @param title is "Д[0-9]+" or <code>MAIN_LIST_NAME</code>
         */
        private fun fillInStudents(
            sheetsService: Sheets,
            id: String,
            students: List<String>,
            title: String,
            getTotalCount: Function<Int, String>
        ) {
            val listBody = mutableListOf(listOf(FCS_COLUMN_NAME, TOTAL_SCORES_COLUMN_NAME))
            listBody.addAll(students.mapIndexed { studentIndex, name ->
                listOf(
                    name,
                    getTotalCount.apply(MIN_STUDENT_ROW_NUMBER + studentIndex)
                )
            })
            val body = ValueRange().setValues(listBody.toList())
            val range = getPrettyRange(
                title,
                TASKS_NAMES_MAIN_LIST_ROW_INDEX,
                listBody.size,
                FCS_COLUMN_INDEX,
                TOTAL_SCORES_COLUMN_NUMBER
            )
            sheetsService.spreadsheets().values()
                .update(id, range, body)
                .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
                .execute()

            val requests = mutableListOf<Request>()
            // Border
            requests.add(
                createGridRequestMaker(
                    sheetsService,
                    id,
                    title,
                    TASKS_NAMES_ROW_INDEX,
                    listBody.size,
                    FCS_COLUMN_INDEX,
                    FCS_COLUMN_NUMBER
                ).colorizeBorders()
            )
            requests.add(
                createGridRequestMaker(
                    sheetsService,
                    id,
                    title,
                    TASKS_NAMES_ROW_INDEX,
                    listBody.size,
                    TOTAL_SCORES_COLUMN_INDEX,
                    TOTAL_SCORES_COLUMN_NUMBER
                ).colorizeBorders()
            )
            requests.add(
                createGridRequestMaker(
                    sheetsService,
                    id,
                    title,
                    TASKS_NAMES_ROW_INDEX,
                    TASKS_NAMES_ROW_NUMBER,
                    FCS_COLUMN_INDEX,
                    listBody.first().size
                ).colorizeBorders()
            )
            // Align
            requests.add(
                createGridRequestMaker(
                    sheetsService,
                    id,
                    title,
                    TASKS_NAMES_ROW_INDEX,
                    listBody.size,
                    FCS_COLUMN_INDEX,
                    TOTAL_SCORES_COLUMN_NUMBER
                ).formatCells()
            )
            val req = BatchUpdateSpreadsheetRequest().setRequests(requests.toList())
            sheetsService.spreadsheets().batchUpdate(
                id,
                req
            ).execute()
        }
    }
}