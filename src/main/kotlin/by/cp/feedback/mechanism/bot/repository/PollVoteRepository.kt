package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.PollVoteDto
import by.cp.feedback.mechanism.bot.table.PollVote
import by.cp.feedback.mechanism.bot.table.Polls
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PollVoteRepository {

    fun save(pollId: Long) = transaction {
        PollVote.insertAndGetId {
            it[PollVote.id] = pollId
        }.value
    }

    fun findInVoting() = transaction {
        Polls.join(PollVote, JoinType.INNER, Polls.id, PollVote.id)
            .select { Polls.status eq PollStatus.VOTING }
            .map {
                PollVoteDto(
                    it[Polls.id].value,
                    it[Polls.messageId],
                    it[Polls.question],
                    it[Polls.allowMultipleAnswers],
                    it[Polls.createdAt],
                    it[Polls.startedAt],
                    it[Polls.finishedAt],
                    it[Polls.options],
                    it[Polls.options].mapIndexed { index, s ->
                        when (index + 1) {
                            1 -> it[PollVote.option_1]
                            2 -> it[PollVote.option_2]
                            3 -> it[PollVote.option_3]
                            4 -> it[PollVote.option_4]
                            5 -> it[PollVote.option_5]
                            6 -> it[PollVote.option_6]
                            7 -> it[PollVote.option_7]
                            8 -> it[PollVote.option_8]
                            9 -> it[PollVote.option_9]
                            10 -> it[PollVote.option_10]
                            else -> throw RuntimeException()
                        }
                    }
                )
            }
    }

    fun delete(pollId: Long) = transaction {
        PollVote.deleteWhere { PollVote.id eq pollId }
    }

    fun vote(pollId: Long, option: Int) = transaction {
        PollVote.update({ PollVote.id eq pollId }) {
            with(SqlExpressionBuilder) {
                when (option) {
                    1 -> it.update(option_1, option_1 + 1)
                    2 -> it.update(option_2, option_2 + 1)
                    3 -> it.update(option_3, option_3 + 1)
                    4 -> it.update(option_4, option_4 + 1)
                    5 -> it.update(option_5, option_5 + 1)
                    6 -> it.update(option_6, option_6 + 1)
                    7 -> it.update(option_7, option_7 + 1)
                    8 -> it.update(option_8, option_8 + 1)
                    9 -> it.update(option_9, option_9 + 1)
                    10 -> it.update(option_10, option_10 + 1)
                }
            }
        }
    }

    fun vote(pollId: Long, options: List<Int>) = transaction {
        PollVote.update({ PollVote.id eq pollId }) {
            options.map { option ->
                with(SqlExpressionBuilder) {
                    when (option) {
                        1 -> it.update(option_1, option_1 + 1)
                        2 -> it.update(option_2, option_2 + 1)
                        3 -> it.update(option_3, option_3 + 1)
                        4 -> it.update(option_4, option_4 + 1)
                        5 -> it.update(option_5, option_5 + 1)
                        6 -> it.update(option_6, option_6 + 1)
                        7 -> it.update(option_7, option_7 + 1)
                        8 -> it.update(option_8, option_8 + 1)
                        9 -> it.update(option_9, option_9 + 1)
                        10 -> it.update(option_10, option_10 + 1)
                    }
                }
            }
        }
    }

}