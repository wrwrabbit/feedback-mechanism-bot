package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.database.DatabaseConfiguration
import by.cp.feedback.mechanism.bot.model.PollForUserReviewDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.table.PollUserReview
import by.cp.feedback.mechanism.bot.table.Polls
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PollUserReviewRepository {

    private val db = DatabaseConfiguration.database

    fun save(pollId: Long) = transaction {
        PollUserReview.insert(
            Users.slice(Users.id, longParam(pollId)).selectAll(),
            columns = listOf(PollUserReview.userId, PollUserReview.pollId)
        )
    }

    fun saveByUserId(userId: Long) = transaction {
        PollUserReview.insert(
            Polls.slice(longParam(userId), Polls.id).select { Polls.status eq PollStatus.ON_USER_REVIEW },
            columns = listOf(PollUserReview.userId, PollUserReview.pollId)
        )
    }

    fun select15() = transaction {
        PollUserReview.selectAll()
            .limit(15)
            .map(::pollForUserReviewDto)
    }

    fun delete(pollId: Long) = transaction {
        PollUserReview.deleteWhere { PollUserReview.pollId eq pollId }
    }

    fun delete(reviewDto: PollForUserReviewDto) = transaction {
        PollUserReview.deleteWhere { (pollId eq reviewDto.pollId) and (userId eq reviewDto.userId) }
    }

    private fun pollForUserReviewDto(it: ResultRow) = PollForUserReviewDto(
        it[PollUserReview.userId],
        it[PollUserReview.pollId],
    )

}