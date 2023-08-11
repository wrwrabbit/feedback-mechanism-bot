package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.PollVoteDto
import by.cp.feedback.mechanism.bot.table.PollUserVote
import by.cp.feedback.mechanism.bot.table.Polls
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

object PollUserVoteRepository {

    fun findResultsInVoting() = transaction {
        val pollIdAl = PollUserVote.id.alias("poll_id")
        val option1Sum = PollUserVote.option_1.sum().alias("option_1_sum")
        val option2Sum = PollUserVote.option_2.sum().alias("option_2_sum")
        val option3Sum = PollUserVote.option_3.sum().alias("option_3_sum")
        val option4Sum = PollUserVote.option_4.sum().alias("option_4_sum")
        val option5Sum = PollUserVote.option_5.sum().alias("option_5_sum")
        val option6Sum = PollUserVote.option_6.sum().alias("option_6_sum")
        val option7Sum = PollUserVote.option_7.sum().alias("option_7_sum")
        val option8Sum = PollUserVote.option_8.sum().alias("option_8_sum")
        val option9Sum = PollUserVote.option_9.sum().alias("option_9_sum")
        val option10Sum = PollUserVote.option_10.sum().alias("option_10_sum")
        val pollUserVote = PollUserVote.slice(
            pollIdAl,
            option1Sum,
            option2Sum,
            option3Sum,
            option4Sum,
            option5Sum,
            option6Sum,
            option7Sum,
            option8Sum,
            option9Sum,
            option10Sum
        ).selectAll().groupBy(PollUserVote.id).alias("poll_user_vote_q")
        Polls.join(pollUserVote, JoinType.INNER, Polls.id, pollUserVote[pollIdAl])
            .slice(
                Polls.id,
                Polls.userId,
                Polls.messageId,
                Polls.question,
                Polls.allowMultipleAnswers,
                Polls.options,
                pollUserVote[pollIdAl],
                pollUserVote[option1Sum],
                pollUserVote[option2Sum],
                pollUserVote[option3Sum],
                pollUserVote[option4Sum],
                pollUserVote[option5Sum],
                pollUserVote[option6Sum],
                pollUserVote[option7Sum],
                pollUserVote[option8Sum],
                pollUserVote[option9Sum],
                pollUserVote[option10Sum]
            )
            .select { Polls.status eq PollStatus.VOTING }
            .map {
                PollVoteDto(
                    id = it[Polls.id].value,
                    userId = it[Polls.userId],
                    messageId = it[Polls.messageId],
                    question = it[Polls.question],
                    allowMultipleAnswers = it[Polls.allowMultipleAnswers],
                    options = it[Polls.options],
                    results = it[Polls.options].mapIndexed { index, s ->
                        when (index + 1) {
                            1 -> it[pollUserVote[option1Sum]]
                            2 -> it[pollUserVote[option2Sum]]
                            3 -> it[pollUserVote[option3Sum]]
                            4 -> it[pollUserVote[option4Sum]]
                            5 -> it[pollUserVote[option5Sum]]
                            6 -> it[pollUserVote[option6Sum]]
                            7 -> it[pollUserVote[option7Sum]]
                            8 -> it[pollUserVote[option8Sum]]
                            9 -> it[pollUserVote[option9Sum]]
                            10 -> it[pollUserVote[option10Sum]]
                            else -> throw RuntimeException()
                        }!!
                    }
                )
            }
    }

    fun delete(pollId: Long) = transaction {
        PollUserVote.deleteWhere { PollUserVote.id eq pollId }
    }

    fun vote(pollId: Long, userId: Long, option: Int) = transaction {
        try {
            PollUserVote.insertAndGetId {
                it[PollUserVote.id] = pollId
                it[PollUserVote.userId] = userId
                when (option) {
                    1 -> it[option_1] = 1
                    2 -> it[option_2] = 1
                    3 -> it[option_3] = 1
                    4 -> it[option_4] = 1
                    5 -> it[option_5] = 1
                    6 -> it[option_6] = 1
                    7 -> it[option_7] = 1
                    8 -> it[option_8] = 1
                    9 -> it[option_9] = 1
                    10 -> it[option_10] = 1
                }
            }.value
        } catch (ex: ExposedSQLException) {
        }
    }

    fun vote(pollId: Long, userId: Long, options: List<Int>) = transaction {
        try {
            PollUserVote.insertAndGetId {
                it[PollUserVote.id] = pollId
                it[PollUserVote.userId] = userId
                options.forEach { option ->
                    when (option) {
                        1 -> it[option_1] = 1
                        2 -> it[option_2] = 1
                        3 -> it[option_3] = 1
                        4 -> it[option_4] = 1
                        5 -> it[option_5] = 1
                        6 -> it[option_6] = 1
                        7 -> it[option_7] = 1
                        8 -> it[option_8] = 1
                        9 -> it[option_9] = 1
                        10 -> it[option_10] = 1
                    }
                }
            }.value
        } catch (ex: ExposedSQLException) {
        }
    }

}