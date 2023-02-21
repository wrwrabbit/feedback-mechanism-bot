package by.cp.feedback.mechanism.bot.table

import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.Table

object PollUserVote : Table(name = "poll_user_vote") {
    val userId: Column<Long> = long("user_id")
    val pollId: Column<Long> = long("poll_id")
}
