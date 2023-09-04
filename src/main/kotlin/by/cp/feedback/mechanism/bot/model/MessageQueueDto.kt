package by.cp.feedback.mechanism.bot.model

data class MessageQueueDto(
    val userId: Long,
    val pollId: Long,
    val type: MessageQueueType
)
