package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.database.DatabaseConfiguration
import by.cp.feedback.mechanism.bot.model.PollForUserReviewDto
import by.cp.feedback.mechanism.bot.table.PollUserVote
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PollUserVoteRepository {

    private val db = DatabaseConfiguration.database

    fun save(pollId: Long) = transaction {
        PollUserVote.insert(
            Users.slice(Users.id, longParam(pollId)).selectAll(),
            columns = listOf(PollUserVote.userId, PollUserVote.pollId)
        )
    }

    fun select15() = transaction {
        PollUserVote.selectAll()
            .limit(15)
            .map(::pollForUserReviewDto)
    }

    fun delete(pollId: Long) = transaction {
        PollUserVote.deleteWhere { PollUserVote.pollId eq pollId }
    }

    fun delete(reviewDto: PollForUserReviewDto) = transaction {
        PollUserVote.deleteWhere { (pollId eq reviewDto.pollId) and (userId eq reviewDto.userId) }
    }

    private fun pollForUserReviewDto(it: ResultRow) = PollForUserReviewDto(
        it[PollUserVote.userId],
        it[PollUserVote.pollId],
    )

}