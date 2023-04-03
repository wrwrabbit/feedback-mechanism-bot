package by.cp.feedback.mechanism.bot.model

import dev.inmo.tgbotapi.bot.ktor.telegramBot

val bot = telegramBot(System.getenv("TOKEN"))

val moderatorsChatId = System.getenv("MODERATORS_CHAT_ID").toLong()
val postChatId = System.getenv("POST_CHAT_ID").toLong()
val moderatorsApprovalsRequired = System.getenv("MODERATORS_APPROVALS_REQUIRED").toInt()
val usersApprovalsRequired = System.getenv("USERS_APPROVALS_REQUIRED").toLong()
val secondsBetweenPolls = System.getenv("SECONDS_BETWEEN_POLLS").toLong()
val secondsTillFinish = System.getenv("SECONDS_TILL_FINISH").toLong()

//MODERATION
const val moderatorApproveDC = "ModeratorsApproveDC"
const val moderatorFixDC = "ModeratorsFixDC"
const val moderatorRejectDC = "ModeratorsRejectDC"
const val userApproveModerationDC = "UserApproveModerationDC"
const val userRejectModerationDC = "UserRejectModerationDC"
const val showModerationDC = "ShowModerationDC"

//REVIEW
const val userUnApproveDC = "UsersUnApproveDC"
const val userApproveDC = "UsersApproveDC"

//VOTE
const val userVoteDC = "UserVoteDC"
const val userVoteMultipleAnswersDC = "UserVoteMultipleAnswersDC"
const val userVoteCheckAnswerDC = "UserVoteCheckAnswerDC"
