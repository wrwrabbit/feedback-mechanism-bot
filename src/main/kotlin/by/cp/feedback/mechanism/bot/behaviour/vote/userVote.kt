package by.cp.feedback.mechanism.bot.behaviour.vote

import by.cp.feedback.mechanism.bot.model.userVoteDataCallback
import by.cp.feedback.mechanism.bot.repository.PollVoteRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun userVote(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val (pollId, index) = callback.data.substring(userVoteDataCallback.length).split("_")
        .let { it[0].toLong() to it[1].toInt() }
    PollVoteRepository.vote(pollId, index)
    delete((callback as MessageDataCallbackQuery).message)
}
