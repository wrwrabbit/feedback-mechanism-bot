package by.cp.feedback.mechanism.bot.model


data class CreatePollDto(
    val question: String,
    val options: Array<String>,
    val allowMultipleAnswers: Boolean
)