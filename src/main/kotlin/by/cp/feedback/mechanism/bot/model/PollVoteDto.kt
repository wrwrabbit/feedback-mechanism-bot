package by.cp.feedback.mechanism.bot.model

import java.time.LocalDateTime

data class PollVoteDto(
    val id: Long,
    val userId: Long?,
    val messageId: Long? = null,
    val question: String,
    val allowMultipleAnswers: Boolean,
    val options: Array<String>,
    val results: List<Long>,
    val startedAt: LocalDateTime?
)