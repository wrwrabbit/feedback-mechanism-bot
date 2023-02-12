package by.cp.feedback.mechanism.bot.model

data class PollsModerationDto(
    val pollId: Long,
    val telegramId: Long,
    val approves: Array<Long>,
    val rejectionReason: String?
)