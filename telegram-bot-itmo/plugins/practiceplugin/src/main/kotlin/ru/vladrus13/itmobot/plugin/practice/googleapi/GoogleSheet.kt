package ru.vladrus13.itmobot.plugin.practice.googleapi

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
import ru.vladrus13.itmobot.plugin.practice.tablemaker.FormattedRectangle
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.createGridRequestMaker
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getPrettyLongRowRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getPrettyRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getTitlePrettyCell
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getTitlePrettyLongRowRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getTitlePrettyOnlyRowRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.GridRequestMaker.Companion.getTitlePrettyRange
import ru.vladrus13.itmobot.plugin.practice.tablemaker.Rectangle
import ru.vladrus13.itmobot.properties.InitialProperties
import java.util.logging.Logger
import kotlin.math.max

class GoogleSheet(private val service: Sheets, private val id: String) {
    private val logger: Logger = InitialProperties.logger
    private val MAX_STUDENTS_COUNT = 500

    fun generateMainSheet(students: List<String>) {
        // rename list to $MAIN_LIST_NAME
        val properties = SheetProperties().setIndex(0).setTitle(MAIN_LIST_NAME)
        val update = UpdateSheetPropertiesRequest()
            .setProperties(properties)
            .setFields("title")
        executeRequestsSequence(Request().setUpdateSheetProperties(update))

        fillInStudents(MAIN_LIST_NAME, Companion::getSumScoresFormula, students)

        // Conditional Format
        executeRequestsSequence(
            getConditionalFormatRequest(
                MAIN_LIST_NAME,
                MIN_STUDENT_ROW_INDEX, MIN_STUDENT_ROW_INDEX + students.size,
                TOTAL_SCORES_COLUMN_INDEX, TOTAL_SCORES_COLUMN_INDEX + 1
            ) { it.setGradientRule(MAIN_SCORES_GRADIENT) }
        )
    }

    fun generateTeacherSheet(): Boolean {
        val sheets = service.spreadsheets().get(id).execute().sheets
        if (sheets.any { it.properties.title == TEACHER_LIST_NAME }) {
            return false
        }

        val properties = SheetProperties().setTitle(TEACHER_LIST_NAME).setIndex(sheets.size)
        executeRequestsSequence(Request().setAddSheet(AddSheetRequest().setProperties(properties)))
        return true
    }

    fun updateFields(teacherTable: List<List<String>>) {
        updateBody(
            getTitlePrettyRange(TEACHER_LIST_NAME, 0, teacherTable.size, 0, teacherTable.first().size),
            teacherTable,
        )
    }

