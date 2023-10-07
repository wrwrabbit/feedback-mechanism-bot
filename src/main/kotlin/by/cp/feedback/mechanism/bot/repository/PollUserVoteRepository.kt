package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.PollVoteDto
import by.cp.feedback.mechanism.bot.table.PollUserVote
import by.cp.feedback.mechanism.bot.table.Polls
import mu.KotlinLogging
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction
import java.math.BigDecimal

object PollUserVoteRepository {

    private val logger = KotlinLogging.logger {}

    fun findResults(status: PollStatus) = transaction {
        Polls.join(PollUserVote, JoinType.INNER, Polls.id, PollUserVote.pollId)
            .select { Polls.status eq status }
            .groupBy {
                it[Polls.id].value
            }
            .map {
                val pollResultRow = it.value.first()
                val results = pollResultRow[Polls.options].map { 0f }.toMutableList()
                it.value.map { pollVoteResultRow ->
                    pollVoteResultRow[PollUserVote.options].toList()
                }.forEach { userVotes ->
                    val voteCost = 1f / userVotes.count()
                    userVotes.forEach { userVote ->
                        results[userVote.toInt() - 1] = results[userVote.toInt() - 1] + voteCost
                    }
                }
                PollVoteDto(
                    id = it.key,
                    userId = pollResultRow[Polls.userId],
                    messageId = pollResultRow[Polls.messageId],
                    question = pollResultRow[Polls.question],
                    allowMultipleAnswers = pollResultRow[Polls.allowMultipleAnswers],
                    options = pollResultRow[Polls.options],
                    results = results,
                    startedAt = pollResultRow[Polls.startedAt]
                )
            }
    }

    fun delete(pollId: Long) = transaction {
        PollUserVote.deleteWhere { PollUserVote.pollId eq pollId }
    }

    fun vote(pollId: Long, userId: Long, option: Int) = transaction {
        try {
            PollUserVote.insert {
                it[PollUserVote.pollId] = pollId
                it[PollUserVote.userId] = userId
                it[PollUserVote.options] = arrayOf(option.toLong())
            }
        } catch (ex: ExposedSQLException) {
            if (ex.message?.contains("duplicate key") != true) {
                logger.error(ex) { "Error while insert" }
            } else {

            }
        }
    }

    fun vote(pollId: Long, userId: Long, options: List<Int>) = transaction {
        try {
            PollUserVote.insert {
                it[PollUserVote.pollId] = pollId
                it[PollUserVote.userId] = userId
                it[PollUserVote.options] = options.map(Int::toLong).toTypedArray()
            }
        } catch (ex: ExposedSQLException) {
            if (ex.message?.contains("duplicate key") != true) {
                logger.error(ex) { "Error while insert" }
            } else {

            }
        }
    }

}