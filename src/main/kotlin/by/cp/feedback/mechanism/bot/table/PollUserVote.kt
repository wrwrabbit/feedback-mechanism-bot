package by.cp.feedback.mechanism.bot.table

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Column

object PollUserVote : LongIdTable(name = "poll_user_vote", columnName = "poll_id") {
    val userId: Column<Long> = long("user_id")
    val option_1: Column<Long> = long("option_1")
    val option_2: Column<Long> = long("option_2")
    val option_3: Column<Long> = long("option_3")
    val option_4: Column<Long> = long("option_4")
    val option_5: Column<Long> = long("option_5")
    val option_6: Column<Long> = long("option_6")
    val option_7: Column<Long> = long("option_7")
    val option_8: Column<Long> = long("option_8")
    val option_9: Column<Long> = long("option_9")
    val option_10: Column<Long> = long("option_10")
}
