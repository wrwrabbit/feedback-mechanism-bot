package by.cp.feedback.mechanism.bot.table

import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.util.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.TextColumnType
import org.jetbrains.exposed.sql.javatime.datetime
import java.time.LocalDateTime

object Polls : LongIdTable(name = "polls") {
    val userId: Column<Long> = long("user_id")
    val status: Column<PollStatus> = enumerationByName("status", 255, PollStatus::class)
    val question: Column<String> = text("question")
    val options: Column<Array<String>> = array("options", columnType = TextColumnType())
    val allowMultipleAnswers: Column<Boolean> = bool("allow_multiple_answers")
    val createdAt: Column<LocalDateTime> = datetime("created_at")
    val approves: Column<Array<Long>> = array("approves", columnType = LongColumnType())
    val rejectionReason: Column<String?> = text("rejection_reason").nullable()
}
