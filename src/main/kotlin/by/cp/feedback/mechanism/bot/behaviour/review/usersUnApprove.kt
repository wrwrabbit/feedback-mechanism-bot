package by.cp.feedback.mechanism.bot.behaviour.review

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.userUnApproveDC
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery

fun userUnApprove(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(userUnApproveDC.length).toLong()
    val userId = callback.user.id.chatId
    val poll = PollRepository.getById(id)
    PollUserReviewRepository.save(poll.id, userId, false)
    delete((callback as MessageDataCallbackQuery).message)
}
