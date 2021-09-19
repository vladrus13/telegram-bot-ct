package ru.vladrus13.itmobot.database.entities

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.bean.UsersSubjects
import ru.vladrus13.itmobot.database.DataBaseEntity

class SubjectsParser : DataBaseEntity<UsersSubjects>() {

    object Subjects : Table() {
        val id = long("chat_id").uniqueIndex()
        val include = varchar("include", 1023)
        val exclude = varchar("exclude", 1023)
    }

    override val table: Table
        get() = Subjects
    override val columnId: Column<Long>
        get() = Subjects.id

    override fun getNew(chatId: Long): UsersSubjects = UsersSubjects(chatId)

    override fun set(o: UsersSubjects, it: UpdateBuilder<Number>) {
        it[Subjects.include] = o.getInclude()
        it[Subjects.exclude] = o.getExclude()
    }

    override fun get(result: ResultRow): UsersSubjects = UsersSubjects(
        result[Subjects.id],
        result[Subjects.include],
        result[Subjects.exclude],
    )

    override val name: String = "Subjects"
}