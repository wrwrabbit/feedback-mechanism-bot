package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.model.userApproveDataCallback
import by.cp.feedback.mechanism.bot.model.usersApprovalsRequired
import by.cp.feedback.mechanism.bot.repository.PollRepository
import by.cp.feedback.mechanism.bot.repository.PollUserReviewRepository
import dev.inmo.tgbotapi.extensions.api.delete
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.queries.callback.MessageDataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun userApprove(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(userApproveDataCallback.length).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    PollRepository.addUserApprove(id)
    if (poll.userApproves + 1 == usersApprovalsRequired) {
        PollRepository.updateStatus(poll.id, PollStatus.READY_FOR_VOTING)
        PollUserReviewRepository.delete(poll.id)
        execute(SendTextMessage(poll.userId.toChatId(), "Your poll #${poll.id} ready for voting"))
    }
    delete((callback as MessageDataCallbackQuery).message)
}
