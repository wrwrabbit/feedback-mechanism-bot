package by.cp.feedback.mechanism.bot.table

import by.cp.feedback.mechanism.bot.util.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.LongColumnType
import org.jetbrains.exposed.sql.Table

object PollUserVote : Table(name = "poll_user_vote") {
    val pollId: Column<Long> = long("poll_id")
    val userId: Column<Long> = long("user_id")
    val options: Column<Array<Long>> = array("options", columnType = LongColumnType())
}
