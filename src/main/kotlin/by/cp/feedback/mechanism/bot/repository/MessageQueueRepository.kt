package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.MessageQueueType
import by.cp.feedback.mechanism.bot.model.MessageQueueDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.UserStatus
import by.cp.feedback.mechanism.bot.table.MessageQueue
import by.cp.feedback.mechanism.bot.table.Polls
import by.cp.feedback.mechanism.bot.table.Users
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object MessageQueueRepository {

    fun save(pollId: Long, type: MessageQueueType) = transaction {
        MessageQueue.insert(
            Users.slice(Users.id, longParam(pollId), stringParam(type.toString())).selectAll(),
            columns = listOf(MessageQueue.userId, MessageQueue.pollId, MessageQueue.type)
        )
    }

    fun save(userId: Long, pollId: Long, type: MessageQueueType) = transaction {
        MessageQueue.insert {
            it[MessageQueue.userId] = userId
            it[MessageQueue.pollId] = pollId
            it[MessageQueue.type] = type
        }
    }

    fun saveReviewByUserId(userId: Long, type: MessageQueueType) = transaction {
        MessageQueue.insert(
            Polls.slice(longParam(userId), Polls.id, stringParam(type.toString()))
                .select { Polls.status eq PollStatus.ON_USER_REVIEW },
            columns = listOf(MessageQueue.userId, MessageQueue.pollId, MessageQueue.type)
        )
    }

    fun saveVoteByUserId(userId: Long, type: MessageQueueType) = transaction {
        MessageQueue.insert(
            Polls.slice(longParam(userId), Polls.id, stringParam(type.toString()))
                .select { Polls.status eq PollStatus.ON_USER_REVIEW },
            columns = listOf(MessageQueue.userId, MessageQueue.pollId, MessageQueue.type)
        )
    }

    fun select15() = transaction {
        MessageQueue.join(Users, JoinType.INNER, MessageQueue.userId, Users.id)
            .select { Users.status eq UserStatus.UNMUTED }
            .limit(15)
            .map(::pollForUserReviewDto)
    }

    fun delete(reviewDto: MessageQueueDto) = transaction {
        MessageQueue.deleteWhere { (pollId eq reviewDto.pollId) and (userId eq reviewDto.userId) }
    }

    private fun pollForUserReviewDto(it: ResultRow) = MessageQueueDto(
        it[MessageQueue.userId],
        it[MessageQueue.pollId],
        it[MessageQueue.type],
    )

}