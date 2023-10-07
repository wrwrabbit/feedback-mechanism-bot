package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.table.PollUserReview
import mu.KotlinLogging
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object PollUserReviewRepository {

    private val logger = KotlinLogging.logger {}

    fun save(pollId: Long, userId: Long, approved: Boolean) = transaction {
        try {
            PollUserReview.insert {
                it[PollUserReview.pollId] = pollId
                it[PollUserReview.userId] = userId
                it[PollUserReview.approved] = approved
            }
        } catch (ex: ExposedSQLException) {
            if (ex.message?.contains("duplicate key") != true) {
                logger.error(ex) { "Error while insert" }
            } else {

            }
        }
    }

    fun approvalsCount(pollId: Long) = transaction {
        PollUserReview.select { (PollUserReview.pollId eq pollId) and (PollUserReview.approved eq true) }.count()
    }

}