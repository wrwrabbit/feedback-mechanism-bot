package by.cp.feedback.mechanism.bot.table

import by.cp.feedback.mechanism.bot.util.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.LongColumnType

object PollsModeration : LongIdTable(name = "polls", columnName = "poll_id") {
    val telegramId: Column<Long> = long("telegram_id")
    val approves: Column<Array<Long>> = array("approves", columnType = LongColumnType())
    val rejectionReason: Column<String?> = text("rejection_reason").nullable()
}
