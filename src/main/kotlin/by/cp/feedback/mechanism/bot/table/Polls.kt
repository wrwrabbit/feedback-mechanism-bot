package by.cp.feedback.mechanism.bot.table

import by.cp.feedback.mechanism.bot.util.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Polls : LongIdTable(name = "polls") {
    val userId: Column<Long> = long("user_id")
    val question: Column<String> = text("question")
    val options: Column<Array<String>> = array("options", columnType = TextColumnType())
    val allowMultipleAnswers: Column<Boolean> = bool("allow_multiple_answers")
    val createdAt: Column<LocalDateTime> = datetime("created_at")
}
