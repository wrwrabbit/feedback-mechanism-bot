package by.cp.feedback.mechanism.bot.model

import dev.inmo.tgbotapi.bot.ktor.telegramBot

val bot = telegramBot(System.getenv("TOKEN"))

val moderatorsChatId = System.getenv("MODERATORS_CHAT_ID").toLong()
val postChatId = System.getenv("POST_CHAT_ID").toLong()
val moderatorsApprovalsRequired = System.getenv("MODERATORS_APPROVALS_REQUIRED").toInt()
val usersApprovalsRequired = System.getenv("USERS_APPROVALS_REQUIRED").toLong()
val secondsBetweenPolls = System.getenv("SECONDS_BETWEEN_POLLS").toLong()

//DATA_CALLBACK
const val moderatorApproveDataCallback = "ModeratorsApproveDataCallback"
const val userUnApproveDataCallback = "UsersUnApproveDataCallback"
const val userApproveDataCallback = "UsersApproveDataCallback"
const val userVoteDataCallback = "UserVoteDataCallback"
const val userVoteMultipleAnswersDataCallback = "UserVoteMultipleAnswersDataCallback"
const val userVoteCheckAnswerDataCallback = "UserVoteCheckAnswerDataCallback"
const val languageDataCallback = "LanguageDataCallback"
