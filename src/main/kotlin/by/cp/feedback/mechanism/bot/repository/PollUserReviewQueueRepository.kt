package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.PollForUserReviewDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.table.PollUserReviewQueue
import by.cp.feedback.mechanism.bot.table.Polls
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PollUserReviewQueueRepository {

    fun save(pollId: Long) = transaction {
        PollUserReviewQueue.insert(
            Users.slice(Users.id, longParam(pollId)).selectAll(),
            columns = listOf(PollUserReviewQueue.userId, PollUserReviewQueue.pollId)
        )
    }

    fun saveByUserId(userId: Long) = transaction {
        PollUserReviewQueue.insert(
            Polls.slice(longParam(userId), Polls.id).select { Polls.status eq PollStatus.ON_USER_REVIEW },
            columns = listOf(PollUserReviewQueue.userId, PollUserReviewQueue.pollId)
        )
    }

    fun select15() = transaction {
        PollUserReviewQueue.selectAll()
            .limit(15)
            .map(::pollForUserReviewDto)
    }

    fun delete(pollId: Long) = transaction {
        PollUserReviewQueue.deleteWhere { PollUserReviewQueue.pollId eq pollId }
    }

    fun delete(reviewDto: PollForUserReviewDto) = transaction {
        PollUserReviewQueue.deleteWhere { (pollId eq reviewDto.pollId) and (userId eq reviewDto.userId) }
    }

    private fun pollForUserReviewDto(it: ResultRow) = PollForUserReviewDto(
        it[PollUserReviewQueue.userId],
        it[PollUserReviewQueue.pollId],
    )

}