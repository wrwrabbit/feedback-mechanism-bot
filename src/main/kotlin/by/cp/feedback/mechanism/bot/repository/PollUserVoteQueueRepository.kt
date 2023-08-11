package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.PollForUserReviewDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.table.PollUserVoteQueue
import by.cp.feedback.mechanism.bot.table.Polls
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PollUserVoteQueueRepository {

    fun save(pollId: Long) = transaction {
        PollUserVoteQueue.insert(
            Users.slice(Users.id, longParam(pollId)).selectAll(),
            columns = listOf(PollUserVoteQueue.userId, PollUserVoteQueue.pollId)
        )
    }

    fun save(pollId: Long, userId: Long) = transaction {
        PollUserVoteQueue.insert {
            it[PollUserVoteQueue.pollId] = pollId
            it[PollUserVoteQueue.userId] = userId
        }
    }

    fun saveByUserId(userId: Long) = transaction {
        PollUserVoteQueue.insert(
            Polls.slice(longParam(userId), Polls.id).select { Polls.status eq PollStatus.VOTING },
            columns = listOf(PollUserVoteQueue.userId, PollUserVoteQueue.pollId)
        )
    }

    fun select15() = transaction {
        PollUserVoteQueue.selectAll()
            .limit(15)
            .map(::pollForUserReviewDto)
    }

    fun delete(pollId: Long) = transaction {
        PollUserVoteQueue.deleteWhere { PollUserVoteQueue.pollId eq pollId }
    }

    fun delete(reviewDto: PollForUserReviewDto) = transaction {
        PollUserVoteQueue.deleteWhere { (pollId eq reviewDto.pollId) and (userId eq reviewDto.userId) }
    }

    private fun pollForUserReviewDto(it: ResultRow) = PollForUserReviewDto(
        it[PollUserVoteQueue.userId],
        it[PollUserVoteQueue.pollId],
    )

}