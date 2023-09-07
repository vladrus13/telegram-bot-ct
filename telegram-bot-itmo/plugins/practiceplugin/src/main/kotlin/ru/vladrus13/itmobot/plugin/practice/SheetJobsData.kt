package ru.vladrus13.itmobot.plugin.practice

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.UpdateBuilder
import ru.vladrus13.itmobot.database.DataBaseEntity

class SheetJob(
    val id: Long,
    val jobId: Long,
    val link: String,
    val userId: Long,
)

object SheetJobTable : Table() {
    val id = long("id").uniqueIndex()
    val jobId = long("job_id")
    val link = text("link")
    val userId = long("user_id")
}

class SheetJobParser(
    override val name: String = "SheetJobs",
    override val table: Table = SheetJobTable,
    override val columnId: Column<Long> = SheetJobTable.id
) : DataBaseEntity<SheetJob>() {
    override fun getNew(chatId: Long): SheetJob = SheetJob(chatId, 0L, "", 0L)

    override fun get(result: ResultRow): SheetJob = SheetJob(
        result[SheetJobTable.id],
        result[SheetJobTable.jobId],
        result[SheetJobTable.link],
        result[SheetJobTable.userId],
    )

    override fun set(o: SheetJob, it: UpdateBuilder<Number>) {
        it[SheetJobTable.jobId] = o.jobId
        it[SheetJobTable.link] = o.link
        it[SheetJobTable.userId] = o.userId
    }
}