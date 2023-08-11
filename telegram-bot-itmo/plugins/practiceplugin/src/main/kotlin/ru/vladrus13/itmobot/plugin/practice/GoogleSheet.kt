package ru.vladrus13.itmobot.plugin.practice

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.model.*
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getGreenAcceptedTask
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getGreenCountTasksColor
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getGreenScores
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getRedScores
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getWhiteColor
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getYellowDeclinedTask
import ru.vladrus13.itmobot.plugin.practice.tablemaker.ColorMaker.Companion.getYellowScores
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.createGridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getPrettyRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getRequests
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.nToAZ
import ru.vladrus13.itmobot.plugin.practice.tablemaker.Rectangle

class GoogleSheet(private val service: Sheets, private val id: String, private val students: List<String>) {
    fun generateMainSheet() {
        // rename list to $MAIN_LIST_NAME
        val properties = SheetProperties().setIndex(0).setTitle(MAIN_LIST_NAME)
        val update = UpdateSheetPropertiesRequest()
            .setProperties(properties)
            .setFields("title")
        val requests = listOf(Request().setUpdateSheetProperties(update))
        val req = BatchUpdateSpreadsheetRequest()
            .setRequests(requests)
        service.spreadsheets().batchUpdate(
            id,
            req
        ).execute()

        fillInStudents(
            students,
            MAIN_LIST_NAME
        ) { ind -> "=$SUM_FORMULA($FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_CHAR$ind:$ind)*$SCORES_FOR_DISCRETE_MATH_TASK" }

        // Conditional Format
        executeRequestsSequence(
            listOf(
                getConditionalFormatRequest(
                    MAIN_LIST_NAME,
                    MIN_STUDENT_ROW_INDEX, MIN_STUDENT_ROW_INDEX + students.size,
                    TOTAL_SCORES_COLUMN_INDEX, TOTAL_SCORES_COLUMN_NUMBER,
                    {
                        it.setGradientRule(
                            GradientRule()
                                .setMinpoint(
                                    InterpolationPoint()
                                        .setType(INTERPOLATION_POINT_TYPE_MIN)
                                        .setColor(getRedScores())
                                )
                                .setMidpoint(
                                    InterpolationPoint()
                                        .setType(INTERPOLATION_POINT_TYPE_PERCENTILE)
                                        .setColor(getYellowScores())
                                        .setValue("50")
                                )
                                .setMaxpoint(
                                    InterpolationPoint()
                                        .setType(INTERPOLATION_POINT_TYPE_MAX)
                                        .setColor(getGreenScores())
                                )
                        )
                    }
                )
            )
        )
    }

