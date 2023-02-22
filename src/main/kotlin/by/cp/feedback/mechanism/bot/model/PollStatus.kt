package by.cp.feedback.mechanism.bot.model

enum class PollStatus {
    ON_MODERATOR_REVIEW,
    REJECTED,
    READY_FOR_USER_REVIEW,
    ON_USER_REVIEW,
    READY_FOR_VOTING,
    VOTING,
    ENDED
}