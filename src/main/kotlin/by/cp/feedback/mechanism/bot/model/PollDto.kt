package by.cp.feedback.mechanism.bot.model

import java.time.LocalDateTime

data class PollDto(
    val id: Long,
    val userId: Long,
    val question: String,
    val options: Array<String>,
    val allowMultipleAnswers: Boolean,
    val createdAt: LocalDateTime
)