    fun generateSheet(tasks: List<String>) {
        val maxStudentRowIndex = students.size
        val maxStudentRowNumber = maxStudentRowIndex + 1
        val lastRowIndex = students.size + 1
        val lastRowNumber = lastRowIndex + 1

        // make new list
        val homeworkCount = service.spreadsheets().get(id).execute().sheets.size
        val title = "Д$homeworkCount"
        val properties = SheetProperties().setTitle(title)
        executeRequestsSequence(
            listOf(
                Request().setAddSheet(AddSheetRequest().setProperties(properties)),
            )
        )

        fillInStudents(
            students,
            title
        ) { ind -> "=$MAIN_LIST_NAME!$TOTAL_SCORES_COLUMN_CHAR$ind" }

        val listBody = mutableListOf(mutableListOf(ONE_PRACTICE_TASKS_COLUMN_NAME) + tasks)
        for (rowNumber in MIN_STUDENT_ROW_NUMBER..maxStudentRowNumber) {
            listBody.add(mutableListOf("=$COUNT_A_FORMULA($TASK_FIRST_COLUMN_CHAR$rowNumber:$rowNumber)"))
        }
        val width = listBody[0].size

        val lastRow =
            mutableListOf("=$COUNT_IF_FORMULA($TASK_FIRST_COLUMN_CHAR$lastRowNumber:$lastRowNumber; \">0\")")
        for (taskIndex in tasks.indices) {
            val columnName = nToAZ(taskIndex + TASK_FIRST_COLUMN_INDEX)
            lastRow.add("=$COUNT_A_FORMULA($columnName$MIN_STUDENT_ROW_NUMBER:$columnName$maxStudentRowNumber)")
        }
        listBody.add(lastRow)

        val body = ValueRange().setValues(listBody.toList())
        service.spreadsheets().values()
            .update(
                id,
                getPrettyRange(
                    title,
                    TASKS_NAMES_ROW_INDEX,
                    lastRowNumber,
                    TASK_COUNTER_COLUMN_INDEX,
                    TASK_COUNTER_COLUMN_INDEX + width
                ),
                body
            )
            .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
            .execute()

        addNewMainListColumn(students, title)

        // Conditional Format
        executeRequestsSequence(
            listOf(
                getListRules(
                    title, MIN_STUDENT_ROW_INDEX,
                    maxStudentRowNumber,
                    TASK_COUNTER_COLUMN_INDEX,
                    TASK_COUNTER_COLUMN_NUMBER,
                    {
                        it.setGradientRule(
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
                    },
                    {
                        it.setBooleanRule(
                            BooleanRule()
                                .setCondition(
                                    BooleanCondition()
                                        .setType(CONDITION_TYPE.NUMBER_EQ.toString())
                                        .setValues(listOf(ConditionValue().setUserEnteredValue("0")))
                                )
                                .setFormat(CellFormat().setBackgroundColor(getWhiteColor()))
                        )
                    }
                ),
                getListRules(
                    title, lastRowIndex, lastRowNumber,
                    TASK_FIRST_COLUMN_INDEX, listBody.size,
                    *getListBooleanConditionsOfAcceptedOrNotTask(
                        "$TASK_FIRST_COLUMN_CHAR$MIN_STUDENT_ROW_NUMBER:$TASK_FIRST_COLUMN_CHAR$maxStudentRowNumber"
                    ).map { it }.toTypedArray()
                ),
                getListRules(
                    title, MIN_STUDENT_ROW_INDEX, maxStudentRowNumber,
                    FCS_COLUMN_INDEX, FCS_COLUMN_NUMBER,
                    *getListBooleanConditionsOfAcceptedOrNotTask(
                        "$TASK_FIRST_COLUMN_CHAR$MIN_STUDENT_ROW_NUMBER:$MIN_STUDENT_ROW_NUMBER"
                    ).map { it }.toTypedArray()
                ),
                getListRules(
                    title, MIN_STUDENT_ROW_INDEX, maxStudentRowNumber,
                    TASK_FIRST_COLUMN_INDEX, listBody.size,
                    *getListBooleanConditionsOfAcceptedOrNotTaskExactlyTOrP().map { it }.toTypedArray()
                ),
                getRequests(
                    service, id, title,
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, lastRowNumber, FCS_COLUMN_INDEX, TASK_COUNTER_COLUMN_INDEX + width,
                        listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells)
                    ),
                    Rectangle(
                        MIN_STUDENT_ROW_INDEX, lastRowNumber, TASK_COUNTER_COLUMN_INDEX, TASK_COUNTER_COLUMN_NUMBER,
                        listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells)
                    ),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_NUMBER, TASK_FIRST_COLUMN_INDEX, TASK_COUNTER_COLUMN_INDEX + width,
                        listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells)
                    ),
                    Rectangle(
                        lastRowIndex, lastRowNumber, TASK_COUNTER_COLUMN_INDEX, TASK_COUNTER_COLUMN_INDEX + width,
                        listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells)
                    )
                )
            )
            .flatten()
        )
    }

    fun getTasksList(): List<List<String>> {
        val tasksList = mutableListOf<List<String>>()
        for (i in 1..30) {
            val result: ValueRange
            try {
                result = getValueRange("Д$i!$TASKS_NAMES_ROW_NUMBER:$TASKS_NAMES_ROW_NUMBER")
            } catch (e: GoogleJsonResponseException) {
                break
            }

            val currentTasksNames = result.getValues().first().map(Any::toString)
            tasksList.add(currentTasksNames.subList(TASK_FIRST_COLUMN_INDEX, currentTasksNames.size))
        }
        return tasksList
    }

    private fun getValueRange(range: String) = service
        .spreadsheets()
        .values()
        .get(id, range)
        .execute()

    private fun getListRules(
        sheetTitle: String,
        firstRow: Int,
        lastRow: Int,
        firstColumn: Int,
        lastColumn: Int,
        vararg addRule: (ConditionalFormatRule) -> ConditionalFormatRule
    ): List<Request> = addRule.map {
        getConditionalFormatRequest(
            sheetTitle,
            firstRow,
            lastRow,
            firstColumn,
            lastColumn,
            it
        )
    }

    private fun getListBooleanConditionsOfAcceptedOrNotTask(range: String): List<(ConditionalFormatRule) -> ConditionalFormatRule> =
        listOf(
            {
                it.setBooleanRule(
                    BooleanRule()
                        .setCondition(
                            getCustomFormulaBooleanCondition("=${getCountIfRussianEnglishIsT(range)}")
                        )
                        .setFormat(CellFormat().setBackgroundColor(getGreenAcceptedTask()))
                )
            },
            {
                it.setBooleanRule(
                    BooleanRule()
                        .setCondition(
                            getCustomFormulaBooleanCondition("=${getCountIfRussianEnglishIsP(range)}")
                        )
                        .setFormat(CellFormat().setBackgroundColor(getYellowDeclinedTask()))
                )
            }
        )

    private fun getListBooleanConditionsOfAcceptedOrNotTaskExactlyTOrP(): List<(ConditionalFormatRule) -> ConditionalFormatRule> =
        listOf(
            {
                it.setBooleanRule(
                    BooleanRule()
                        .setCondition(
                            BooleanCondition()
                                .setType(CONDITION_TYPE.TEXT_EQ.toString())
                                .setValues(listOf(ConditionValue().setUserEnteredValue("T")))
                        )
                        .setFormat(CellFormat().setBackgroundColor(getGreenAcceptedTask()))
                )

            },
            {
                it.setBooleanRule(
                    BooleanRule()
                        .setCondition(
                            BooleanCondition()
                                .setType(CONDITION_TYPE.TEXT_EQ.toString())
                                .setValues(listOf(ConditionValue().setUserEnteredValue("P")))
                        )
                        .setFormat(CellFormat().setBackgroundColor(getYellowDeclinedTask()))
                )
            }
        )

    private fun getConditionalFormatRequest(
        sheetTitle: String,
        firstRow: Int,
        lastRow: Int,
        firstColumn: Int,
        lastColumn: Int,
        addRule: (ConditionalFormatRule) -> ConditionalFormatRule
    ): Request = Request().setAddConditionalFormatRule(
        AddConditionalFormatRuleRequest().setRule(
            addRule(
                ConditionalFormatRule()
                    .setRanges(
                        listOf(
                            createGridRequestMaker(
                                service,
                                id, sheetTitle,
                                firstRow, lastRow,
                                firstColumn, lastColumn
                            ).range
                        )
                    )
            )
        )
    )

    private fun getCustomFormulaBooleanCondition(userEnteredValue: String) = BooleanCondition()
        .setType(CONDITION_TYPE.CUSTOM_FORMULA.toString())
        .setValues(listOf(ConditionValue().setUserEnteredValue(userEnteredValue)))

    private fun executeRequestsSequence(requests: List<Request>) {
        val req = BatchUpdateSpreadsheetRequest().setRequests(requests)
        service.spreadsheets().batchUpdate(id, req).execute()
    }

    private fun addNewMainListColumn(
        students: List<String>,
        titleSheet: String
    ) {
        val maxStudentRowIndex = students.size
        val maxStudentRowNumber = maxStudentRowIndex + 1

        val width: Int
        try {
            val result = getValueRange(
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
            val range = "$titleSheet!$TASK_FIRST_COLUMN_CHAR$studentRowNumber:$studentRowNumber"
            // There are russian 'Т' and English 'T'.
            listBody.add(mutableListOf("=${getCountIfRussianEnglishIsT(range)}"))
        }

        val body = ValueRange().setValues(listBody.toList())
        service.spreadsheets().values()
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

        // format new column
        val requests = getRequests(
            service, id, MAIN_LIST_NAME,
            Rectangle(
                TASKS_NAMES_MAIN_LIST_ROW_INDEX, maxStudentRowNumber, width, width + 1,
                listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells)
            ),
            Rectangle(
                TASKS_NAMES_MAIN_LIST_ROW_INDEX, TASKS_NAMES_MAIN_LIST_ROW_NUMBER, width, width + 1,
                listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells)
            )
        )

        executeRequestsSequence(requests)
    }

    private fun getCountIfRussianEnglishIsT(range: String) =
        "${getCountIf(range, RUSSIAN_T)} + ${getCountIf(range, ENGLISH_T)}"

    private fun getCountIfRussianEnglishIsP(range: String) =
        "${getCountIf(range, RUSSIAN_P)} + ${getCountIf(range, ENGLISH_P)}"

    private fun getCountIf(range: String, char: Char) = "$COUNT_IF_FORMULA($range;\"$char\")"

    /**
     * @param title is "Д[0-9]+" or <code>MAIN_LIST_NAME</code>
     */
    private fun fillInStudents(
        students: List<String>,
        title: String,
        getTotalCount: (Int) -> String
    ) {
        val listBody = mutableListOf(listOf(FCS_COLUMN_NAME, TOTAL_SCORES_COLUMN_NAME))
        listBody.addAll(students.mapIndexed { studentIndex, name ->
            listOf(
                name,
                getTotalCount(MIN_STUDENT_ROW_NUMBER + studentIndex)
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
        service.spreadsheets().values()
            .update(id, range, body)
            .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
            .execute()

        val requests = getRequests(
            service, id, title,
            Rectangle(
                TASKS_NAMES_ROW_INDEX, listBody.size, FCS_COLUMN_INDEX, FCS_COLUMN_NUMBER,
                listOf(GridRequestMaker::colorizeBorders)
            ),
            Rectangle(
                TASKS_NAMES_ROW_INDEX, listBody.size, TOTAL_SCORES_COLUMN_INDEX, TOTAL_SCORES_COLUMN_NUMBER,
                listOf(GridRequestMaker::colorizeBorders)
            ),
            Rectangle(
                TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_NUMBER, FCS_COLUMN_INDEX, listBody.first().size,
                listOf(GridRequestMaker::colorizeBorders)
            ),
            Rectangle(
                TASKS_NAMES_ROW_INDEX, listBody.size, FCS_COLUMN_INDEX, TOTAL_SCORES_COLUMN_NUMBER,
                listOf(GridRequestMaker::formatCells)
            ),
        )

        executeRequestsSequence(requests)
    }

    companion object {
        enum class WHO_ENTERED {
            USER_ENTERED,
        }

        enum class CONDITION_TYPE {
            NUMBER_EQ,
            CUSTOM_FORMULA,
            TEXT_EQ
        }

        private const val INTERPOLATION_POINT_TYPE_MIN = "MIN"
        private const val INTERPOLATION_POINT_TYPE_MAX = "MAX"
        private const val INTERPOLATION_POINT_TYPE_PERCENTILE = "PERCENTILE"

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

        private val RUSSIAN_T = 'Т'
        private val ENGLISH_T = 'T'
        private val RUSSIAN_P = 'Р'
        private val ENGLISH_P = 'P'
    }
}