package by.cp.feedback.mechanism.bot.model

data class UserDto(
    val id: Long,
    val langCode: String,
    val status: UserStatus
)