    fun generateSheet(tasks: List<String>) {
        val students = getStudentList()
        val maxStudentRowIndex = students.size
        val taskCounterRowIndex = maxStudentRowIndex + 1
        val lastRowIndex = students.size + 1

        // make new list
        val homeworkCount = service.spreadsheets().get(id).execute().sheets.size + 1 - EXCESS_SHEETS
        val title = "Д$homeworkCount"
        val properties = SheetProperties().setTitle(title).setIndex(1)
        val lastTaskColumnIndex = TASK_COUNTER_COLUMN_INDEX + tasks.size
        executeRequestsSequence(Request().setAddSheet(AddSheetRequest().setProperties(properties)))

        fillInStudents(title, Companion::getActualScoreFormula, students)

        val body = listOf(listOf(ONE_PRACTICE_TASKS_COLUMN_NAME) + tasks) +
                (MIN_STUDENT_ROW_INDEX..maxStudentRowIndex).map {
                    listOf(getCountAFormula(getPrettyLongRowRange(it, it + 1, TASK_FIRST_COLUMN_INDEX)))
                } +
                listOf(
                    listOf(
                        getCountIfFormula(
                            getPrettyLongRowRange(lastRowIndex, lastRowIndex + 1, TASK_FIRST_COLUMN_INDEX), ">0"
                        )
                    ) + tasks.indices.map { it + TASK_FIRST_COLUMN_INDEX }.map {
                        getCountAFormula(
                            getPrettyRange(MIN_STUDENT_ROW_INDEX, maxStudentRowIndex + 1, it, it + 1)
                        )
                    }
                )

        updateBody(
            getTitlePrettyRange(
                title,
                TASKS_NAMES_ROW_INDEX, lastRowIndex + 1,
                TASK_COUNTER_COLUMN_INDEX, lastTaskColumnIndex + 1
            ),
            body
        )

        addNewMainListColumn(title, students)

        // Conditional Format
        executeRequestsSequence(
            *colorCellsByTaskGreenGradient(
                title, MIN_STUDENT_ROW_INDEX, maxStudentRowIndex + 1,
                TASK_COUNTER_COLUMN_INDEX, TASK_COUNTER_COLUMN_INDEX + 1
            ).toTypedArray(),
            *colorCellsByTaskGreenGradient(
                title, taskCounterRowIndex, taskCounterRowIndex + 1,
                TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1
            ).toTypedArray(),
            *getListRules(
                title,
                lastRowIndex, lastRowIndex + 1,
                TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1,
                *getListBooleanConditionsOfAcceptedOrNotTask(
                    getPrettyRange(
                        MIN_STUDENT_ROW_INDEX, maxStudentRowIndex + 1,
                        TASK_FIRST_COLUMN_INDEX, TASK_FIRST_COLUMN_INDEX + 1
                    )
                ).map { it }.toTypedArray()
            ).toTypedArray(),
            *getListRules(
                title,
                MIN_STUDENT_ROW_INDEX, maxStudentRowIndex + 1,
                FCS_COLUMN_INDEX, FCS_COLUMN_INDEX + 1,
                *getListBooleanConditionsOfAcceptedOrNotTask(
                    getPrettyLongRowRange(MIN_STUDENT_ROW_INDEX, MIN_STUDENT_ROW_INDEX + 1, TASK_FIRST_COLUMN_INDEX)
                ).map { it }.toTypedArray()
            ).toTypedArray(),
            *getListRules(
                title,
                MIN_STUDENT_ROW_INDEX, maxStudentRowIndex + 1,
                TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1,
                *getListBooleanConditionsOfAcceptedOrNotTaskExactlyTOrP().map { it }.toTypedArray()
            ).toTypedArray(),
            *getRequests(
                title,
                *getEqualsActionsRectangles(
                    listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, lastRowIndex + 1,
                        FCS_COLUMN_INDEX, lastTaskColumnIndex + 1
                    ),
                    Rectangle(
                        MIN_STUDENT_ROW_INDEX, lastRowIndex + 1,
                        TASK_COUNTER_COLUMN_INDEX, TASK_COUNTER_COLUMN_INDEX + 1
                    ),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_INDEX + 1,
                        TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1
                    ),
                    Rectangle(
                        lastRowIndex, lastRowIndex + 1,
                        TASK_COUNTER_COLUMN_INDEX, lastTaskColumnIndex + 1
                    )
                ).toTypedArray(),

                *getEqualsActionsRectangles(
                    listOf { grid -> grid.setWidth(37) },
                    Rectangle(
                        NONE_INDEX, NONE_INDEX,
                        TASK_COUNTER_COLUMN_INDEX, TASK_COUNTER_COLUMN_INDEX + 1
                    )
                ).toTypedArray(),
                *getEqualsActionsRectangles(
                    listOf { grid -> grid.setWidth(32) },
                    Rectangle(
                        NONE_INDEX, NONE_INDEX,
                        TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1
                    )
                ).toTypedArray(),
            ).toTypedArray(),
        )
    }

    private fun colorCellsByTaskGreenGradient(
        title: String,
        beginRowIndex: Int, endRowIndex: Int,
        beginColumnIndex: Int, endColumnIndex: Int
    ) = getListRules(
        title, beginRowIndex,
        endRowIndex,
        beginColumnIndex,
        endColumnIndex,
        { it.setGradientRule(LOCAL_SCORED_GRADIENT) },
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
    )

    private fun getStudentList(): List<String> = try {
        getValueRange("$MAIN_LIST_NAME!A${MIN_STUDENT_ROW_INDEX + 1}:A$MAX_STUDENTS_COUNT")
            .getValues()
            .map { list -> list.first() }
            .map(Any::toString)
            .filter(String::isNotBlank)
    } catch (_: GoogleJsonResponseException) {
        logger.info("Not found students in google sheet")
        emptyList()
    }

    /*
         * Return table like this
         * |         | 1 | 2 | 3 | 4 |
         * | Alexey  | 1 | T |   | 1 |
         * | Daniil  | T |   | P | T |
         *
     */
    fun getFCSTasksWithMarks(): List<List<String>> {
        val students = listOf("").plus(getStudentList())

        val allPracticesTasksWithAnswers = List(students.size) { mutableListOf<String>() }
        for (i in 1..MAX_WEEK_DAY) {
            val onePracticeSheet: List<List<Any>>
            try {
                onePracticeSheet =
                    getValueRange("Д$i!${TASKS_NAMES_ROW_INDEX + 1}:${TASKS_NAMES_ROW_INDEX + students.size}")
                        .getValues()
            } catch (e: GoogleJsonResponseException) {
                break
            }

            val onePracticeTasksWithAnswers =
                onePracticeSheet.map {
                    val missingEmptyCells = List(max(0, onePracticeSheet.first().size - it.size)) { "" }
                    it
                        .asSequence()
                        .plus(missingEmptyCells)
                        .map(Any::toString)
                        .filterIndexed { index, _ ->
                            index !in setOf(
                                FCS_COLUMN_INDEX,
                                TASK_COUNTER_COLUMN_INDEX,
                                TOTAL_SCORES_COLUMN_INDEX
                            )
                        }.toList()
                }
            allPracticesTasksWithAnswers.mapIndexed { index, list -> list.addAll(onePracticeTasksWithAnswers[index]) }
        }
        return allPracticesTasksWithAnswers.mapIndexed { index, array -> listOf(students[index]).plus(array) }
    }

    fun getTeacherList(): List<List<String>> {
        val result: ValueRange
        try {
            result = getValueRange("$TEACHER_LIST_NAME!A:B")
        } catch (_: GoogleJsonResponseException) {
            logger.warning("Can't parse teacher sheet")
            return listOf()
        }
        return result.getValues()?.map { it.map(Any::toString) } ?: listOf()
    }

    private fun updateBody(range: String, body: List<List<String>>) = service
        .spreadsheets()
        .values()
        .update(id, range, ValueRange().setValues(body))
        .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())
        .execute()

    private fun getValueRange(range: String) = getValueRange(range, service, id)

    private fun getListRules(
        sheetTitle: String,
        firstRow: Int,
        lastRow: Int,
        firstColumn: Int,
        lastColumn: Int,
        vararg addRule: (ConditionalFormatRule) -> ConditionalFormatRule
    ): List<Request> = addRule.map {
        getConditionalFormatRequest(sheetTitle, firstRow, lastRow, firstColumn, lastColumn, it)
    }

    private fun getListBooleanConditionsOfAcceptedOrNotTask(range: String): List<(ConditionalFormatRule) -> ConditionalFormatRule> =
        listOf(
            {
                it.setBooleanRule(
                    BooleanRule()
                        .setCondition(
                            getCustomFormulaBooleanCondition(getCountIfRussianEnglishIsTFormula(range))
                        )
                        .setFormat(CellFormat().setBackgroundColor(getGreenAcceptedTask()))
                )
            },
            {
                it.setBooleanRule(
                    BooleanRule()
                        .setCondition(
                            getCustomFormulaBooleanCondition(getCountIfRussianEnglishIsPFormula(range))
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

    private fun executeRequestsSequence(vararg requests: Request) = service.spreadsheets()
        .batchUpdate(
            id,
            BatchUpdateSpreadsheetRequest().setRequests(requests.toList())
        ).execute()

    private fun addNewMainListColumn(title: String, students: List<String>) {
        val maxStudentRowIndex = students.size
        val maxStudentRowNumber = maxStudentRowIndex + 1

        val width: Int
        try {
            val result = getValueRange(
                getTitlePrettyOnlyRowRange(
                    MAIN_LIST_NAME,
                    TASKS_NAMES_MAIN_LIST_ROW_INDEX, TASKS_NAMES_MAIN_LIST_ROW_INDEX + 1
                )
            )
            width = result.getValues()[0].size
        } catch (e: GoogleJsonResponseException) {
            return
        } catch (e: IndexOutOfBoundsException) {
            return
        }

        val body = listOf(listOf(title)) +
                students.indices.map { it + MIN_STUDENT_ROW_INDEX }.map {
                    listOf(
                        getCountIfRussianEnglishIsTFormula(
                            getTitlePrettyLongRowRange(title, it, it + 1, TASK_FIRST_COLUMN_INDEX)
                        )
                    )
                }

        updateBody(
            getTitlePrettyRange(
                MAIN_LIST_NAME,
                TASKS_NAMES_MAIN_LIST_ROW_INDEX, maxStudentRowNumber, width, width + 1
            ),
            body,
        )

        // format new column
        executeRequestsSequence(
            *getRequests(
                MAIN_LIST_NAME,
                *getEqualsActionsRectangles(
                    listOf(
                        GridRequestMaker::colorizeBorders,
                        GridRequestMaker::formatCells,
                        { grid -> grid.setWidth(32) }
                    ),
                    Rectangle(TASKS_NAMES_MAIN_LIST_ROW_INDEX, maxStudentRowNumber, width, width + 1),
                    Rectangle(TASKS_NAMES_MAIN_LIST_ROW_INDEX, TASKS_NAMES_MAIN_LIST_ROW_INDEX + 1, width, width + 1)
                ).toTypedArray()
            ).toTypedArray(),
        )
    }


    /**
     * @param title is "Д[0-9]+" or <code>MAIN_LIST_NAME</code>
     */
    private fun fillInStudents(title: String, scoresFormula: (Int) -> String, students: List<String>) {
        val body =
            listOf(listOf(FCS_COLUMN_NAME, TOTAL_SCORES_COLUMN_NAME)) + students.mapIndexed { studentIndex, name ->
                listOf(name, scoresFormula(MIN_STUDENT_ROW_INDEX + studentIndex))
            }

        updateBody(
            getTitlePrettyRange(
                title,
                TASKS_NAMES_MAIN_LIST_ROW_INDEX, body.size, FCS_COLUMN_INDEX, TOTAL_SCORES_COLUMN_INDEX + 1
            ),
            body,
        )

        executeRequestsSequence(
            *getRequests(
                title,
                *getEqualsActionsRectangles(
                    listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, body.size,
                        FCS_COLUMN_INDEX, FCS_COLUMN_INDEX + 1
                    ),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, body.size,
                        TOTAL_SCORES_COLUMN_INDEX, TOTAL_SCORES_COLUMN_INDEX + 1
                    ),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_INDEX + 1,
                        FCS_COLUMN_INDEX, body.first().size
                    ),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, body.size,
                        FCS_COLUMN_INDEX, TOTAL_SCORES_COLUMN_INDEX + 1
                    )
                ).toTypedArray(),
                *getEqualsActionsRectangles(
                    listOf { grid -> grid.setWidth(200) },
                    Rectangle(
                        NONE_INDEX, NONE_INDEX,
                        FCS_COLUMN_INDEX, FCS_COLUMN_INDEX + 1
                    )
                ).toTypedArray(),
                *getEqualsActionsRectangles(
                    listOf { grid -> grid.setWidth(73) },
                    Rectangle(
                        NONE_INDEX, NONE_INDEX,
                        TOTAL_SCORES_COLUMN_INDEX, TOTAL_SCORES_COLUMN_INDEX + 1
                    )
                ).toTypedArray(),
            ).toTypedArray(),
        )
    }

    private fun getRequests(
        title: String,
        vararg updateCells: FormattedRectangle
    ): List<Request> =
        updateCells.map { rect ->
            rect.actions.map { action -> action(createGridRequestMaker(service, id, title, rect.rectangle)) }
        }.flatten()

    private fun getEqualsActionsRectangles(
        actions: List<(GridRequestMaker) -> Request>,
        vararg rectangles: Rectangle
    ): List<FormattedRectangle> = rectangles.map { rect -> FormattedRectangle(rect, actions) }

    companion object {
        enum class WHO_ENTERED {
            USER_ENTERED,
        }

        enum class CONDITION_TYPE {
            NUMBER_EQ,
            CUSTOM_FORMULA,
            TEXT_EQ
        }

        fun getValueRange(range: String, service: Sheets, id: String) = service
            .spreadsheets()
            .values()
            .get(id, range)
            .execute()

        private const val INTERPOLATION_POINT_TYPE_PERCENTILE = "PERCENTILE"

        private fun getInterpolationPoint(color: Color, percentile: String) = InterpolationPoint()
            .setType(INTERPOLATION_POINT_TYPE_PERCENTILE)
            .setColor(color)
            .setValue(percentile)

        private val MAIN_SCORES_GRADIENT = GradientRule()
            .setMinpoint(getInterpolationPoint(getRedScores(), "0"))
            .setMidpoint(getInterpolationPoint(getYellowScores(), "50"))
            .setMaxpoint(getInterpolationPoint(getGreenScores(), "100"))

        private val LOCAL_SCORED_GRADIENT = GradientRule()
            .setMinpoint(getInterpolationPoint(getWhiteColor(), "0"))
            .setMaxpoint(getInterpolationPoint(getGreenCountTasksColor(), "100"))

        private fun getSumScoresFormula(index: Int) =
            "=SUM(${
                getPrettyLongRowRange(
                    index,
                    index + 1,
                    FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_INDEX
                )
            })*$SCORES_FOR_DISCRETE_MATH_TASK"


        private fun getCountIf(range: String, condition: String) = "COUNTIF($range;\"$condition\")"

        private fun getCountAFormula(range: String) = "=COUNTA($range)"

        private fun getCountIfFormula(range: String, condition: String) = "=" + getCountIf(range, condition)

        private fun getCountIfRussianEnglishIsTFormula(range: String) = "=" +
                getCountIf(range, RUSSIAN_T) + " + " + getCountIf(range, ENGLISH_T)

        private fun getCountIfRussianEnglishIsPFormula(range: String) = "=" +
                getCountIf(range, RUSSIAN_P) + " + " + getCountIf(range, ENGLISH_P)

        private fun getActualScoreFormula(index: Int) =
            "=" + getTitlePrettyCell(MAIN_LIST_NAME, index, FCS_COLUMN_INDEX)

        private const val MAIN_LIST_NAME = "Results"
        private const val TEACHER_LIST_NAME = "Таблица с баллами"
        private const val FCS_COLUMN_NAME = "ФИО"
        private const val TOTAL_SCORES_COLUMN_NAME = "Total"
        private const val ONE_PRACTICE_TASKS_COLUMN_NAME = "S"
        private const val SCORES_FOR_DISCRETE_MATH_TASK: Int = 5

        private const val MAX_WEEK_DAY = 20
        private const val EXCESS_SHEETS = 2
        private const val NONE_INDEX = -1
        private const val MIN_STUDENT_ROW_INDEX = 1
        private const val TASKS_NAMES_ROW_INDEX = 0
        private const val TASKS_NAMES_MAIN_LIST_ROW_INDEX = 0
        private const val FCS_COLUMN_INDEX = 0
        private const val TOTAL_SCORES_COLUMN_INDEX = 1
        private const val TASK_COUNTER_COLUMN_INDEX = 2
        private const val TASK_FIRST_COLUMN_INDEX = 3
        private const val FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_INDEX = 2

        const val RUSSIAN_T = "Т"
        const val ENGLISH_T = "T"
        const val RUSSIAN_P = "Р"
        const val ENGLISH_P = "P"
    }
}