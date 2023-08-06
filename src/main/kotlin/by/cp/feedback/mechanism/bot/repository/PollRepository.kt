package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.model.PollDto
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.table.Polls
import by.cp.feedback.mechanism.bot.util.PagedDto
import by.cp.feedback.mechanism.bot.util.toLimitOffsetTotalPages
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDateTime
import java.time.ZoneOffset

object PollRepository {

    fun save(userId: Long, question: String, options: Array<String>, allowMultipleAnswers: Boolean): PollDto =
        transaction {
            val createdAt = LocalDateTime.now(ZoneOffset.UTC)
            val status = PollStatus.ON_MODERATOR_REVIEW
            val id = Polls.insertAndGetId {
                it[Polls.userId] = userId
                it[Polls.status] = status
                it[Polls.question] = question
                it[Polls.options] = options
                it[Polls.allowMultipleAnswers] = allowMultipleAnswers
                it[Polls.createdAt] = createdAt
            }.value
            PollDto(
                id,
                userId,
                status,
                question,
                options,
                allowMultipleAnswers,
                createdAt,
                arrayOf(),
                0,
                null
            )
        }

    fun updatePoll(id: Long, question: String, options: Array<String>, allowMultipleAnswers: Boolean): PollDto =
        transaction {
            Polls.update({ Polls.id eq id }) {
                it[Polls.question] = question
                it[Polls.options] = options
                it[Polls.allowMultipleAnswers] = allowMultipleAnswers
            }
            getById(id)!!
        }

    fun updateStatus(id: Long, status: PollStatus) = transaction {
        Polls.update({ Polls.id eq id }) {
            it[Polls.status] = status
        }
    }

    fun start(id: Long, messageId: Long) = transaction {
        Polls.update({ Polls.id eq id }) {
            it[status] = PollStatus.VOTING
            it[startedAt] = LocalDateTime.now(ZoneOffset.UTC)
            it[Polls.messageId] = messageId
        }
    }

    fun finish(id: Long) = transaction {
        Polls.update({ Polls.id eq id }) {
            it[status] = PollStatus.FINISHED
            it[finishedAt] = LocalDateTime.now(ZoneOffset.UTC)
        }
    }

    fun updateApproves(id: Long, approves: Array<Long>) = transaction {
        Polls.update({ Polls.id eq id }) {
            it[moderatorApproves] = approves
        }
    }

    fun addUserApprove(id: Long) = transaction {
        Polls.update({ Polls.id eq id }) {
            with(SqlExpressionBuilder) {
                it.update(userApproves, userApproves + 1)
            }
        }
    }

    fun updateRejectionReason(id: Long, rejectionReason: String?) = transaction {
        Polls.update({ Polls.id eq id }) {
            it[Polls.rejectionReason] = rejectionReason
        }
    }

    fun getByUserId(userId: Long, page: Int): PagedDto<PollDto> = transaction {
        val size = 8
        val totalElements = Polls.select { Polls.userId eq userId }.count()
        val (limit, offset, totalPages) = toLimitOffsetTotalPages(page, size, totalElements)
        val polls = Polls.select { Polls.userId eq userId }
            .orderBy(Polls.id)
            .limit(limit, offset)
            .map(::pollDto)
        PagedDto(
            polls,
            page,
            size,
            totalPages,
            totalElements
        )
    }

    fun getByStatus(status: PollStatus): List<PollDto> = transaction {
        Polls.select { Polls.status eq status }
            .map(::pollDto)
    }

    fun getById(id: Long): PollDto? = transaction {
        Polls.select { Polls.id eq id }
            .map(::pollDto)
            .firstOrNull()
    }

    fun lastUserPoll(userId: Long): PollDto? = transaction {
        Polls.select { Polls.userId eq userId }
            .orderBy(Polls.createdAt to SortOrder.DESC)
            .limit(1)
            .map(::pollDto)
            .firstOrNull()
    }

    private fun pollDto(it: ResultRow) = PollDto(
        it[Polls.id].value,
        it[Polls.userId],
        it[Polls.status],
        it[Polls.question],
        it[Polls.options],
        it[Polls.allowMultipleAnswers],
        it[Polls.createdAt],
        it[Polls.moderatorApproves],
        it[Polls.userApproves],
        it[Polls.rejectionReason]
    )

}