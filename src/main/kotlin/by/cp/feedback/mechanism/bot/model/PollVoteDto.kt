package by.cp.feedback.mechanism.bot.model

data class PollVoteDto(
    val id: Long,
    val messageId: Long? = null,
    val question: String,
    val allowMultipleAnswers: Boolean,
    val options: Array<String>,
    val results: List<Long>
)