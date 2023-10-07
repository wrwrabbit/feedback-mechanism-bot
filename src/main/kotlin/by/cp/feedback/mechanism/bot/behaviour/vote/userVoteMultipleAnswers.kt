package by.cp.feedback.mechanism.bot.behaviour.vote

import by.cp.feedback.mechanism.bot.behaviour.captcha.captchaRequest
import by.cp.feedback.mechanism.bot.model.userVoteMultipleAnswersDC
import by.cp.feedback.mechanism.bot.model.voteResultText
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserVoteRepository
import by.cp.feedback.mechanism.bot.repository.UserRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.reply_markup
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.message.MarkdownParseMode
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun userVoteMultipleAnswers(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val userId = callback.user.id.chatId
    val pollId = callback.data.substring(userVoteMultipleAnswersDC.length).split("_")
        .let { it[0].toLong() }
    val message = (callback as MessageDataCallbackQuery).message
    val options = message.reply_markup!!.keyboard.mapIndexed { index, inlineKeyboardButtons ->
        index + 1 to inlineKeyboardButtons.first().text.contains("✅")
    }.filter {
        it.second
    }.map { it.first }
    if (UserRepository.captchaRequired(userId)) {
        captchaRequest(userId, callback.user.id)
    }
    PollUserVoteRepository.vote(pollId, userId, options)
    UserRepository.voteCountInc(userId)
    val messageId = PollRepository.getById(pollId)?.messageId
    execute(SendTextMessage(userId.toChatId(), voteResultText(messageId), parseMode = MarkdownParseMode))
    delete(callback.message)
}
