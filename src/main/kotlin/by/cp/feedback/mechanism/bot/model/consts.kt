package by.cp.feedback.mechanism.bot.model

import dev.inmo.tgbotapi.bot.ktor.telegramBot

val bot = telegramBot(System.getenv("TOKEN"))

val moderatorsChatId = System.getenv("MODERATORS_CHAT_ID").toLong()
val postChatId = System.getenv("POST_CHAT_ID").toLong()
const val moderatorsApprovalsRequired = 1
const val usersApprovalsRequired = 1L

//DATA_CALLBACK
const val moderatorApproveDataCallback = "ModeratorsApproveDataCallback"
const val userUnApproveDataCallback = "UsersUnApproveDataCallback"
const val userApproveDataCallback = "UsersApproveDataCallback"
const val userVoteDataCallback = "UserVoteDataCallback"
const val userVoteMultipleAnswersDataCallback = "UserVoteMultipleAnswersDataCallback"
const val userVoteCheckAnswerDataCallback = "UserVoteCheckAnswerDataCallback"
const val languageDataCallback = "LanguageDataCallback"
