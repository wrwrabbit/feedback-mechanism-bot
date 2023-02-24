package by.cp.feedback.mechanism.bot.behaviour.vote

import by.cp.feedback.mechanism.bot.model.userVoteMultipleAnswersDataCallback
import by.cp.feedback.mechanism.bot.repository.PollVoteRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.reply_markup
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun userVoteMultipleAnswers(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val pollId = callback.data.substring(userVoteMultipleAnswersDataCallback.length).split("_")
        .let { it[0].toLong() }
    val message = (callback as MessageDataCallbackQuery).message
    val options = message.reply_markup!!.keyboard.mapIndexed { index, inlineKeyboardButtons ->
        index + 1 to inlineKeyboardButtons.first().text.contains("✅")
    }.filter {
        it.second
    }.map { it.first }
    PollVoteRepository.vote(pollId, options)
    delete(callback.message)
}
