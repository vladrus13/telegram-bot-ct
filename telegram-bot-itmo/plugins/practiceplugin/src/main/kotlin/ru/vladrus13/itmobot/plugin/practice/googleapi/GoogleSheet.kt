package ru.vladrus13.itmobot.plugin.practice.googleapi

import com.google.api.client.googleapis.json.GoogleJsonResponseException
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.Sheets.Spreadsheets.Values.Update
import com.google.api.services.sheets.v4.model.AddConditionalFormatRuleRequest
import com.google.api.services.sheets.v4.model.AddSheetRequest
import com.google.api.services.sheets.v4.model.BatchUpdateSpreadsheetRequest
import com.google.api.services.sheets.v4.model.BooleanCondition
import com.google.api.services.sheets.v4.model.BooleanRule
import com.google.api.services.sheets.v4.model.CellFormat
import com.google.api.services.sheets.v4.model.Color
import com.google.api.services.sheets.v4.model.ConditionValue
import com.google.api.services.sheets.v4.model.ConditionalFormatRule
import com.google.api.services.sheets.v4.model.GradientRule
import com.google.api.services.sheets.v4.model.InterpolationPoint
import com.google.api.services.sheets.v4.model.Request
import com.google.api.services.sheets.v4.model.SheetProperties
import com.google.api.services.sheets.v4.model.UpdateSheetPropertiesRequest
import com.google.api.services.sheets.v4.model.ValueRange
import org.slf4j.LoggerFactory
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

        val (requests, fillValuesUpdate) = fillInStudents(
            MAIN_LIST_NAME, Companion::getSumScoresFormula, Companion::getSumCells, students,
            isMainSheet = true
        )

        // Conditional Format
        executeRequestsSequence(
            *requests.toTypedArray(),
            getConditionalFormatRequest(
                MAIN_LIST_NAME,
                MIN_STUDENT_ROW_INDEX, MIN_STUDENT_ROW_INDEX + students.size,
                TOTAL_TASKS_COLUMN_INDEX, TOTAL_TASKS_COLUMN_INDEX + 1
            ) { it.setGradientRule(MAIN_SCORES_GRADIENT) }
        )

        fillValuesUpdate.execute()

        generateTeacherSheet()
    }

    fun updateFields(teacherTable: List<List<String>>) {
        updateBody(
            getTitlePrettyRange(TEACHER_LIST_NAME, 0, teacherTable.size, 0, teacherTable.first().size),
            teacherTable,
        ).execute()
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
        val lastTaskColumnIndex = S_TASKS_COLUMN_INDEX + tasks.size
        executeRequestsSequence(Request().setAddSheet(AddSheetRequest().setProperties(properties)))

        val (requestsToNewList, fillValuesUpdate) = fillInStudents(
            title,
            Companion::getActualScoreFormula,
            Companion::getSumCells,
            students,
            1,
            false
        )

        // Conditional Format
        executeRequestsSequence(
            *requestsToNewList.toTypedArray(),
            *colorCellsByTaskGreenGradient(
                title, MIN_STUDENT_ROW_INDEX, maxStudentRowIndex + 1,
                S_TASKS_COLUMN_INDEX, S_TASKS_COLUMN_INDEX + 1
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
                        S_TASKS_COLUMN_INDEX, S_TASKS_COLUMN_INDEX + 1
                    ),
                    Rectangle(
                        TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_INDEX + 1,
                        TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1
                    ),
                    Rectangle(
                        lastRowIndex, lastRowIndex + 1,
                        S_TASKS_COLUMN_INDEX, lastTaskColumnIndex + 1
                    )
                ).toTypedArray(),

                *getEqualsActionsRectangles(
                    listOf { grid -> grid.setWidth(S_TASKS_WIDTH) },
                    Rectangle(
                        NONE_INDEX, NONE_INDEX,
                        S_TASKS_COLUMN_INDEX, S_TASKS_COLUMN_INDEX + 1
                    )
                ).toTypedArray(),
                *getEqualsActionsRectangles(
                    listOf { grid -> grid.setWidth(TASK_WIDTH) },
                    Rectangle(
                        NONE_INDEX, NONE_INDEX,
                        TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1
                    )
                ).toTypedArray(),
            ).toTypedArray(),
        )

        fillValuesUpdate.execute()

        val body = buildList {
            add(tasks)
            addAll(List((maxStudentRowIndex + 1) - MIN_STUDENT_ROW_INDEX) { listOf("") })
            add(tasks.indices.map { it + TASK_FIRST_COLUMN_INDEX }.map {
                getCountAFormula(
                    getPrettyRange(MIN_STUDENT_ROW_INDEX, maxStudentRowIndex + 1, it, it + 1)
                )
            })
        }

        updateBody(
            getTitlePrettyRange(
                title,
                TASKS_NAMES_ROW_INDEX, lastRowIndex + 1,
                TASK_FIRST_COLUMN_INDEX, lastTaskColumnIndex + 1
            ),
            body,
        ).execute()

        addNewMainListColumn(title, students)
    }

    private fun generateTeacherSheet(): Boolean {
        val sheets = service.spreadsheets().get(id).execute().sheets
        if (sheets.any { it.properties.title == TEACHER_LIST_NAME }) {
            return false
        }

        val properties = SheetProperties().setTitle(TEACHER_LIST_NAME).setIndex(sheets.size)
        executeRequestsSequence(Request().setAddSheet(AddSheetRequest().setProperties(properties)))
        return true
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
                                S_TASKS_COLUMN_INDEX,
                                TOTAL_TASKS_COLUMN_INDEX
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

    private fun updateBody(range: String, body: List<List<String>>): Update = service
        .spreadsheets()
        .values()
        .update(id, range, ValueRange().setValues(body))
        .setValueInputOption(WHO_ENTERED.USER_ENTERED.toString())

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

    // Update S_TASKS column and add new day practice
    private fun addNewMainListColumn(title: String, students: List<String>) {
        val maxStudentRowIndex = students.size
        val maxStudentRowNumber = maxStudentRowIndex + 1
        val width: Int = getTasksWidthMainSheet() ?: throw IllegalArgumentException("Something wrong, width=null")

        updateSTasksColumn(students, width, maxStudentRowNumber)
        addNewHWColumn(title, students, maxStudentRowNumber, width)
    }

    private fun updateSTasksColumn(
        students: List<String>,
        width: Int,
        maxStudentRowNumber: Int
    ) {
        val body = buildList {
            addAll(students.indices.map { it + MIN_STUDENT_ROW_INDEX }.map {
                listOf(getSumCells(it, (width + 1) - FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_INDEX))
            })
        }

        updateBody(
            getTitlePrettyRange(
                MAIN_LIST_NAME,
                MIN_STUDENT_ROW_INDEX,
                maxStudentRowNumber,
                S_TASKS_COLUMN_INDEX,
                S_TASKS_COLUMN_INDEX + 1
            ),
            body,
        ).execute()
    }

    private fun addNewHWColumn(
        title: String,
        students: List<String>,
        maxStudentRowNumber: Int,
        width: Int,
    ) {
        val body = buildList {
            add(listOf(title))
            addAll(students.indices.map { it + MIN_STUDENT_ROW_INDEX }.map {
                listOf(
                    getCountIfRussianEnglishIsTFormula(
                        getTitlePrettyLongRowRange(title, it, it + 1, TASK_FIRST_COLUMN_INDEX)
                    )
                )
            })
        }

        // format new column
        executeRequestsSequence(
            *getRequests(
                MAIN_LIST_NAME,
                *getEqualsActionsRectangles(
                    listOf(
                        GridRequestMaker::colorizeBorders,
                        GridRequestMaker::formatCells,
                        { grid -> grid.setWidth(TASK_WIDTH) }
                    ),
                    Rectangle(TASKS_NAMES_ROW_INDEX, maxStudentRowNumber, width, width + 1),
                    Rectangle(TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_INDEX + 1, width, width + 1)
                ).toTypedArray()
            ).toTypedArray(),
        )

        updateBody(
            getTitlePrettyRange(MAIN_LIST_NAME, TASKS_NAMES_ROW_INDEX, maxStudentRowNumber, width, width + 1),
            body,
        ).execute()
    }

    private fun getTasksWidthMainSheet() = try {
        val result = getValueRange(
            getTitlePrettyOnlyRowRange(
                MAIN_LIST_NAME,
                TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_INDEX + 1
            )
        )
        result.getValues()[0].size
    } catch (e: GoogleJsonResponseException) {
        LOG.error("$e")
        null
    } catch (e: IndexOutOfBoundsException) {
        LOG.error("$e")
        null
    }

    /**
     * @param title is "Д[0-9]+" or <code>MAIN_LIST_NAME</code>
     */
    private fun fillInStudents(
        title: String,
        totalFormula: (Int) -> String,
        sFormula: (Int, Int) -> String,
        students: List<String>,
        workingSheets: Int = 0,
        isMainSheet: Boolean = false,
    ): Pair<List<Request>, Sheets.Spreadsheets.Values.Update> {
        val body = buildList {
            add(listOf(FCS_COLUMN_NAME, TOTAL_TASKS_COLUMN_NAME, S_TASKS_COLUMN_NAME))
            addAll(students.mapIndexed { index, name ->
                val studentIndex = index + MIN_STUDENT_ROW_INDEX

                listOf(
                    name,
                    totalFormula(MIN_STUDENT_ROW_INDEX + index),
                    if (isMainSheet) sFormula(studentIndex, workingSheets)
                    else getCountAFormula(
                        getPrettyLongRowRange(studentIndex, studentIndex + 1, TASK_FIRST_COLUMN_INDEX)
                    ),
                )
            })
        }

        return Pair(
            getRequests(
                title,
                *makeBorders(body),
                *setFCSWidth(),
                *setTOTALWidth(),
                *setSWidth(),
            ), updateBody(
                getTitlePrettyRange(
                    title,
                    TASKS_NAMES_ROW_INDEX, body.size, FCS_COLUMN_INDEX, S_TASKS_COLUMN_INDEX + 1
                ),
                body,
            )
        )
    }

    // Ставит определённую ширину столбца FCS
    private fun setFCSWidth() = getEqualsActionsRectangles(
        listOf { grid -> grid.setWidth(FCS_TASKS_WIDTH) },
        Rectangle(
            NONE_INDEX, NONE_INDEX,
            FCS_COLUMN_INDEX, FCS_COLUMN_INDEX + 1
        )
    ).toTypedArray()

    // Ставит определённую ширину столбца Total
    private fun setTOTALWidth() = getEqualsActionsRectangles(
        listOf { grid -> grid.setWidth(TOTAL_TASKS_WIDTH) },
        Rectangle(
            NONE_INDEX, NONE_INDEX,
            TOTAL_TASKS_COLUMN_INDEX, TOTAL_TASKS_COLUMN_INDEX + 1
        )
    ).toTypedArray()

    // Ставит определённую ширину столбца S
    private fun setSWidth() = getEqualsActionsRectangles(
        listOf { grid -> grid.setWidth(S_TASKS_WIDTH) },
        Rectangle(
            NONE_INDEX, NONE_INDEX,
            S_TASKS_COLUMN_INDEX, S_TASKS_COLUMN_INDEX + 1
        )
    ).toTypedArray()

    // Ограничивает линиями таблицу
    private fun makeBorders(body: List<List<String>>) = getEqualsActionsRectangles(
        listOf(GridRequestMaker::colorizeBorders, GridRequestMaker::formatCells),
        Rectangle(
            TASKS_NAMES_ROW_INDEX, body.size,
            FCS_COLUMN_INDEX, FCS_COLUMN_INDEX + 1
        ),
        Rectangle(
            TASKS_NAMES_ROW_INDEX, body.size,
            TOTAL_TASKS_COLUMN_INDEX, TOTAL_TASKS_COLUMN_INDEX + 1
        ),
        Rectangle(
            TASKS_NAMES_ROW_INDEX, body.size,
            S_TASKS_COLUMN_INDEX, S_TASKS_COLUMN_INDEX + 1
        ),
        Rectangle(
            TASKS_NAMES_ROW_INDEX, TASKS_NAMES_ROW_INDEX + 1,
            FCS_COLUMN_INDEX, body.first().size
        ),
        Rectangle(
            TASKS_NAMES_ROW_INDEX, body.size,
            FCS_COLUMN_INDEX, S_TASKS_COLUMN_INDEX + 1
        )
    ).toTypedArray()

    private fun getRequests(
        title: String,
        vararg updateCells: FormattedRectangle
    ): List<Request> = updateCells.map { rect ->
        rect.actions.map { action -> action(createGridRequestMaker(service, id, title, rect.rectangle)) }
    }.flatten()

    private fun getEqualsActionsRectangles(
        actions: List<(GridRequestMaker) -> Request>,
        vararg rectangles: Rectangle
    ): List<FormattedRectangle> = rectangles.map { rect -> FormattedRectangle(rect, actions) }

    companion object {
        private val LOG = LoggerFactory.getLogger(GoogleSheet::class.java)

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

        // Нужен для выделения логики заполнения кол-ва всех решённых задач человеком
        private fun getSumCells(rowIndex: Int, listCount: Int) = "=SUM(${
            List(listCount) { i ->
                GridRequestMaker.getCellWithTitleAndNotChangingColumn("Д${i + 1}", rowIndex, S_TASKS_COLUMN_INDEX)
            }.joinToString(", ")
        })"

        private fun getCountIf(range: String, condition: String) = "COUNTIF($range;\"$condition\")"

        private fun getCountAFormula(range: String) = "=COUNTA($range)"

        private fun getCountIfFormula(range: String, condition: String) = "=" + getCountIf(range, condition)

        private fun getCountIfRussianEnglishIsTFormula(range: String) = "=" +
                getCountIf(range, RUSSIAN_T) + " + " + getCountIf(range, ENGLISH_T)

        private fun getCountIfRussianEnglishIsPFormula(range: String) = "=" +
                getCountIf(range, RUSSIAN_P) + " + " + getCountIf(range, ENGLISH_P)

        private fun getActualScoreFormula(index: Int) =
            "=" + getTitlePrettyCell(MAIN_LIST_NAME, index, FCS_COLUMN_INDEX)

        // This name of main list
        private const val MAIN_LIST_NAME = "Results"
        private const val TEACHER_LIST_NAME = "Таблица с баллами"
        private const val FCS_COLUMN_NAME = "ФИО"
        private const val TOTAL_TASKS_COLUMN_NAME = "Total"
        private const val S_TASKS_COLUMN_NAME = "S"
        private const val SCORES_FOR_DISCRETE_MATH_TASK: Int = 5

        private const val MAX_WEEK_DAY = 20

        // Кол-во доп. листов, в данном случае есть 'Results' & 'Таблица для учителей'
        private const val EXCESS_SHEETS = 2

        // Используется для применения свойств ко всем строкам
        private const val NONE_INDEX = -1

        // Индекс строки, с которой начинаются студенты
        private const val MIN_STUDENT_ROW_INDEX = 1

        // Индекс строки, где указаны номера заданий
        private const val TASKS_NAMES_ROW_INDEX = 0

        // Индекс столбца, где указаны ФИО
        private const val FCS_COLUMN_INDEX = 0

        // Индекс столбца, где указано итоговое кол-во баллов
        private const val TOTAL_TASKS_COLUMN_INDEX = 1

        // Индекс столбца, где указаны общее кол-во решённых заданий
        private const val S_TASKS_COLUMN_INDEX = 2

        // Индекс столбца, с которого начинаются перечисления заданий
        private const val TASK_FIRST_COLUMN_INDEX = 3

        // Индекс столбца, с которого начинаются кол-во отмеченных заданий
        private const val FIRST_TASKS_COUNTER_MAIN_LIST_COLUMN_INDEX = 3

        // Размер в пискелях FCS столбца
        private const val FCS_TASKS_WIDTH = 200

        // Размер в пискелях TOTAL столбца
        private const val TOTAL_TASKS_WIDTH = 73

        // Размер в пискелях S столбца
        private const val S_TASKS_WIDTH = 37

        // Размер в пикселях столбцов решённых задач у студентов
        private const val TASK_WIDTH = 32

        const val RUSSIAN_T = "Т"
        const val ENGLISH_T = "T"
        const val RUSSIAN_P = "Р"
        const val ENGLISH_P = "P"
        const val CORRECT_1 = "#"
        const val CORRECT_2 = "##"

        val correct_letters = listOf(RUSSIAN_T, ENGLISH_T, CORRECT_1, CORRECT_2)
    }
}