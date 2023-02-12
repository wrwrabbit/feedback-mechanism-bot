package by.cp.feedback.mechanism.bot.behaviour

import by.cp.feedback.mechanism.bot.approvalsRequired
import by.cp.feedback.mechanism.bot.exception.AlreadyApprovedException
import by.cp.feedback.mechanism.bot.exception.CantApproveRejectedException
import by.cp.feedback.mechanism.bot.exception.PollNotFoundInDbException
import by.cp.feedback.mechanism.bot.exception.YouAreNotOwnerOfPollException
import by.cp.feedback.mechanism.bot.model.PollStatus
import by.cp.feedback.mechanism.bot.repository.PollRepository
import dev.inmo.tgbotapi.extensions.behaviour_builder.BehaviourContext
import dev.inmo.tgbotapi.extensions.utils.extensions.raw.message
import dev.inmo.tgbotapi.requests.send.SendTextMessage
import dev.inmo.tgbotapi.types.queries.callback.DataCallbackQuery
import dev.inmo.tgbotapi.types.toChatId

fun approve(): suspend BehaviourContext.(DataCallbackQuery) -> Unit = { callback ->
    val id = callback.data.substring(8).toLong()
    val poll = PollRepository.getById(id) ?: throw PollNotFoundInDbException()
    if (poll.rejectionReason != null) throw CantApproveRejectedException()
    val userId: Long = callback.user.id.chatId
    if (userId in poll.approves) {
        execute(
            SendTextMessage(
                callback.message!!.chat.id,
                "You already approved this poll"
            )
        )
        throw AlreadyApprovedException()
    }
    val resultArray = poll.approves.plus(userId)
    PollRepository.updateApproves(id, resultArray)
    if (resultArray.size == approvalsRequired) {
        PollRepository.updateStatus(poll.id, PollStatus.READY_FOR_USER_REVIEW)
        execute(SendTextMessage(poll.userId.toChatId(), "Your poll #${poll.id} approved"))
    }
    execute(
        SendTextMessage(
            callback.message!!.chat.id,
            "You approved this poll, current approves ${resultArray.size}/$approvalsRequired"
        )
    )
}