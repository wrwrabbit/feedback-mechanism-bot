package by.cp.feedback.mechanism.bot.repository

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
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
                id = id,
                userId = userId,
                status = status,
                question = question,
                options = options,
                allowMultipleAnswers = allowMultipleAnswers,
                createdAt = createdAt,
                startedAt = null,
                moderatorApproves = arrayOf(),
                rejectionReason = null,
                messageId = null,
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

    fun getLastPollByUserId(userId: Long): PollDto? = transaction {
        Polls.select { Polls.userId eq userId }
            .orderBy(Polls.id, SortOrder.DESC)
            .map(::pollDto)
            .firstOrNull()
    }

    fun getUserIdByPollId(pollId: Long): Long = transaction {
        Polls.select { Polls.id eq pollId }
            .map(::pollDto)
            .firstOrNull()?.userId!!
    }

    fun getByStatus(status: PollStatus): List<PollDto> = transaction {
        Polls.select { Polls.status eq status }
            .map(::pollDto)
    }

    fun getById(id: Long): PollDto = transaction {
        Polls.select { Polls.id eq id }
            .map(::pollDto)
            .firstOrNull()
    } ?: throw PollNotFoundInDbException(id.toString())

    fun lastUserPoll(userId: Long): PollDto? = transaction {
        Polls.select { Polls.userId eq userId }
            .orderBy(Polls.createdAt to SortOrder.DESC)
            .limit(1)
            .map(::pollDto)
            .firstOrNull()
    }

    private fun pollDto(it: ResultRow) = PollDto(
        id = it[Polls.id].value,
        userId = it[Polls.userId],
        status = it[Polls.status],
        question = it[Polls.question],
        options = it[Polls.options],
        allowMultipleAnswers = it[Polls.allowMultipleAnswers],
        createdAt = it[Polls.createdAt],
        startedAt = it[Polls.startedAt],
        moderatorApproves = it[Polls.moderatorApproves],
        rejectionReason = it[Polls.rejectionReason],
        messageId = it[Polls.messageId]
    )

}