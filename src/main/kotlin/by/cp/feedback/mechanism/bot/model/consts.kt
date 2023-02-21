package by.cp.feedback.mechanism.bot.model

import dev.inmo.tgbotapi.bot.ktor.telegramBot

val bot = telegramBot(System.getenv("TOKEN"))

const val pollTemplate = "Вопрос: Сколько?\n" +
    "Ответ: 10\n" +
    "...\n" +
    "Ответ: 12\n" +
    "Больше одного ответа: Да"
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

//COMMANDS
const val startCommand = "start"
const val sendToUsersReviewCommand = "send_to_users_review"
const val sendToModeratorsReviewCommand = "send_to_moderators_review"
const val sendToVoteCommand = "send_to_vote"
const val getChatIdCommand = "get_chat_id"
const val rejectCommand = "reject"
const val unrejectCommand = "unreject"
const val myPollsCommand = "my_polls"
const val fixPollCommand = "fix_poll"
const val templateCommand = "template"
const val getPollCommand = "get_poll"
