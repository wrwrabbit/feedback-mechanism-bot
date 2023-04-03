package by.cp.feedback.mechanism.bot.model

import java.time.LocalDateTime

data class PollVoteDto(
    val id: Long,
    val messageId: Long? = null,
    val question: String,
    val allowMultipleAnswers: Boolean,
    val createdAt: LocalDateTime,
    val startedAt: LocalDateTime?,
    val finishedAt: LocalDateTime?,
    val options: Array<String>,
    val results: List<Long>
)