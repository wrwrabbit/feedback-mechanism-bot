package by.cp.feedback.mechanism.bot.behaviour.vote

import by.cp.feedback.mechanism.bot.behaviour.captcha.captchaRequest
import by.cp.feedback.mechanism.bot.model.userVoteDC
import by.cp.feedback.mechanism.bot.model.voteResultText
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.MarkdownParseMode
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun userVote(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val userId = callback.user.id.chatId
    val (pollId, index) = callback.data.substring(userVoteDC.length).split("_")
        .let { it[0].toLong() to it[1].toInt() }
    if (UserRepository.captchaRequired(userId)) {
        captchaRequest(userId, callback.user.id)
    }
    PollUserVoteRepository.vote(pollId, userId, index)
    UserRepository.voteCountInc(userId)
    val messageId = PollRepository.getById(pollId)?.messageId
    execute(SendTextMessage(userId.toChatId(), voteResultText(messageId), parseMode = MarkdownParseMode))
    delete((callback as MessageDataCallbackQuery).message)
}
