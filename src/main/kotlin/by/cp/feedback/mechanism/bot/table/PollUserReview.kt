package by.cp.feedback.mechanism.bot.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object PollUserReview : LongIdTable(name = "poll_user_review", columnName = "poll_id") {
    val userId: Column<Long> = long("user_id")
    val approved: Column<Boolean> = bool("approved")
}
