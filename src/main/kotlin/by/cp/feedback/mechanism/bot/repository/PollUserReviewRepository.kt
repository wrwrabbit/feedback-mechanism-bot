package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.table.PollUserReview
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PollUserReviewRepository {

    fun save(pollId: Long, userId: Long, approved: Boolean) = transaction {
        PollUserReview.insertAndGetId {
            it[PollUserReview.id] = pollId
            it[PollUserReview.userId] = userId
            it[PollUserReview.approved] = approved
        }.value
    }

    fun approvalsCount(pollId: Long) = transaction {
        PollUserReview.select { (PollUserReview.id eq pollId) and (PollUserReview.approved eq true) }.count()
    }

}